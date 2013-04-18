package org.scalaide.play2.routeeditor

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

  override def handlePreferenceStoreChanged(event: PropertyChangeEvent) = {
    sourceViewConfiguration.handlePropertyChangeEvent(event)
    super.handlePreferenceStoreChanged(event)
  }

  override def affectsTextPresentation(event: PropertyChangeEvent): Boolean = {
    // TODO: more precise filtering
    true
  }

  def getViewer: ISourceViewer = getSourceViewer
}