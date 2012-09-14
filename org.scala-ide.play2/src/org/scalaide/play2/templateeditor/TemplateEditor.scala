package org.scalaide.play2.templateeditor

import scala.tools.eclipse.util.SWTUtils.fnToPropertyChangeListener
import org.eclipse.jdt.internal.ui.text.java.hover.SourceViewerInformationControl
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.IInformationControlCreator
import org.eclipse.jface.text.source.IOverviewRuler
import org.eclipse.jface.text.source.IVerticalRuler
import org.eclipse.jface.text.source.projection.ProjectionSupport
import org.eclipse.jface.text.source.projection.ProjectionViewer
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Shell
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.editors.text.TextEditor
import org.eclipse.ui.texteditor.AnnotationPreference
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences
import org.scalaide.play2.PlayPlugin
import scala.tools.eclipse.ISourceViewerEditor
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.jdt.internal.ui.JavaPlugin
import scala.tools.eclipse.ui.InteractiveCompilationUnitEditor
import scala.tools.eclipse.InteractiveCompilationUnit

class TemplateEditor extends TextEditor with ISourceViewerEditor with InteractiveCompilationUnitEditor {
  lazy val preferenceStore = new ChainedPreferenceStore(Array((EditorsUI.getPreferenceStore()), PlayPlugin.prefStore))
  val sourceViewConfiguration = new TemplateConfiguration(preferenceStore, this)
  val documentProvider = new TemplateDocumentProvider()
  
  setSourceViewerConfiguration(sourceViewConfiguration);
  setPreferenceStore(preferenceStore)
  setDocumentProvider(documentProvider);

  override def dispose() = {
    super.dispose();
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
  
  def getViewer: ISourceViewer = getSourceViewer
  
  override def getInteractiveCompilationUnit(): Option[InteractiveCompilationUnit] = TemplateCompilationUnit.fromEditor(this)

}