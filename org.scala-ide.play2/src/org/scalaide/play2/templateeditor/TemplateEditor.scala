package org.scalaide.play2.templateeditor

import scala.tools.eclipse.util.SWTUtils
import scala.tools.eclipse.util.SWTUtils.fnToPropertyChangeListener
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.ui.editors.text.TextEditor
import org.scalaide.play2.PlayPlugin

class TemplateEditor extends TextEditor {
  val prefStore = PlayPlugin.prefStore
  this.setPreferenceStore(prefStore)
  val sourceViewConfiguration = new TemplateConfiguration(prefStore, this)
  setSourceViewerConfiguration(sourceViewConfiguration);
  setDocumentProvider(new TemplateDocumentProvider());

  override def dispose() = {
    super.dispose();
    prefStore.removePropertyChangeListener(preferenceListener)
  }

  private val preferenceListener: IPropertyChangeListener = handlePreferenceStoreChanged _

  override def handlePreferenceStoreChanged(event: PropertyChangeEvent) = {
    sourceViewConfiguration.handlePropertyChangeEvent(event)
    getSourceViewer().invalidateTextPresentation
  }

  prefStore.addPropertyChangeListener(preferenceListener)
}