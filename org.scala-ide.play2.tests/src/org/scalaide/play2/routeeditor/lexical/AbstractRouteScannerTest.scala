package org.scalaide.play2.routeeditor.lexical

import org.eclipse.jface.text.rules.IToken
import org.eclipse.jface.text.rules.Token
import org.junit.Assert.assertEquals
import org.scalaide.play2.PlayPlugin

abstract class AbstractRouteScannerTest {
  protected val prefStore = PlayPlugin.preferenceStore
  protected val scanner : AbstractRouteScanner
  protected val defaultToken = scanner.getDefaultReturnToken
  protected val wsToken = Token.WHITESPACE
  protected val eofToken = Token.EOF

  protected def check(expected: IToken) = {
    assertEquals(expected, scanner.nextToken())
  }

}