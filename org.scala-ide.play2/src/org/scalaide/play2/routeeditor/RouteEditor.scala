package org.scalaide.play2.routeeditor

import scala.tools.eclipse.util.SWTUtils
import scala.tools.eclipse.util.SWTUtils.fnToPropertyChangeListener
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.ui.editors.text.TextEditor
import org.scalaide.play2.PlayPlugin
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.ui.editors.text.EditorsUI

class RouteEditor extends TextEditor {
  lazy val preferenceStore = new ChainedPreferenceStore(Array(PlayPlugin.prefStore, EditorsUI.getPreferenceStore))
  this.setPreferenceStore(preferenceStore)
  val sourceViewConfiguration = new RouteConfiguration(preferenceStore, this)
  setSourceViewerConfiguration(sourceViewConfiguration);
  setDocumentProvider(new RouteDocumentProvider());

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
}