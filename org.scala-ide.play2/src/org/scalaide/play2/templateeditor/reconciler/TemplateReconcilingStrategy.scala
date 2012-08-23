package org.scalaide.play2.templateeditor
package reconciler

import scala.tools.eclipse.logging.HasLogger
import org.eclipse.core.resources.IMarker
import org.eclipse.jdt.core.IJavaModelMarker
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jdt.internal.core.builder.JavaBuilder
import org.eclipse.jdt.internal.ui.javaeditor.JavaMarkerAnnotation
import org.eclipse.jface.text.DocumentEvent
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IDocumentListener
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Position
import org.eclipse.jface.text.reconciler.DirtyRegion
import org.eclipse.jface.text.reconciler.IReconcilingStrategy
import org.eclipse.ui.IFileEditorInput
import org.eclipse.ui.texteditor.ITextEditor
import org.eclipse.ui.texteditor.MarkerAnnotation
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.scalaide.play2.PlayPlugin
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider.ProblemAnnotation

class TemplateReconcilingStrategy(textEditor: /*ITextEditor*/ TemplateEditor) extends IReconcilingStrategy with HasLogger {
  private var document: IDocument = _
  private lazy val annotationModel = textEditor.getDocumentProvider.getAnnotationModel(textEditor.getEditorInput)
  //  private val annotationModel = textEditor.retrieveSourceViewer.getAnnotationModel()
  //  val annotationModel = new AnnotationModel()

  lazy val templateUnit = TemplateCompilationUnit.fromEditor(textEditor).get // we know the editor is a Template editor
  //  val templateUnit = TemplateCompilationUnit.instance

  def setDocument(doc: IDocument) {
    document = doc

    doc.addDocumentListener(reloader)
  }

  def reconcile(dirtyRegion: DirtyRegion, subRegion: IRegion) {
    logger.debug("Incremental reconciliation not implemented.")
  }

  def reconcile(partition: IRegion) {
    val errors = templateUnit.reconcile(document.get)

    updateErrorAnnotations(errors)
  }

  private var previousAnnotations = List[ProblemAnnotation]()
  //  private var previousAnnotations = List[SimpleMarkerAnnotation]()

  def createMarkerAnnotation(problem: IProblem) = {
    /*
     val markerType = PlayPlugin.plugin.problemMarkerId
    val file = textEditor.getEditorInput().asInstanceOf[IFileEditorInput].getFile()
    val marker = file.createMarker(markerType)
    marker.setAttribute(IMarker.CHAR_START, problem.getSourceStart());
    marker.setAttribute(IMarker.CHAR_END, problem.getSourceEnd() + 1);
    marker.setAttribute(IMarker.LINE_NUMBER, problem.getSourceLineNumber());
    //marker.setAttribute(IMarker.LOCATION, "#" + problem.getSourceLineNumber());
    marker.setAttribute(IMarker.MESSAGE, problem.getMessage());
    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
    //    marker.setAttribute(IMarker.SOURCE_ID, JavaBuilder.SOURCE_ID);
     */
    templateUnit.reportBuildError(problem.getMessage(), problem.getSourceStart(), problem.getSourceEnd(), problem.getSourceLineNumber())
  }

  private def updateErrorAnnotations(errors: List[IProblem]) {
    def position(p: IProblem) = new Position(p.getSourceStart, p.getSourceEnd - p.getSourceStart + 1)

    annotationModel.connect(document)
    previousAnnotations.foreach(annotationModel.removeAnnotation _)
    templateUnit.clearBuildErrors

    for (e <- errors) {
      createMarkerAnnotation(e)
      val annotation = new ProblemAnnotation(e, null) {
        setQuickFixable(true)
      }
      annotationModel.addAnnotation(annotation, position(e))

      previousAnnotations ::= annotation
    }
    annotationModel.disconnect(document)
  }

  /**
   * Ask the underlying unit to reload on each document change event.
   *
   *  This is certainly wasteful, but otherwise the AST trees are not up to date
   *  in the interval between the last keystroke and reconciliation (which has a delay of
   *  500ms usually). The user can be quick and ask for completions in this interval, and get
   *  wrong results.
   */
  private object reloader extends IDocumentListener {
    def documentChanged(event: DocumentEvent) {
//      templateUnit.askReload() //FIXME
      textEditor.getViewer.invalidateTextPresentation()
    }

    def documentAboutToBeChanged(event: DocumentEvent) {}

  }
}