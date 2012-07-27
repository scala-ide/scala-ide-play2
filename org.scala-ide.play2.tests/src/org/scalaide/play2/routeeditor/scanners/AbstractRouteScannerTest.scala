package org.scalaide.play2.routeeditor.scanners

import org.eclipse.jdt.ui.text.IColorManager
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.rules.IToken
import org.eclipse.jface.text.rules.Token
import org.junit.Assert.assertEquals
import org.scalaide.play2.PlayPlugin

abstract class AbstractRouteScannerTest(constructor: (IPreferenceStore, IColorManager) => AbstractRouteScanner) {
//  protected val colorManager = new JavaColorManager()
  protected val colorManager = null
  protected val prefStore = PlayPlugin.prefStore
  protected val scanner = constructor(prefStore, colorManager);
  protected val defaultToken = scanner.getDefaultReturnToken
  protected val wsToken = Token.WHITESPACE
  protected val eofToken = Token.EOF

  protected def check(expected: IToken) = {
    assertEquals(expected, scanner.nextToken())
  }

}