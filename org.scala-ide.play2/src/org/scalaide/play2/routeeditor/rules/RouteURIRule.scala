package org.scalaide.play2.routeeditor.rules

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;

class RouteURIRule(token: IToken) extends EndlessWordPatternRule(new URIDetector(), "/", token)

class URIDetector extends IWordDetector {

  override def isWordStart(c: Char) = {
    c == '/'
  }
  override def isWordPart(c: Char) = {
    !Character.isWhitespace(c)
  }

}
