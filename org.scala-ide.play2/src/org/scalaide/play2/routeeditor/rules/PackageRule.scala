package org.scalaide.play2.routeeditor.rules

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

class PackageRule(packageToken: IToken) extends IRule {
	sealed class PackageReadingState(i: Int)
	case class Middle extends PackageReadingState(0)
	case class PointReached extends PackageReadingState(1)
	case class Found extends PackageReadingState(2)
	case class NotFound extends PackageReadingState(3)

	override def evaluate(scanner: ICharacterScanner) = {
		var state: PackageReadingState = NotFound()
		var r = scanner.read()
		var c = r.asInstanceOf[Char];
		var rCount = 1;
		if (r != ICharacterScanner.EOF && Character.isLowerCase(c)) {
			state = Middle();
			while (!(state.isInstanceOf[NotFound] || 
			    state.isInstanceOf[Found])) {
				r = scanner.read();
				rCount += 1;
				c = r.asInstanceOf[Char];
				if (r == ICharacterScanner.EOF) {
					state = Found()
				}
				state match {
				case Middle() => 
					if (c == '.')
						state = PointReached()
					else if (c == '(')
						state = NotFound()
				case PointReached() =>
					if (Character.isLowerCase(c))
						state = Middle()
					else if (Character.isUpperCase(c)) {
						state = Found()
						scanner.unread()
					}
				case _ =>
				}
			}
		}
		state match {
		  case NotFound() =>
		    for (i <- 0 until rCount){
		      scanner.unread()
		    }
		    Token.UNDEFINED
		  case _ =>
		    packageToken
		}
	}

}
