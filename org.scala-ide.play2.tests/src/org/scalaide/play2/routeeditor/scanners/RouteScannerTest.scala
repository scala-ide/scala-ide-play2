package org.scalaide.play2.routeeditor.scanners

import org.junit.Test
import org.junit.Assert._
import org.scalaide.play2.routeeditor.ColorManager
import org.eclipse.jdt.internal.core.util.SimpleDocument
import org.scalaide.play2.routeeditor.RouteColorConstants
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.rules.Token
import org.eclipse.jface.text.rules.IToken

class RouteScannerTest extends AbstractRouteScannerTest(new RouteScanner(_)) {
  private val httpToken = scanner.asInstanceOf[RouteScanner].httpToken

  @Test
  def keywordTest1() = {
    val content = "GET POST		 DELETE HELLO PUT HEAD"
    val document = new Document(content)
    scanner.setRange(document, 0, content.length)

    check(httpToken)
    check(wsToken)
    check(httpToken)
    check(wsToken)
    check(httpToken)
    check(wsToken)
    check(defaultToken)
    check(wsToken)
    check(httpToken)
    check(wsToken)
    check(httpToken)
  }

  @Test
  def keywordTest2() = {
    val content =
      "GET\tPOST PUT GETME GETTER"
    val document = new Document(content)
    scanner.setRange(document, 0, content.length)

    check(httpToken)
    check(wsToken)
    check(httpToken)
    check(wsToken)
    check(httpToken)
    check(wsToken)
    check(defaultToken)
    check(wsToken)
    check(defaultToken)
  }

}