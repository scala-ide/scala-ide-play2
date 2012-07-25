package org.scalaide.play2.routeeditor.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class CopyOfRouteActionRule implements IPredicateRule {
	enum ActionReadingState {
		Middle, ParenthesisReached, Found, NotFound
	}

	private ActionReadingState state;
	private IToken packageToken;
	private IToken fToken;

	public CopyOfRouteActionRule(IToken packageToken) {
		this.packageToken = packageToken;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		state = ActionReadingState.NotFound;
		int r = scanner.read();
		char c = (char) r;
		int rCount = 1;
		if (r != ICharacterScanner.EOF && Character.isJavaIdentifierStart(c)) {
			state = ActionReadingState.Middle;
			while (true) {
				r = scanner.read();
				rCount++;
				c = (char) r;
				if (r == ICharacterScanner.EOF) {
					break;
				}
				switch (state) {
				case Middle:
					if (c == '(')
						state = ActionReadingState.ParenthesisReached;
					else if (c == ')')
						state = ActionReadingState.NotFound;
					// else if (c == '\r' || c == '\n')
					else if (Character.isWhitespace(c))
						state = ActionReadingState.NotFound;
					break;
				case ParenthesisReached:
					if (c == ')')
						state = ActionReadingState.Found;
					else if (c == '\r' || c == '\n')
						state = ActionReadingState.Found;
					break;
				}
				if (state == ActionReadingState.NotFound
						|| state == ActionReadingState.Found) {
					break;
				}
			}
		}
		if (state == ActionReadingState.NotFound) {
			for (int i = 0; i < rCount; i++) {
				scanner.unread();
			}
			fToken = Token.UNDEFINED;
			return Token.UNDEFINED;
		} else {
			// scanner.unread();
			fToken = packageToken;
			return packageToken;
		}

	}

	@Override
	public IToken getSuccessToken() {
		return fToken;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		return evaluate(scanner);
	}

}
