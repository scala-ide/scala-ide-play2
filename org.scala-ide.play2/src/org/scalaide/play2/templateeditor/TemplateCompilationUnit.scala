package org.scalaide.play2.templateeditor

import scala.tools.eclipse.InteractiveCompilationUnit
import scala.tools.eclipse.ScalaPlugin
import scala.tools.eclipse.ScalaPresentationCompiler
import scala.tools.eclipse.resources.MarkerFactory
import scala.tools.eclipse.util.EclipseFile
import scala.tools.eclipse.util.EclipseResource
import scala.tools.nsc.interactive.Response
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.util.SourceFile
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IResource
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.Position
import org.eclipse.jface.text.Region
import org.eclipse.ui.IEditorInput
import org.eclipse.ui.part.FileEditorInput
import org.eclipse.ui.texteditor.ITextEditor
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.PlayProject
import org.scalaide.play2.templateeditor.compiler.PositionHelper
import org.eclipse.core.runtime.Path

/**
 * A Script compilation unit connects the presentation compiler
 *  view of a script with the Eclipse IDE view of the underlying
 *  resource.
 */
case class TemplateCompilationUnit(val workspaceFile: IFile) extends InteractiveCompilationUnit {

  private var document: Option[IDocument] = None

  override val file: AbstractFile = EclipseResource(workspaceFile)

  lazy val templateSourceFile = {
    def relativePath(first: String, second: String) = {
      first.substring(first.indexOf(second))
    }
    val relPath = relativePath(file.file.getAbsolutePath(), scalaProject.underlying.getFullPath().toString())
    val fileName = playProject.sourceDir.getAbsolutePath() + relPath
    EclipseResource.fromString(fileName) match {
      case Some(v) => v match {
        case ef @ EclipseFile(_) => ef
        case _ => null
      }
      case None => null
    }
  }

  override lazy val scalaProject = ScalaPlugin.plugin.asScalaProject(workspaceFile.getProject).get
  lazy val playProject = PlayProject(scalaProject)

  def getTemplateName = workspaceFile.getName()

  def getTemplateFullPath = file.file.getAbsolutePath().toCharArray

  /** Return the compiler ScriptSourceFile corresponding to this unit. */
  override def sourceFile(contents: Array[Char]): SourceFile = {
    batchSourceFile(contents)
  }

  /** Return the compiler ScriptSourceFile corresponding to this unit. */
  def batchSourceFile(contents: Array[Char]): BatchSourceFile = {
    new BatchSourceFile(templateSourceFile, contents)
  }

  override def exists(): Boolean = true

  override def getContents: Array[Char] = {
    withSourceFile({ (sourceFile, compiler) =>
      sourceFile.content
    })()
  }

  def getTemplateContents: Array[Char] = document.map(_.get.toCharArray).getOrElse(templateSourceFile.toCharArray)

  /** no-op */
  override def scheduleReconcile(): Response[Unit] = {
    val r = new Response[Unit]
    r.set()
    r
  }

  def connect(doc: IDocument): this.type = {
    document = Option(doc)
    this
  }

  override def currentProblems: List[IProblem] = {
    playProject.withPresentationCompiler { pc =>
      pc.problemsOf(this)
    }
  }

  /**
   * Reconcile the unit. Return all compilation errors.
   *  Blocks until the unit is type-checked.
   */
  override def reconcile(newContents: String): List[IProblem] = {
    playProject.withPresentationCompiler { pc =>
      askReload(newContents.toCharArray)
      pc.problemsOf(this)
    }
  }

  def askReload(newContents: Array[Char] = getTemplateContents): Unit =
    playProject.withPresentationCompiler { pc =>
      pc.askReload(this, newContents)
    }

  // TODO should be cleaner
  override def withSourceFile[T](op: (SourceFile, ScalaPresentationCompiler) => T)(orElse: => T = scalaProject.defaultOrElse): T = {
    playProject.withSourceFile(this)(op)
  }

  def clearBuildErrors(): Unit = {
    workspaceFile.deleteMarkers(PlayPlugin.plugin.problemMarkerId, true, IResource.DEPTH_INFINITE)
  }

  def reportBuildError(errorMsg: String, start: Int, end: Int, line: Int): Unit = {
    reportBuildError(errorMsg, new Position(start, end - start + 1), line)
  }
  def reportBuildError(errorMsg: String, position: Position, line: Int): Unit = {
    def positionConvertor(position: Position, line: Int) = {
      MarkerFactory.RegionPosition(position.offset, position.length, line)
    }
    val pos = positionConvertor(position, line)
    TemplateProblemMarker.create(workspaceFile, IMarker.SEVERITY_ERROR, errorMsg, pos)
  }


  def mapTemplateToScalaRegion(region: Region) = {
    synchronized {
      val offset = mapTemplateToScalaOffset(region.getOffset())
      // TODO it is changed a little!
      val end = mapTemplateToScalaOffset(region.getOffset() + region.getLength() - 1)
      new Region(offset, end - offset + 1)
    }
  }

  def mapTemplateToScalaOffset(offset: Int) = {
    playProject.withPresentationCompiler { pc =>
      val gen = pc.generatedSource(this)
      PositionHelper.mapSourcePosition(gen.matrix, offset)
    }
  }
  
  def updateTemplateSourceFile() = {
    scalax.file.Path(templateSourceFile.file).write(document.get.get)
  }

  // these lines are for supporting javaHyperlinking XXX

  //  def newSearchableEnvironment(workingCopyOwner : WorkingCopyOwner) : SearchableEnvironment = {
  //    val getJavaProject = scalaProject.javaProject
  //    val javaProject = getJavaProject.asInstanceOf[JavaProject]
  //    javaProject.newSearchableNameEnvironment(workingCopyOwner)
  //  }
  //
  //  def newSearchableEnvironment() : SearchableEnvironment =
  //    newSearchableEnvironment(DefaultWorkingCopyOwner.PRIMARY)

  // until here

}

object TemplateProblemMarker extends MarkerFactory(PlayPlugin.plugin.problemMarkerId)

object TemplateCompilationUnit {
  //  val instance = new TemplateCompilationUnit(null)
  def fromEditorInput(editorInput: IEditorInput): Option[TemplateCompilationUnit] = {
    getFile(editorInput).map(TemplateCompilationUnit.apply)
  }

  def fromEditor(textEditor: ITextEditor): Option[TemplateCompilationUnit] = {
    val input = textEditor.getEditorInput
    for (unit <- fromEditorInput(input))
      yield unit.connect(textEditor.getDocumentProvider().getDocument(input))
  }

  private def getFile(editorInput: IEditorInput): Option[IFile] =
    editorInput match {
      case fileEditorInput: FileEditorInput if fileEditorInput.getName.endsWith("scala.html") =>
        Some(fileEditorInput.getFile)
      case _ => None
    }
}