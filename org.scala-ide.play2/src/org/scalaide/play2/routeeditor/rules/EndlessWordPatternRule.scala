package org.scalaide.play2.routeeditor.rules

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordPatternRule;

class EndlessWordPatternRule(detector: IWordDetector, startSequence: String,
  token: IToken) extends WordPatternRule(detector, startSequence, null, token) {
  val fBuffer = new StringBuffer();

  protected override def endSequenceDetected(scanner: ICharacterScanner): Boolean = {
    fBuffer.setLength(0);
    var c = scanner.read();
    while (c != ICharacterScanner.EOF && fDetector.isWordPart(c.asInstanceOf[Char])) {
      fBuffer.append(c.asInstanceOf[Char]);
      c = scanner.read();
    }
    scanner.unread();

    if (fBuffer.length() >= fEndSequence.length) {
      var i = fEndSequence.length - 1
      var j = fBuffer.length() - 1
      while (i >= 0) {
        if (fEndSequence(i) != fBuffer.charAt(j)) {
          unreadBuffer(scanner);
          return false;
        }
        i -= 1
        j -= 1
      }
      return true;
    }

    unreadBuffer(scanner);
    return false;
  }

}