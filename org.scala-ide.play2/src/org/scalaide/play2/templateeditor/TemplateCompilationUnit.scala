package org.scalaide.play2.templateeditor

import java.io.PrintStream
import org.scalaide.core.ScalaPlugin
import org.scalaide.core.compiler.ScalaPresentationCompiler
import org.scalaide.core.internal.project.ScalaProject
import org.scalaide.logging.HasLogger
import scala.tools.nsc.interactive.Response
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualFile
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.util.SourceFile
import scala.util.Try
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
import org.scalaide.editor.CompilationUnit
import org.scalaide.editor.CompilationUnitProvider
import org.scalaide.ui.internal.actions.ToggleScalaNatureAction

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

  override val scalaProject: ScalaProject = {
    def obtainScalaProject(project: IProject): ScalaProject = {
      ScalaPlugin.plugin.asScalaProject(project) match {
        case Some(scalaProject) => scalaProject
        case None =>
          def programmaticallyAddScalaNature(project: IProject): Unit = {
            val toggleScalaNature = new ToggleScalaNatureAction()
            toggleScalaNature.performAction(project)  
          }
          programmaticallyAddScalaNature(project)
          ScalaPlugin.plugin.asScalaProject(project) getOrElse {
            val message = s"Failed to create a ScalaProject instance for Play template ${workspaceFile.getFullPath().toOSString()}. ${IssueTracker.createATicketMessage}"
            throw new IllegalStateException(message)
          }
      }
    }

    obtainScalaProject(workspaceFile.getProject)
  }

  lazy val playProject = PlayProject(scalaProject)

  def getTemplateName = workspaceFile.getName()

  def getTemplateFullPath = file.file.getAbsolutePath()

  /** Return the compiler ScriptSourceFile corresponding to this unit. */
  override def sourceFile(contents: Array[Char]): SourceFile = {
    batchSourceFile(contents)
  }

  /** Return the compiler ScriptSourceFile corresponding to this unit. */
  def batchSourceFile(contents: Array[Char]): BatchSourceFile = {
    new BatchSourceFile(templateSourceFile, contents)
  }

  /** Return contents of generated scala file. */
  override def getContents: Array[Char] = {
    withSourceFile({ (sourceFile, compiler) =>
      sourceFile.content
    }).orNull
  }

  /** Return contents of template file. */
  def getTemplateContents: String = document.map(_.get).getOrElse(scala.io.Source.fromFile(file.file).mkString)

  override def currentProblems: List[IProblem] = {
    playProject.withPresentationCompiler { pc =>
      pc.problemsOf(this)
    }
  }

  /** Reconcile the unit. Return all compilation errors.
   *  Blocks until the unit is type-checked.
   */
  override def reconcile(newContents: String): List[IProblem] = {
    playProject.withPresentationCompiler { pc =>
      askReload(newContents.toCharArray)
      pc.problemsOf(this)
    }
  }

  def askReload(newContents: Array[Char] = getTemplateContents.toCharArray): Unit =
    playProject.withPresentationCompiler { pc =>
      pc.askReload(this, newContents)
    }

  override def doWithSourceFile(op: (SourceFile, ScalaPresentationCompiler) => Unit) {
    playProject.withSourceFile(this)(op)
  }

  override def withSourceFile[T](op: (SourceFile, ScalaPresentationCompiler) => T): Option[T] = {
    playProject.withSourceFile(this)(op)
  }

  /** maps a region in template file into generated scala file
   */
  def mapTemplateToScalaRegion(region: IRegion): Option[IRegion] = synchronized {
    for { 
      start <- mapTemplateToScalaOffset(region.getOffset())
      end <- mapTemplateToScalaOffset(region.getOffset() + region.getLength())
    } yield {
      val range = end - start
      new Region(start, if (range > 0) range + 1 else 0)
    }
  }

  /** maps an offset in template file into generated scala file
   */
  def mapTemplateToScalaOffset(offset: Int): Option[Int] = synchronized {
    for(genSource <- generatedSource().toOption) yield {
      PositionHelper.mapSourcePosition(genSource.matrix, offset)
    }
  }

  /** Return the offset in the template file, given an offset in the generated source file.
   *  It is the inverse of `mapTemplateToScalaOffset`. */
  def templateOffset(generatedOffset: Int): Option[Int] = synchronized {
    generatedSource().toOption.map(_.mapPosition(generatedOffset))
  }
  /* guarded by `this`*/
  private var cachedGenerated: Try[GeneratedSourceVirtual] = generatedSource()
  /* guarded by `this`*/
  private var oldContents = getTemplateContents

  /** Returns generated source of the given compilation unit.
   * 
   *  It caches results in order to save on (relatively expensive) calls to the template compiler.
   */
  def generatedSource(): Try[GeneratedSourceVirtual] = synchronized {
    if (oldContents != getTemplateContents) {
      oldContents = getTemplateContents
      logger.debug("[generating template] " + getTemplateFullPath)
      cachedGenerated = CompilerUsing.compileTemplateToScalaVirtual(getTemplateContents.toString(), file.file, playProject, usesInclusiveDot)
    }
    cachedGenerated
  }

  /** updates template virtual file
   */
  def updateTemplateSourceFile() = {
    new PrintStream(templateSourceFile.output).print(document.map(_.get))
  }

}

case class TemplateCompilationUnitProvider(val usesInclusiveDot: Boolean) extends CompilationUnitProvider[TemplateCompilationUnit] {
  override protected def mkCompilationUnit(file: IFile): TemplateCompilationUnit = TemplateCompilationUnit(file, usesInclusiveDot)
  override protected def fileExtension: String = PlayPlugin.TemplateExtension
}