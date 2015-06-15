package org.scalaide.play2.templateeditor

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.DocumentCommand
import org.eclipse.jface.text.IAutoEditStrategy
import org.eclipse.jface.text.IDocument

/**
 * Copied from scala-ide.core since the class has been removed.
 */
class BracketAutoEditStrategy(prefStore: IPreferenceStore) extends IAutoEditStrategy {

  def customizeDocumentCommand(document: IDocument, command: DocumentCommand) = {
    def ch(i: Int, c: Char) = {
      val o = command.offset + i
      o >= 0 && o < document.getLength && document.getChar(o) == c
    }

    def jumpOverClosingBrace() = {
      if (ch(0, '}')) {
        command.text = ""
        command.caretOffset = command.offset + 1
      }
    }

    def removeClosingBrace() = {
      if (ch(0, '{') && ch(1, '}')) {
        command.length = 2
      }
    }

    command.text match {
      case "}" => jumpOverClosingBrace()
      case ""  => removeClosingBrace()
      case _   =>
    }
  }
}
