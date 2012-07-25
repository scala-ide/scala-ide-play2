package org.scalaide.play2.routeeditor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlink;
import org.eclipse.jdt.ui.actions.OpenAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.scalaide.play2.routeeditor.scanners.RoutePartitionScanner;
import org.scalaide.play2.routeeditor.tools.MethodFinder;

public class CopyOfRouteHyperlinkDetector implements IHyperlinkDetector {
	private RouteEditor routeEditor;

	public CopyOfRouteHyperlinkDetector(RouteEditor routeEditor) {
		this.routeEditor = routeEditor;
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		try {
			if (RoutePartitionScanner.isRouteAction(textViewer.getDocument()
					.getContentType(region.getOffset()))) {
				Region wordRegion = findWord(textViewer.getDocument().get(),
						region.getOffset());
				if (!isInMethodNamePart(textViewer.getDocument().get(),
						wordRegion))
					return null;
				// IHyperlink hyperLink = new URLHyperlink(r,
				// "http://www.typesafe.com/") {
				// @Override
				// public String getHyperlinkText() {
				// return "TYPESAFE!";
				// }
				// };
				// return new IHyperlink[] { hyperLink };
				int offset = wordRegion.getOffset();
				int length = wordRegion.getLength();
				String methodName = textViewer.getDocument()
						.get(offset, length);
				String[] parameterTypes = findParameterTypes(textViewer
						.getDocument().get(), offset + length);
				IJavaElement[] elems = MethodFinder.searchMethod(methodName,
						parameterTypes);
				int numberOfElements = elems.length;
				if (numberOfElements > 0) {
					IHyperlink[] hyperLinks = new IHyperlink[numberOfElements];
					OpenAction openAction = new OpenAction(
							routeEditor.getEditorSite());
					for (int i = 0; i < elems.length; i++) {
						hyperLinks[i] = new JavaElementHyperlink(wordRegion,
								openAction, elems[i], numberOfElements > 1);
					}
					return hyperLinks;
				}
				return null;
			} else {
				return null;
			}
		} catch (BadLocationException e) {
			return null;
		}
	}

	private boolean isIdentifierPart(char c) {
		return (c == '.') || Character.isUnicodeIdentifierPart(c);
	}

	private boolean isInMethodNamePart(String document, Region region) {
		int startIndex = region.getOffset() + region.getLength();
		try {
			if (document.charAt(region.getOffset()) == '(')
				return false;
			if (document.substring(startIndex).trim().charAt(0) == '(')
				return true;
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public String[] findParameterTypes(String document, int endOfMethodNameIndex) {
		int startIndex = document.indexOf("(", endOfMethodNameIndex);
		int endIndex = document.indexOf(")", endOfMethodNameIndex);
		if (startIndex + 1 == endIndex) {
			return new String[] {};
		}
		String parametersString = document.substring(startIndex + 1, endIndex);
		String[] paramStringArray = parametersString.split(",");
		int numberOfParameters = paramStringArray.length;
		String[] paramTypes = new String[numberOfParameters];
		for (int i = 0; i < numberOfParameters; i++) {
			paramTypes[i] = inferParameterType(paramStringArray[i]);
		}
		return paramTypes;
	}

	public String inferParameterType(String parameterString) {
		parameterString = parameterString.trim();
		int startIndex = parameterString.indexOf(":");
		if (startIndex == -1) {
			return "String";
		}
		String typeString = parameterString.substring(startIndex + 1).trim();
		Region r = findWord(typeString, 0);
		return typeString.substring(r.getOffset(),
				r.getOffset() + r.getLength());
	}

	public Region findWord(String string, int offset) {
		char[] document = string.toCharArray();
		int start = -2;
		int end = -1;

		int pos = offset;

		while (pos >= 0 && isIdentifierPart(document[pos]))
			pos -= 1;

		start = pos;

		pos = offset;
		int len = document.length;
		while (pos < len && isIdentifierPart(document[pos]))
			pos += 1;

		end = pos;

		if (start >= -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		} else
			return null;
	}
}
