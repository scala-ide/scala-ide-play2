package org.scalaide.play2.routeeditor

import scala.tools.eclipse.ScalaPlugin
import scala.tools.eclipse.ScalaProject
import scala.tools.eclipse.logging.HasLogger
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.editors.text.TextEditor
import org.eclipse.ui.part.FileEditorInput
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.ui.texteditor.TextOperationAction
import org.scalaide.play2.PlayPlugin
import scala.tools.eclipse.ISourceViewerEditor

class RouteEditor extends TextEditor with ISourceViewerEditor with HasLogger with HasScalaProject {
  private lazy val preferenceStore = new ChainedPreferenceStore(Array(PlayPlugin.preferenceStore, EditorsUI.getPreferenceStore))
  private val config = new RouteConfiguration(preferenceStore, this)

  this.setPreferenceStore(preferenceStore)
  setSourceViewerConfiguration(config)
  setDocumentProvider(new RouteDocumentProvider())

  /** It is necessary for binding ctrl+shift+f to formatting */
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
    config.handlePropertyChangeEvent(event)
    super.handlePreferenceStoreChanged(event)
  }

  override def affectsTextPresentation(event: PropertyChangeEvent): Boolean = {
    // TODO: more precise filtering
    true
  }

  override def getViewer: ISourceViewer = getSourceViewer
  
  /** Returns project containing the edited file, if it exists. */
  override def getScalaProject: Option[ScalaProject] ={
    getEditorInput() match {
      case fileEditorInput: FileEditorInput =>
        ScalaPlugin.plugin.asScalaProject(fileEditorInput.getFile().getProject())
      case _ =>
        logger.info("Attempted to find a Scala project for %s".format(getEditorInput()))
        None
    }
  }
}