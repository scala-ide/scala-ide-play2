package org.scalaide.play2.routeeditor.scanners

import org.eclipse.jface.text.rules.IRule
import org.eclipse.jface.text.rules.IToken
import org.eclipse.jface.text.rules.IWordDetector
import org.eclipse.jface.text.rules.RuleBasedScanner
import org.eclipse.jface.text.rules.WordRule
import org.scalaide.play2.routeeditor.ColorManager
import org.scalaide.play2.routeeditor.RouteColorConstants
import org.eclipse.jface.text.rules.ITokenScanner
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.rules.Token

class RouteCommentScanner(token: IToken) extends ITokenScanner {
private var offset: Int = _
  private var length: Int = _
  private var consumed = false

  def setRange(document: IDocument, offset: Int, length: Int) {
    this.offset = offset
    this.length = length
    this.consumed = false
  }

  def nextToken(): IToken =
    if (consumed)
      Token.EOF
    else {
      consumed = true
      token
    }

  def getTokenOffset = offset

  def getTokenLength = length
}
