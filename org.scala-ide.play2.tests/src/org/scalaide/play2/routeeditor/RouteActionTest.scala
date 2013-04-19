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

class RouteActionTest {

  @Test
  def emptyParametersAction() {

    test("GET /path controller.Application.method()", 10, 31, 17, "controller.Application", "method", Nil)

  }

  @Test
  def noParameterAction() {

    test("GET /path controller.Application.method", 10, 29, 16, "controller.Application", "method", Nil)

  }
  
  @Test
  def actionInt() {
    test("GET /path controller.Application.method(a: Int)",10, 37, 18, "controller.Application", "method", List("Int") )
  }

  @Test
  def actionString() {
    test("GET /path controller.Application.method(b)",10, 32, 18, "controller.Application", "method", List("String") )
  }

  @Test
  def actionIntString() {
    test("GET /path controller.Application.method(c: Int, d)",10, 40, 18, "controller.Application", "method", List("Int", "String") )
  }
  
  @Test
  def actionStringType() {
    test("GET /path controller.Application.method(e, f: some.Type)",10, 46, 18, "controller.Application", "method", List("String", "some.Type") )
  }

  private def test(
      documentContent: String,
      partitionOffset: Int,
      partitionLength: Int,
      hyperlinkOffset: Int,
      expectedTypeName: String,
      expectedMethodName: String,
      expectedParameterTypes: List[String]) {

    val document = new TestDocumentWithRoutePartition(documentContent, new TypedRegion(partitionOffset, partitionLength, RoutePartitions.ROUTE_ACTION))
    
    val expected = Some(RouteAction(expectedTypeName, expectedMethodName, expectedParameterTypes, new Region(partitionOffset, partitionLength)))

    val action = RouteAction.routeActionAt(document, hyperlinkOffset)

    assertEquals("Wrong action", expected, action)

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
