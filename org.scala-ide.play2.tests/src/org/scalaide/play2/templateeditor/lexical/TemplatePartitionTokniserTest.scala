package org.scalaide.play2.templateeditor.lexical

import org.junit.Test
import org.junit.Assert._
import scala.tools.eclipse.testsetup.TestProjectSetup
import org.eclipse.jdt.core.IJavaElement
import TemplatePartitionTokeniser._
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.Signature
import org.eclipse.jface.text.TypedRegion
import org.eclipse.jface.text.TypedRegion
import org.junit.AfterClass
import scala.tools.eclipse.testsetup.SDTTestUtils
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.Document
import org.scalaide.editor.util.RegionHelper._
import scala.annotation.tailrec

class TemplatePartitionTokeniserTest {

  def s(offset: Int, length: Int) = new TypedRegion(offset, length, TemplatePartitions.TEMPLATE_SCALA)
  def d(offset: Int, length: Int) = new TypedRegion(offset, length, TemplatePartitions.TEMPLATE_DEFAULT)
  def c(offset: Int, length: Int) = new TypedRegion(offset, length, TemplatePartitions.TEMPLATE_COMMENT)
  def tg(offset: Int, length: Int) = new TypedRegion(offset, length, TemplatePartitions.TEMPLATE_TAG)
  def p(offset: Int, length: Int) = new TypedRegion(offset, length, TemplatePartitions.TEMPLATE_PLAIN)

  @Test
  def definitionSection() {
    val p1 = "@(param: String)"
    testForTokenise(List(d(0, 1), s(1, p1.length() - 1)), p1)
  }
  
  @Test
  def simpleTokeniseTest() = {
    def t = testForTokenise _
    val p2 = "@*comment*@"
    t(List(c(0, p2.length())), p2)
    val p3 = "<html_tag></html_tag>"
    t(List(tg(0, 10), tg(10, 11)), p3)
    val p4 = "Some plain text"
    t(List(p(0, p4.length())), p4)
    val p5 = "@methodCall(arg)"
    t(List(d(0, 1), s(1, p5.length() - 1)), p5)
    val p6 = """@{val blockOfScalaCode = ""}"""
    t(List(d(0, 1), s(1, p6.length() - 1)), p6)
  }
  
  
  
  @Test
  def complexTokeniseTest() = {
    def t = testForTokenise _
    // bug #20
    // reusable block
    val p1 = """@test = {
  is viewed
}"""
    t(List(d(0, 1), s(1, 4), d(5, 4), p(9, 3), p(12, 10), d(22, 1)), p1)
    // bug #20
    // reusable block with scala code
    val p2 = """@test = @{
val x = "x string"
}"""
    t(List(d(0, 1), s(1, 4), d(5, 4), s(9, 22)), p2)
    // bug #18
    // for statements without yield
    val p3 = """@for()"""
    t(List(d(0, 1), s(1, 5)), p3)
  }

  private def testForTokenise(expected: List[TypedRegion], program: String) = {
    val actual = tokenise(new Document(program))

    @tailrec
    def noOverlap(list: List[TypedRegion]): Boolean = {
      list match {
        case head :: second :: tail =>
          !head.overlapsWith(second) && noOverlap(list.tail)
        case _ =>
          true
      }
    }

    def complete: Boolean = {
      def testComplete(start: Int, list: List[TypedRegion]): Boolean = {
        list match {
          case hd :: tail => if (start == hd.getOffset()) testComplete(hd.getOffset() + hd.getLength(), tail) else false
          case _ => start == program.length()
        }
      }
      testComplete(0, actual)
    }

    assertTrue("list has overlap", noOverlap(actual))
    assertTrue("list is not complete", complete)
    assertEquals("list doesn't match", expected, actual)
  }

}