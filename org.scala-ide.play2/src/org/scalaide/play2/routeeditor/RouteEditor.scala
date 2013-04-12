package org.scalaide.play2.routeeditor

import scala.tools.eclipse.util.SWTUtils
import scala.tools.eclipse.util.SWTUtils.fnToPropertyChangeListener
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.ui.editors.text.TextEditor
import org.scalaide.play2.PlayPlugin
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.texteditor.TextOperationAction
import org.eclipse.jface.text.source.ISourceViewer

class RouteEditor extends TextEditor {
  lazy val preferenceStore = new ChainedPreferenceStore(Array(PlayPlugin.preferenceStore, EditorsUI.getPreferenceStore))
  this.setPreferenceStore(preferenceStore)
  val sourceViewConfiguration = new RouteConfiguration(preferenceStore, this)
  setSourceViewerConfiguration(sourceViewConfiguration);
  setDocumentProvider(new RouteDocumentProvider());

  override def dispose() = {
    super.dispose();
    PlayPlugin.preferenceStore.removePropertyChangeListener(preferenceListener)
  }
  
  /**
   * It is necessary for binding ctrl+shift+f to formatting
   */
  override def initializeKeyBindingScopes() {
    setKeyBindingScopes(Array("org.scala-ide.play2.routeeditor.editorScope"))
  }

  
  override def createActions() {
    super.createActions()

    // Adding source formatting action in the editor popup dialog 
    val formatAction = new TextOperationAction(EditorMessages.resourceBundle, "Editor.Format.", this, ISourceViewer.FORMAT)
    formatAction.setActionDefinitionId("org.scala-ide.play2.routeeditor.commands.format")
    setAction("format", formatAction)

  }

  private val preferenceListener: IPropertyChangeListener = handlePreferenceStoreChanged _

  override def handlePreferenceStoreChanged(event: PropertyChangeEvent) = {
    sourceViewConfiguration.handlePropertyChangeEvent(event)
    getSourceViewer().invalidateTextPresentation
  }
  
  def getViewer: ISourceViewer = getSourceViewer

  PlayPlugin.preferenceStore.addPropertyChangeListener(preferenceListener)
}