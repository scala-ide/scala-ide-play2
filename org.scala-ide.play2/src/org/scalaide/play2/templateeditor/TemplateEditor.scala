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
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.scalaide.play2.PlayPlugin
import org.scalaide.editor.SourceCodeEditor
import org.scalaide.editor.CompilationUnitProvider
import org.eclipse.jface.preference.IPreferenceStore


class TemplateEditor extends SourceCodeEditor {
  override protected type UnderlyingCompilationUnit = TemplateCompilationUnit

  override val compilationUnitProvider: CompilationUnitProvider[UnderlyingCompilationUnit] = TemplateCompilationUnit

  override protected lazy val preferenceStore: IPreferenceStore = new ChainedPreferenceStore(Array((EditorsUI.getPreferenceStore()), PlayPlugin.prefStore))
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
}

object TemplateEditor {
  /** The annotation types shown when hovering on the left-side ruler (or in the status bar). */
  val annotationsShownInHover = Set(
    "org.eclipse.jdt.ui.error", "org.eclipse.jdt.ui.warning", "org.eclipse.jdt.ui.info")
}