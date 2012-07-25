package org.scalaide.play2.routeeditor.scanners

import org.junit.Test
import org.junit.Assert._
import org.scalaide.play2.routeeditor.ColorManager
import org.eclipse.jdt.internal.core.util.SimpleDocument
import org.scalaide.play2.routeeditor.RouteColorConstants
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.rules.Token
import org.eclipse.jface.text.rules.IToken

class RouteActionScannerTest extends AbstractRouteScannerTest(new RouteActionScanner(_)) {
  val packageToken = scanner.asInstanceOf[RouteActionScanner].packageToken
  val classToken = scanner.asInstanceOf[RouteActionScanner].classToken
  val methodToken = scanner.asInstanceOf[RouteActionScanner].methodToken

  @Test
  def methodTest1() = {
    val content = "show()"
    val document = new Document(content)
    scanner.setRange(document, 0, content.length)

    check(methodToken)
    check(defaultToken)
    check(Token.EOF)
  }
  @Test
  def methodTest2() = {
    val content = "show(a: Int)"
    val document = new Document(content)
    scanner.setRange(document, 0, content.length)

    check(methodToken)
    check(defaultToken)
    check(Token.EOF)
  }
  @Test
  def methodTest3() = {
    val content = "show(a ?= \"\")"
    val document = new Document(content)
    scanner.setRange(document, 0, content.length)

    check(methodToken)
    check(defaultToken)
    check(Token.EOF)
  }
  @Test
  def classMethodTest1() = {
    val content = "A.show(a ?= \"\")"
    val document = new Document(content)
    scanner.setRange(document, 0, content.length)

    check(classToken)
    check(defaultToken)
    check(methodToken)
    check(defaultToken)
    check(Token.EOF)
  }

  @Test
  def classMethodTest2() = {
    val content = "A.B.C.D_1.show(a ?= \"\")"
    val document = new Document(content)
    scanner.setRange(document, 0, content.length)

    check(classToken)
    check(defaultToken)
    check(classToken)
    check(defaultToken)
    check(classToken)
    check(defaultToken)
    check(classToken)
    check(defaultToken)
    check(methodToken)
    check(defaultToken)
    check(Token.EOF)
  }

  @Test
  def packageClassMethodTest1() = {
    val content = "test.Class.show()"
    val document = new Document(content)
    scanner.setRange(document, 0, content.length)

    check(packageToken)
    check(classToken)
    check(defaultToken)
    check(methodToken)
    check(defaultToken)
    check(Token.EOF)
  }
  @Test
  def packageClassMethodTest2() = {
	  val content = "test.test1.Class.show()"
			  val document = new Document(content)
	  scanner.setRange(document, 0, content.length)
	  
	  check(packageToken)
	  check(classToken)
	  check(defaultToken)
	  check(methodToken)
	  check(defaultToken)
	  check(Token.EOF)
  }
  @Test
  def packageClassMethodTest3() = {
	  val content = "test.test1.t_2.t__3.tAt.Class.show()"
			  val document = new Document(content)
	  scanner.setRange(document, 0, content.length)
	  
	  check(packageToken)
	  check(classToken)
	  check(defaultToken)
	  check(methodToken)
	  check(defaultToken)
	  check(Token.EOF)
  }
}