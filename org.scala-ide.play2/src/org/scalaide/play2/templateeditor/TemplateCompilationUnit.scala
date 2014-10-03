package org.scalaide.play2.templateeditor

import java.io.PrintStream
import org.scalaide.core.IScalaPlugin
import org.scalaide.core.compiler.IScalaPresentationCompiler
import org.scalaide.core.IScalaProject
import org.scalaide.logging.HasLogger
import scala.tools.nsc.interactive.Response
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualFile
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.util.SourceFile
import scala.util.{ Try, Success, Failure }
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Region
import org.eclipse.ui.IEditorInput
import org.eclipse.ui.part.FileEditorInput
import org.scalaide.play2.IssueTracker
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.PlayProject
import org.scalaide.play2.templateeditor.compiler.CompilerUsing
import org.scalaide.play2.templateeditor.compiler.PositionHelper
import play.twirl.compiler.GeneratedSourceVirtual
import org.scalaide.ui.editor.CompilationUnit
import org.scalaide.ui.editor.CompilationUnitProvider
import org.scalaide.ui.internal.actions.ToggleScalaNatureAction
import org.scalaide.core.compiler.ISourceMap
import org.scalaide.core.compiler.IPositionInformation
import org.scalaide.play2.templateeditor.compiler.TemplateToScalaCompilationError
import org.scalaide.core.compiler.ScalaCompilationProblem
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities
import org.scalaide.core.extensions.SourceFileProvider
import org.eclipse.core.runtime.IPath
import org.scalaide.util.internal.eclipse.EclipseUtils
import org.scalaide.core.compiler.InteractiveCompilationUnit

/** A Template compilation unit connects the presentation compiler
 *  view of a tmeplate with the Eclipse IDE view of the underlying
 *  resource.
 */
case class TemplateCompilationUnit(_workspaceFile: IFile, val usesInclusiveDot: Boolean) extends CompilationUnit(_workspaceFile) with HasLogger {

  /** A virtual file which is in synch with content of the document
   *  in order not to use a temporary real file
   *
   *  FIXME: This does not go through the `ScalaFileManager`
   */
  private lazy val templateSourceFile = {
    new VirtualFile(getTemplateFullPath)
  }

  override val scalaProject: IScalaProject = {
    def obtainScalaProject(project: IProject): IScalaProject = {
      IScalaPlugin().asScalaProject(project) match {
        case Some(scalaProject) => scalaProject
        case None =>
          def programmaticallyAddScalaNature(project: IProject): Unit = {
            val toggleScalaNature = new ToggleScalaNatureAction()
            toggleScalaNature.performAction(project)
          }
          programmaticallyAddScalaNature(project)
          IScalaPlugin().asScalaProject(project) getOrElse {
            val message = s"Failed to create a ScalaProject instance for Play template ${workspaceFile.getFullPath().toOSString()}. ${IssueTracker.createATicketMessage}"
            throw new IllegalStateException(message)
          }
      }
    }

    obtainScalaProject(workspaceFile.getProject)
  }

  lazy val playProject = PlayProject(scalaProject)

  private def getTemplateFullPath = file.file.getAbsolutePath()

  @volatile private var lastInfo: TemplateSourceMap = _

  override def lastSourceMap(): TemplateSourceMap = {
    if (lastInfo eq null)
      lastInfo = sourceMap(getContents())
    lastInfo
  }

  override def sourceMap(contents: Array[Char]): TemplateSourceMap ={
    lastInfo = new TemplateSourceMap(contents)
    lastInfo
  }

  override def getContents(): Array[Char] =
    document.map(_.get.toCharArray).getOrElse(file.toCharArray)

  /** Return contents of template file. */
  def getTemplateContents: String = document.map(_.get).getOrElse(scala.io.Source.fromFile(file.file).mkString)

  override def currentProblems() = {
    lastSourceMap().generatedSource match {
      case Success(_) =>
        super.currentProblems()
      case Failure(parseError: TemplateToScalaCompilationError) =>
        List(parseError.toProblem)

      case Failure(error) =>
        logger.error(s"Unexpected error while parsing template ${file.name}", error)
        List(unknownError(this, error))
    }
  }

  private def unknownError(tcu: TemplateCompilationUnit, error: Throwable) = {
    val message = s"${error.getMessage()} - ${error.getClass()}"
    ScalaCompilationProblem(
      getTemplateFullPath,
      ProblemSeverities.Error,
      message,
      0,
      1,
      1,
      1)
  }

  /** maps a region in template file into generated scala file
   */
  def mapTemplateToScalaRegion(region: IRegion): Option[IRegion] = synchronized {
    val start = lastSourceMap().scalaPos(region.getOffset())
    Some(new Region(start, region.getLength))
  }

  class TemplateSourceMap(override val originalSource: Array[Char]) extends ISourceMap {
    lazy val generatedSource: Try[GeneratedSourceVirtual] = {
      logger.debug("[generating template] " + getTemplateFullPath)
      CompilerUsing.compileTemplateToScalaVirtual(originalSource.mkString(""), file.file, playProject, usesInclusiveDot)
    }

    /** The translated Scala source code, for example the translation of a Play HTML template. */
    override def scalaSource: Array[Char] = {
      generatedSource.map(_.content).getOrElse("").toCharArray()
    }

    /** Map from the original source into the corresponding position in the Scala translation. */
    def scalaPos: IPositionInformation = new IPositionInformation {
      def apply(pos: Int): Int = {
        (for(genSource <- generatedSource.toOption) yield {
          PositionHelper.mapSourcePosition(genSource.matrix, pos)
        }) getOrElse 0
      }

      def offsetToLine(offset: Int): Int = sourceFile.offsetToLine(offset)

      def lineToOffset(line: Int): Int = sourceFile.lineToOffset(line)
    }

    /** Map from Scala source to its equivalent in the original source. */
    def originalPos: IPositionInformation = new IPositionInformation {
      // not a Scala source file, but still a source file. Used to implement line/offset translations
      private val src = new BatchSourceFile(file, originalSource)

      def apply(pos: Int) =
        generatedSource.toOption.map(_.mapPosition(pos)).getOrElse(0)

      def offsetToLine(offset: Int): Int =
        src.offsetToLine(offset)

      def lineToOffset(line: Int): Int =
        src.lineToOffset(line)
    }

    /** Return a compiler `SourceFile` implementation with the given contents. The implementation decides
     *  if this is a batch file or a script/other kind of source file.
     */
    val sourceFile: SourceFile = new BatchSourceFile(file, scalaSource)
  }

  override def toString(): String =
    s"$file <usesInclusiveDot=$usesInclusiveDot>"

  override def hashCode: Int = file.hashCode
  override def equals(other: Any): Boolean = other match {
    case that: TemplateCompilationUnit => this.file == that.file
    case _ => false
  }
}

class TemplateCompilationUnitProvider(val usesInclusiveDot: Boolean) extends CompilationUnitProvider[TemplateCompilationUnit] with SourceFileProvider {
  def this() {
    this(false)
  }

  override protected def mkCompilationUnit(file: IFile): TemplateCompilationUnit = TemplateCompilationUnit(file, usesInclusiveDot)
  override protected def fileExtension: String = PlayPlugin.TemplateExtension

  def createFrom(path: IPath): Option[InteractiveCompilationUnit] = {
    Option(mkCompilationUnit(EclipseUtils.workspaceRoot.getFile(path)))
  }
}