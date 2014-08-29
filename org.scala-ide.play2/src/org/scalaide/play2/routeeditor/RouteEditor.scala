package org.scalaide.play2.routeeditor

import org.scalaide.ui.internal.editor.ISourceViewerEditor
import org.scalaide.core.IScalaPlugin
import org.scalaide.core.IScalaProject
import org.scalaide.logging.HasLogger
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.core.commands.IExecutionListener
import org.eclipse.core.commands.NotHandledException
import org.eclipse.jface.text.Region
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.ui.commands.ICommandService
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.editors.text.TextEditor
import org.eclipse.ui.handlers.HandlerUtil
import org.eclipse.ui.part.FileEditorInput
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.ui.texteditor.TextOperationAction
import org.scalaide.play2.PlayPlugin

class RouteEditor extends TextEditor with ISourceViewerEditor with HasLogger with HasScalaProject { self =>
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

    // Add execution listener to observe save events and perform route autoformatting if necessary.
    // Theoretically, only one of these needs to be installed (instead of one per RouteEditor instance),
    // however the implementation defines on private members of RouteEditor, making the the RouteEditor
    // the most logical place to this code. Thus, the implementation is specialized per instance.
    val commandService = getSite().getService(classOf[ICommandService]).asInstanceOf[ICommandService]
    commandService.addExecutionListener(new IExecutionListener {
      override def notHandled(commandId: String, exception: NotHandledException) = {}

      override def postExecuteFailure(commandId: String, exception: ExecutionException) = {}

      override def postExecuteSuccess(commandId: String, returnValue: Any) = {}

      override def preExecute(commandid: String, event: ExecutionEvent) = {
        HandlerUtil.getActiveEditor(event) match {
          case routeEditor: RouteEditor if routeEditor eq self => {
            val isSaveAction = commandid == "org.eclipse.ui.file.save"
            val shouldFormatOnSave = PlayPlugin.preferenceStore.getBoolean(PlayPlugin.RouteFormatterFormatOnSaveId)
            if (isSaveAction && shouldFormatOnSave) {
              val document = routeEditor.getSourceViewer.getDocument
              routeEditor.config.getContentFormatter(routeEditor.getSourceViewer).format(document, new Region(0, document.getLength))
            }
          }

          case _ =>
        }
      }
    })
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
  override def getScalaProject: Option[IScalaProject] = {
    getEditorInput() match {
      case fileEditorInput: FileEditorInput =>
        IScalaPlugin().asScalaProject(fileEditorInput.getFile().getProject())
      case _ =>
        logger.info("Attempted to find a Scala project for %s".format(getEditorInput()))
        None
    }
  }
}