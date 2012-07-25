package org.scalaide.play2.routeeditor.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class CopyOfPackageRule implements IRule {
	enum PackageReadingState {
		Middle, PointReached, Found, NotFound
	}

	private PackageReadingState state;
	private IToken packageToken;

	public CopyOfPackageRule(IToken packageToken) {
		this.packageToken = packageToken;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		state = PackageReadingState.NotFound;
		int r = scanner.read();
		char c = (char) r;
		int rCount = 1;
		if (r != ICharacterScanner.EOF && Character.isLowerCase(c)) {
			state = PackageReadingState.Middle;
			while (true) {
				r = scanner.read();
				rCount++;
				c = (char) r;
				if (r == ICharacterScanner.EOF) {
					break;
				}
				switch (state) {
				case Middle:
					if (c == '.')
						state = PackageReadingState.PointReached;
					else if (c == '(')
						state = PackageReadingState.NotFound;
					break;
				case PointReached:
					if (Character.isLowerCase(c))
						state = PackageReadingState.Middle;
					else if (Character.isUpperCase(c)) {
						state = PackageReadingState.Found;
						scanner.unread();
					}
					break;
				}
				if (state == PackageReadingState.NotFound
						|| state == PackageReadingState.Found) {
					break;
				}
			}
		}
		if (state == PackageReadingState.NotFound) {
			for (int i = 0; i < rCount; i++) {
				scanner.unread();
			}
			return Token.UNDEFINED;
		} else {
			scanner.unread();
			return packageToken;
		}

	}

}
