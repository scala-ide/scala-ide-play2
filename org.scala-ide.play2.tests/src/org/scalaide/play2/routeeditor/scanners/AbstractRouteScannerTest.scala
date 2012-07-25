package org.scalaide.play2.routeeditor.scanners

import org.junit.Test
import org.junit.Assert._
import org.scalaide.play2.routeeditor.ColorManager
import org.eclipse.jdt.internal.core.util.SimpleDocument
import org.scalaide.play2.routeeditor.RouteColorConstants
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.rules.Token
import org.eclipse.jface.text.rules.IToken

abstract class AbstractRouteScannerTest(constructor: ColorManager => AbstractRouteScanner) {
  protected val manager = ColorManager.colorManager
  protected val scanner = constructor(manager);
  protected val defaultToken = scanner.getDefaultReturnToken
  protected val wsToken = Token.WHITESPACE
  protected val eofToken = Token.EOF

  protected def check(expected: IToken) = {
    assertEquals(expected, scanner.nextToken())
  }

}