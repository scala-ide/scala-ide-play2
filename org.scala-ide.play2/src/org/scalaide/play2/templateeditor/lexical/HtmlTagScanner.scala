package org.scalaide.play2.templateeditor.lexical

import org.scalaide.core.internal.lexical.XmlTagScanner
import org.eclipse.jdt.ui.text.IColorManager
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.rules.IToken
import org.scalaide.ui.syntax.ScalaSyntaxClasses._

/** A special code scanner for Play templates. It only mark the beginning and end quotes as
 */
class HtmlTagScanner(preferenceStore: IPreferenceStore) extends XmlTagScanner(preferenceStore) {
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