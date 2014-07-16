package org.scalaide.play2.routeeditor.lexical

import org.eclipse.jface.text.Document
import org.junit.Test

class RouteURIScannerTest extends AbstractRouteScannerTest {
  override val scanner = new RouteURIScanner(prefStore)

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