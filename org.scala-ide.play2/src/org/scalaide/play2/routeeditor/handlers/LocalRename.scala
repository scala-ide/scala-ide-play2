package org.scalaide.play2.routeeditor.handlers

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.ui.handlers.HandlerUtil
import org.scalaide.play2.routeeditor.RouteEditor
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.text.ITextSelection

class LocalRename extends AbstractHandler {

  override def execute(event: ExecutionEvent): AnyRef = {
    for {
      editor <- getRouteEditor(event)
      selection <- getTextSelection(event)
    } {
      matchingURIRegions(editor.getViewer.getDocument(), selection)

    }

    // always return null, as speced
    null
  }

  private def getRouteEditor(event: ExecutionEvent): Option[RouteEditor] =
    HandlerUtil.getActiveEditor(event) match {
      case editor: RouteEditor =>
        Some(editor)
      case _ =>
        None
    }

  private def getTextSelection(event: ExecutionEvent): Option[ITextSelection] =
    HandlerUtil.getCurrentSelection(event) match {
      case selection: ITextSelection =>
        Some(selection)
      case _ =>
        None
    }

  def matchingURIRegions(document: IDocument, selection: ITextSelection): List[(Int, Int)] = {
    val selectionStart = selection.getOffset()
    val selectionEnd = selectionStart + selection.getLength()
    println(document)
    println(selection)

    Nil
  }

}

