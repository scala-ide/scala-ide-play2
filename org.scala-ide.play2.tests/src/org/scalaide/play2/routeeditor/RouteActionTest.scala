package org.scalaide.play2.routeeditor

import org.junit.Test
import org.junit.Assert._
import org.eclipse.jdt.internal.core.util.SimpleDocument
import org.eclipse.jface.text.TypedRegion
import org.scalaide.play2.routeeditor.lexical.RoutePartitions
import org.eclipse.jface.text.ITypedRegion
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Region

object RouteActionTest {

  def actionPartitionOffset(content: String): Int = {
    val regex = """\S*\s*/\S*\s*""".r

    regex.findFirstMatchIn(content) match {
      case Some(prefix) =>
        prefix.end
      case None =>
        fail("Unable to partition the test content")
        0
    }

  }

}

class RouteActionTest {

  @Test
  def emptyParametersAction() {

    test("GET /path controller.Application.method()", 17, "controller.Application", "method", Nil)

  }

  @Test
  def noParameterAction() {

    test("GET /path controller.Application.method", 16, "controller.Application", "method", Nil)

  }

  @Test
  def actionInt() {
    test("GET /path controller.Application.method(a: Int)", 18, "controller.Application", "method", List(("a", "Int")))
  }

  @Test
  def actionString() {
    test("GET /path controller.Application.method(b)", 18, "controller.Application", "method", List(("b", "String")))
  }

  @Test
  def actionIntString() {
    test("GET /path controller.Application.method(c: Int, d)", 18, "controller.Application", "method", List(("c", "Int"), ("d", "String")))
  }

  @Test
  def actionStringType() {
    test("GET /path controller.Application.method(e, f: some.Type)", 18, "controller.Application", "method", List(("e", "String"), ("f", "some.Type")))
  }

  @Test
  def discardDefaultValue() {
    test("""GET /path controller.Application.method(g ?= "gg", h: Int ?= 3)""", 19, "controller.Application", "method", List(("g", "String"), ("h", "Int")))
  }

  private def test(
    documentContent: String,
    hyperlinkOffset: Int,
    expectedTypeName: String,
    expectedMethodName: String,
    expectedParams: List[(String, String)]) {

    val partitionOffset = RouteActionTest.actionPartitionOffset(documentContent)
    val partitionLength = documentContent.length() - partitionOffset

    val document = new TestDocumentWithRoutePartition(documentContent, new TypedRegion(partitionOffset, partitionLength, RoutePartitions.ROUTE_ACTION))

    val expected = new RouteAction(expectedTypeName, expectedMethodName, expectedParams, new Region(partitionOffset, partitionLength))

    val actual = RouteAction.routeActionAt(document, hyperlinkOffset)

    compare(expected, actual)
  }

  private def compare(expected: RouteAction, actual: Option[RouteAction]) {
    actual match {
      case Some(action) =>
        assertEquals("Wrong type", expected.typeName, action.typeName)
        assertEquals("Wrong method", expected.methodName, action.methodName)
        assertEquals("Wrong parameters", expected.params, action.params)
        assertEquals("Wrong region", expected.region, action.region)
      case None =>
        fail("A RouteAction is expected")
    }
  }

}

/** IDocumentation implementation with getPartition() support
 */
class TestDocumentWithRoutePartition(content: String, routePartition: TypedRegion) extends SimpleDocument(content) {

  override def getPartition(offset: Int): ITypedRegion = {
    if (offset >= routePartition.getOffset && offset < routePartition.getOffset + routePartition.getLength()) {
      routePartition
    } else {
      new TypedRegion(0, 0, IDocument.DEFAULT_CONTENT_TYPE)
    }
  }

}
