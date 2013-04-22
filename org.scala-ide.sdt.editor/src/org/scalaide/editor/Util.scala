package org.scalaide.editor

import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.core.runtime.Platform
import org.eclipse.jface.text.IDocument
import org.eclipse.ui.texteditor.ITextEditor

object Util {
  /** The default line separator */
  def defaultLineSeparator: String = {
    val ls = Option(EditorsUI.getPreferenceStore()).map(_.getString(Platform.PREF_LINE_SEPARATOR))
    ls match {
      case Some("") | None => System.getProperty("line.separator")
      case Some(s)         => s
    }
  }

  /** Return the document associated with the given editor, if possible. */
  def getEditorDocument(editor: ITextEditor): Option[IDocument] =
    Option(editor.getDocumentProvider()).map(_.getDocument(editor.getEditorInput()))
}