package org.scalaide.play2.templateeditor.lexical

import scala.tools.eclipse.lexical.XmlTagScanner
import org.eclipse.jdt.ui.text.IColorManager
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.rules.IToken
import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClasses._

class HtmlTagScanner(_colorManager: IColorManager, _preferenceStore: IPreferenceStore) extends XmlTagScanner(_colorManager, _preferenceStore) {
  import XmlTagScanner._

  var start: Int = -1

  override def setRange(document: IDocument, offset: Int, length: Int) {
    super.setRange(document, offset, length)
    this.start = offset
  }

  private def ch = if (pos > end) EOF else document.getChar(pos)

  override def nextToken(): IToken = {
    if (pos == start && pos != -1) {
      ch match {
        case '\'' | '\"' => {
          tokenOffset = start
          tokenLength = 1
          pos += 1
          return getToken(XML_ATTRIBUTE_VALUE)
        }
        case _ =>
      }
    }
    super.nextToken
  }
}