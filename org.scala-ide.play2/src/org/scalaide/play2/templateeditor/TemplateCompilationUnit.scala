package org.scalaide.play2.templateeditor

import java.io.PrintStream

import scala.tools.eclipse.InteractiveCompilationUnit
import scala.tools.eclipse.ScalaPlugin
import scala.tools.eclipse.ScalaPresentationCompiler
import scala.tools.eclipse.logging.HasLogger
import scala.tools.eclipse.util.EclipseResource
import scala.tools.nsc.interactive.Response
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualFile
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.util.SourceFile
import scala.util.Try

import org.eclipse.core.resources.IFile
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Region
import org.eclipse.ui.IEditorInput
import org.eclipse.ui.part.FileEditorInput
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.PlayProject
import org.scalaide.play2.templateeditor.compiler.CompilerUsing
import org.scalaide.play2.templateeditor.compiler.PositionHelper

import play.templates.GeneratedSourceVirtual

/** A Template compilation unit connects the presentation compiler
 *  view of a tmeplate with the Eclipse IDE view of the underlying
 *  resource.
 */
case class TemplateCompilationUnit(val workspaceFile: IFile) extends InteractiveCompilationUnit with HasLogger {

  private var document: Option[IDocument] = None

  override val file: AbstractFile = EclipseResource(workspaceFile)

  /** A virtual file which is in synch with content of the document
   *  in order not to use a temporary real file
   */
  private lazy val templateSourceFile = {
    new VirtualFile(getTemplateFullPath)
  }

  override lazy val scalaProject = ScalaPlugin.plugin.asScalaProject(workspaceFile.getProject).get
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

  override def exists(): Boolean = true

  /** Return contents of generated scala file
   */
  override def getContents: Array[Char] = {
    withSourceFile({ (sourceFile, compiler) =>
      sourceFile.content
    })(null)
  }

  /** Return contents of template file
   */
  def getTemplateContents: String = document.map(_.get).getOrElse(scalax.file.Path(file.file).string())

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

  override def withSourceFile[T](op: (SourceFile, ScalaPresentationCompiler) => T)(orElse: => T = scalaProject.defaultOrElse): T = {
    playProject.withSourceFile(this)(op) getOrElse (orElse)
  }

  /** maps a region in template file into generated scala file
   */
  def mapTemplateToScalaRegion(region: IRegion): Option[IRegion] = synchronized {
    for { 
      start <- mapTemplateToScalaOffset(region.getOffset())
      end <- mapTemplateToScalaOffset(region.getOffset() + region.getLength() - 1)
    } yield new Region(start, end - start + 1)
  }

  /** maps an offset in template file into generated scala file
   */
  def mapTemplateToScalaOffset(offset: Int): Option[Int] = synchronized {
    for(genSource <- generatedSource().toOption) yield {
      playProject.withPresentationCompiler { pc =>
        PositionHelper.mapSourcePosition(genSource.matrix, offset)
      }
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
      cachedGenerated = CompilerUsing.compileTemplateToScalaVirtual(getTemplateContents.toString(), file.file, playProject)
    }
    cachedGenerated
  }

  /** updates template virtual file
   */
  def updateTemplateSourceFile() = {
    new PrintStream(templateSourceFile.output).print(document.map(_.get))
  }

}

object TemplateCompilationUnit {
  private def fromEditorInput(editorInput: IEditorInput): TemplateCompilationUnit = TemplateCompilationUnit(getFile(editorInput))

  def fromEditor(templateEditor: TemplateEditor): TemplateCompilationUnit = {
    val input = templateEditor.getEditorInput
    if(input == null) 
      throw new NullPointerException("No editor input for the passed `templateEditor`. Hint: Maybe the editor isn't yet fully initialized?")
    else {
      val unit = fromEditorInput(input)
      unit.connect(templateEditor.getDocumentProvider().getDocument(input))
    }
  }

  private def getFile(editorInput: IEditorInput): IFile = 
    editorInput match {
      case fileEditorInput: FileEditorInput if fileEditorInput.getName.endsWith(PlayPlugin.TemplateExtension) =>
        fileEditorInput.getFile
      case _ => throw new IllegalArgumentException("Expected to open file with extension %s, found %s.".format(PlayPlugin.TemplateExtension, editorInput.getName))
    }
}