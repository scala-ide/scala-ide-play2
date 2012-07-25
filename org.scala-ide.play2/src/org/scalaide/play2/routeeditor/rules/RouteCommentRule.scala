package org.scalaide.play2.routeeditor.rules

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;

class RouteCommentRule(token: IToken) extends EndlessWordPatternRule(new CommentDetector(), "#", token)

class CommentDetector extends IWordDetector {
  override def isWordStart(c: Char) = {
    c == '#'
  }
  override def isWordPart(c: Char) = {
    !(c == '\r' || c == '\n')
  }
}
