package org.scalaide.play2.routeeditor.rules

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

class RouteActionRule(packageToken: IToken) extends IPredicateRule {
  sealed class ActionReadingState(i: Int)
  case class Middle extends ActionReadingState(0)
  case class ParenthesisReached extends ActionReadingState(1)
  case class Found extends ActionReadingState(2)
  case class NotFound extends ActionReadingState(3)

  private var fToken: IToken = _;

  override def evaluate(scanner: ICharacterScanner) = {
    var state: ActionReadingState = NotFound();
    var r = scanner.read();
    var c = r.asInstanceOf[Char];
    var rCount = 1;
    if (r != ICharacterScanner.EOF && Character.isJavaIdentifierStart(c)) {
      state = Middle();
      while (!(state.isInstanceOf[Found] || state.isInstanceOf[NotFound])) {
        r = scanner.read()
        rCount += 1
        c = r.asInstanceOf[Char]
        if (r == ICharacterScanner.EOF) {
          state = Found()
        }
        state match {
          case Middle() =>
            if (c == '(')
              state = ParenthesisReached()
            else if (c == ')')
              state = NotFound()
            else if (Character.isWhitespace(c))
              state = NotFound()
          case ParenthesisReached() =>
            if (c == ')')
              state = Found()
            else if (c == '\r' || c == '\n')
              state = Found()
          case _ =>
        }
      }
    }
    state match {
      case NotFound() => {
        for (i <- 0 until rCount) {
          scanner.unread()
        }
        fToken = Token.UNDEFINED
      }
      case _ => {
        fToken = packageToken
      }
    }
    fToken
  }

  override def getSuccessToken() = {
    fToken;
  }

  override def evaluate(scanner: ICharacterScanner, resume: Boolean) = {
    evaluate(scanner)
  }

}