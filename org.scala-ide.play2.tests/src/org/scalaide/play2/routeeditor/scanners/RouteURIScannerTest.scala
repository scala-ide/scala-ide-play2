package org.scalaide.play2.routeeditor.scanners

import org.junit.Test
import org.junit.Assert._
import org.scalaide.play2.routeeditor.ColorManager
import org.eclipse.jdt.internal.core.util.SimpleDocument
import org.scalaide.play2.routeeditor.RouteColorConstants
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.rules.Token
import org.eclipse.jface.text.rules.IToken

class RouteURIScannerTest extends AbstractRouteScannerTest(new RouteURIScanner(_)){
  private val dynamicToken = scanner.asInstanceOf[RouteURIScanner].dynamic

  @Test
  def dynamicTest1() = {
    val content = "/hello/route/*id"
    val document = new Document(content)
    scanner.setRange(document, 0, content.length)

    check(defaultToken)
    check(dynamicToken)
    check(eofToken)
  }

  @Test
  def dynamicTest2() = {
    val content = "/hello/:id"
    val document = new Document(content)
    scanner.setRange(document, 0, content.length)

    check(defaultToken)
    check(dynamicToken)
    check(eofToken)
  }

  @Test
  def dynamicTest3() = {
    val content = "/hello/route_1/$id_2"
    val document = new Document(content)
    scanner.setRange(document, 0, content.length)

    check(defaultToken)
    check(dynamicToken)
    check(eofToken)
  }

}