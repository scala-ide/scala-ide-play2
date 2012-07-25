package org.scalaide.play2.routeeditor.rules

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;

class HTTPKeywordRule(defaultToken: IToken, httpToken: IToken) extends WordRule(new KeywordDetector(), defaultToken) {
  {
    addWord("GET", httpToken);
    addWord("POST", httpToken);
    addWord("PUT", httpToken);
    addWord("DELETE", httpToken);
    addWord("HEAD", httpToken);
  }

}

class KeywordDetector extends IWordDetector {
  def isWordStart(c: Char) = {
    Character.isLetter(c);
  }

  def isWordPart(c: Char) = {
    Character.isLetter(c);
  }
}