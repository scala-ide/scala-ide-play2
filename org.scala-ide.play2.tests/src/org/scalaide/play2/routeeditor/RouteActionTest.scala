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
import org.scalaide.play2.routeeditor.completion.CompletionComputerTest

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

class RouteActionTest extends CompletionComputerTest {
  
  override protected def createCompletionComputer = ???

  @Test
  def emptyParametersAction() {

    test("GET /path controller.Applic@ation.method()", "controller.Application", "method", Nil)

  }

  @Test
  def noParameterAction() {

    test("GET /path controller.Applica@tion.method", "controller.Application", "method", Nil)

  }

  @Test
  def actionInt() {
    test("GET /path controller.Applicat@ion.method(a: Int)", "controller.Application", "method", List(("a", "Int")))
  }

  @Test
  def actionString() {
    test("GET /path controller.Application.me@thod(b)", "controller.Application", "method", List(("b", "String")))
  }

  @Test
  def actionIntString() {
    test("GET /path controller.App@lication.method(c: Int, d)", "controller.Application", "method", List(("c", "Int"), ("d", "String")))
  }

  @Test
  def actionStringType() {
    test("GET /path controller.Application.method(e,@ f: some.Type)", "controller.Application", "method", List(("e", "String"), ("f", "some.Type")))
  }

  @Test
  def discardDefaultValue() {
    test("""GET /path controller.Application.me@thod(g ?= "gg", h: Int ?= 3)""", "controller.Application", "method", List(("g", "String"), ("h", "Int")))
  }

  private def test(
    documentContent: String,
    expectedTypeName: String,
    expectedMethodName: String,
    expectedParams: List[(String, String)]) {

    val file = new RouteFile(documentContent)

    val expected = new RouteAction(expectedTypeName, expectedMethodName, expectedParams, file.document.getPartition(file.caretOffset))

    val actual = RouteAction.routeActionAt(file.document, file.caretOffset)

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
