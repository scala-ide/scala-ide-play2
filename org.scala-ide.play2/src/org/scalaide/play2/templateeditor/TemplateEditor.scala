package org.scalaide.play2.templateeditor

import scala.collection.JavaConverters
import scala.tools.eclipse.ISourceViewerEditor
import scala.tools.eclipse.InteractiveCompilationUnit
import scala.tools.eclipse.ui.InteractiveCompilationUnitEditor
import scala.tools.eclipse.util.SWTUtils.fnToPropertyChangeListener

import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider.ProblemAnnotation
import org.eclipse.jface.text.Position
import org.eclipse.jface.text.source.IAnnotationModel
import org.eclipse.jface.text.source.IAnnotationModelExtension
import org.eclipse.jface.text.source.IAnnotationModelExtension2
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.editors.text.TextEditor
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.scalaide.play2.PlayPlugin

class TemplateEditor extends TextEditor with ISourceViewerEditor with InteractiveCompilationUnitEditor {
  private lazy val preferenceStore = new ChainedPreferenceStore(Array((EditorsUI.getPreferenceStore()), PlayPlugin.prefStore))
  private val sourceViewConfiguration = new TemplateConfiguration(preferenceStore, this)
  private val documentProvider = new TemplateDocumentProvider()
  
  setSourceViewerConfiguration(sourceViewConfiguration);
  setPreferenceStore(preferenceStore)
  setDocumentProvider(documentProvider);

  override def dispose() = {
    super.dispose()
    PlayPlugin.prefStore.removePropertyChangeListener(preferenceListener)
  }

  private val preferenceListener: IPropertyChangeListener = handlePreferenceStoreChanged _

  override def handlePreferenceStoreChanged(event: PropertyChangeEvent) = {
    sourceViewConfiguration.handlePropertyChangeEvent(event)
    getSourceViewer().invalidateTextPresentation
  }

  PlayPlugin.prefStore.addPropertyChangeListener(preferenceListener)

  override def editorSaved() = {
    super.editorSaved()
    sourceViewConfiguration.strategy.reconcile(null)
  }
  
  override def getViewer: ISourceViewer = getSourceViewer
  
  override def getInteractiveCompilationUnit(): InteractiveCompilationUnit = TemplateCompilationUnit.fromEditor(this)

  @volatile
  private var previousAnnotations: List[ProblemAnnotation] = Nil
  
  private type IAnnotationModelExtended = IAnnotationModel with IAnnotationModelExtension with IAnnotationModelExtension2

  /** Return the annotation model associated with the current document. */
  private def annotationModel: IAnnotationModelExtended = getDocumentProvider.getAnnotationModel(getEditorInput).asInstanceOf[IAnnotationModelExtended]

  def updateErrorAnnotations(errors: List[IProblem]) {
    import scala.collection.JavaConverters._

    def position(p: IProblem) = new Position(p.getSourceStart, p.getSourceEnd - p.getSourceStart + 1)

    val newAnnotations = for (e <- errors) yield { (new ProblemAnnotation(e, null), position(e)) }

    annotationModel.replaceAnnotations(previousAnnotations.toArray, newAnnotations.toMap.asJava)
    previousAnnotations = newAnnotations.unzip._1 
  }
}