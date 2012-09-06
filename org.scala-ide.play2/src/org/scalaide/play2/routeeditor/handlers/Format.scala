package org.scalaide.play2.routeeditor.handlers

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.jface.text.ITextOperationTarget
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.ui.handlers.HandlerUtil
import org.scalaide.play2.routeeditor.RouteEditor

/**
 * Invoke the formatter on a Route editor
 */
class Format extends AbstractHandler {
  override def execute(event: ExecutionEvent): AnyRef = {
    HandlerUtil.getActiveEditor(event) match {
      case editor: RouteEditor =>
        // in our case, the viewer is always an ITexOperationTarget
        editor.getViewer.asInstanceOf[ITextOperationTarget].doOperation(ISourceViewer.FORMAT)
    }
    null
  }
}