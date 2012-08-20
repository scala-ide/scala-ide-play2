package org.scalaide.play2.templateeditor

import scala.tools.eclipse.InteractiveCompilationUnit
import scala.tools.eclipse.ScalaPlugin
import scala.tools.eclipse.resources.MarkerFactory
import scala.tools.eclipse.util.EclipseResource
import scala.tools.nsc.interactive.Response
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.util.SourceFile

import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IResource
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.Position
import org.eclipse.ui.IEditorInput
import org.eclipse.ui.part.FileEditorInput
import org.eclipse.ui.texteditor.ITextEditor
import org.scalaide.play2.PlayPlugin

/**
 * A Script compilation unit connects the presentation compiler
 *  view of a script with the Eclipse IDE view of the underlying
 *  resource.
 */
case class TemplateCompilationUnit(val workspaceFile: IFile) extends InteractiveCompilationUnit {

  private var document: Option[IDocument] = None

  override def file: AbstractFile = EclipseResource(workspaceFile)

  override lazy val scalaProject = ScalaPlugin.plugin.asScalaProject(workspaceFile.getProject).get

  //  /** Return the compiler ScriptSourceFile corresponding to this unit. */
  //  override def sourceFile(contents: Array[Char]): ScriptSourceFile = {
  //    ScriptSourceFile.apply(file, contents)
  //  }
  /** Return the compiler ScriptSourceFile corresponding to this unit. */
  override def sourceFile(contents: Array[Char]): SourceFile = {
    batchSourceFile(contents)
  }

  /** Return the compiler ScriptSourceFile corresponding to this unit. */
  def batchSourceFile(contents: Array[Char]): BatchSourceFile = {
    new BatchSourceFile(file, contents)
  }

  override def exists(): Boolean = true

  override def getContents: Array[Char] = document.map(_.get.toCharArray).getOrElse(file.toCharArray)

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
    scalaProject.withPresentationCompiler { pc =>
      pc.problemsOf(file)
    }(Nil)
  }

  /**
   * Reconcile the unit. Return all compilation errors.
   *  Blocks until the unit is type-checked.
   */
  override def reconcile(newContents: String): List[IProblem] = {
    // FIXME just for test
    val severityLevel = ProblemSeverities.Error
    val msg = "message!"
    val p = new DefaultProblem(
      file.file.getAbsolutePath().toCharArray,
      msg,
      IProblem.Syntax,
      new Array[String](0),
      severityLevel,
      20,
      25,
      2,
      5)
    List(p)
    //    scalaProject.withPresentationCompiler { pc =>
    //      askReload(newContents.toCharArray)
    //      pc.problemsOf(file)
    //    }(Nil)
  }

  def askReload(newContents: Array[Char] = Array()): Unit = {
    // TODO just for test
  }
  //  def askReload(newContents: Array[Char] = getContents): Unit = {
  //    scalaProject.withPresentationCompiler { pc =>
  //      val src = batchSourceFile(newContents)
  //      pc.withResponse[Unit] { response =>
  //        pc.askReload(List(src), response)
  //        response.get
  //      }
  //    }()

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

  object TemplateProblemMarker extends MarkerFactory(PlayPlugin.plugin.problemMarkerId)

}

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