package org.scalaide.play2.routeeditor.rules

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;

class HTTPKeywordRule(defaultToken: IToken, httpToken: IToken) extends WordRule(new KeywordDetector(), defaultToken) {
  HTTPKeywordRule.words foreach {
    addWord(_, httpToken)
  }

}

object HTTPKeywordRule {
  val words = Array(
      "GET", "POST", "PUT", "DELETE", "HEAD"
      )
}

class KeywordDetector extends IWordDetector {
  def isWordStart(c: Char) = {
    Character.isLetter(c);
  }

  def isWordPart(c: Char) = {
    Character.isLetter(c);
  }
}