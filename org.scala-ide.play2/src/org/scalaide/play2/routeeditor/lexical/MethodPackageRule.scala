package org.scalaide.play2.routeeditor.lexical

import org.eclipse.jface.text.rules.ICharacterScanner
import org.eclipse.jface.text.rules.IRule
import org.eclipse.jface.text.rules.IToken
import org.eclipse.jface.text.rules.Token
/**
 * A rule for identifying a method name or package name 
 */
class MethodPackageRule(packageToken: IToken, methodToken: IToken) extends IRule {
  sealed class PackageReadingState(i: Int)
  case object Middle extends PackageReadingState(0)
  case object NotFound extends PackageReadingState(2)
  case object PackageFound extends PackageReadingState(3)
  case object MethodFound extends PackageReadingState(4)

  override def evaluate(scanner: ICharacterScanner) = {
    var state: PackageReadingState = NotFound
    var r = scanner.read()
    var c = r.asInstanceOf[Char]
    var rCount = 1
    if (r != ICharacterScanner.EOF && Character.isLowerCase(c)) {
      state = Middle
      while (state == Middle) {
        r = scanner.read();
        rCount += 1;
        c = r.asInstanceOf[Char];
        if (r == ICharacterScanner.EOF) {
          state = MethodFound
        }
        if (c == '.')
          state = PackageFound // `.' IS inside package token
        else if (c == '(') {
          state = MethodFound
          scanner.unread() // `(' is NOT inside method token
        }
      }
    }
    state match {
      case NotFound =>
        for (i <- 0 until rCount) {
          scanner.unread()
        }
        Token.UNDEFINED
      case PackageFound =>
        packageToken
      case MethodFound =>
        methodToken
      case _ =>
        Token.UNDEFINED // impossible to reach!
    }
  }

}
