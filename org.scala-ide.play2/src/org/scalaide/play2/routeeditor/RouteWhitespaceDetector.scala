package org.scalaide.play2.routeeditor

import org.eclipse.jface.text.rules.IWhitespaceDetector

class RouteWhitespaceDetector extends IWhitespaceDetector {
  def isWhitespace(c: Char) = {
    (c == ' ' || c == '\t' || c == '\n' || c == '\r')
  }
}