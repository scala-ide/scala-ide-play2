package org.scalaide.play2.templateeditor.lexical

import org.junit.Test
import org.junit.Assert._
import scala.tools.eclipse.testsetup.TestProjectSetup
import org.eclipse.jdt.core.IJavaElement
import TemplatePartitionTokeniser._
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.Signature
import scala.tools.eclipse.lexical.ScalaPartitionRegion
import scala.tools.eclipse.lexical.ScalaPartitionRegion

object TemplatePartitionTokeniserTest extends TestProjectSetup("aProject", bundleName = "org.scala-ide.play2.tests")

class TemplatePartitionTokeniserTest {
  TemplatePartitionTokeniserTest
  def s(start: Int, end: Int) = ScalaPartitionRegion(TemplatePartitions.TEMPLATE_SCALA, start, end)
  def d(start: Int, end: Int) = ScalaPartitionRegion(TemplatePartitions.TEMPLATE_DEFAULT, start, end)
  def c(start: Int, end: Int) = ScalaPartitionRegion(TemplatePartitions.TEMPLATE_COMMENT, start, end)
  def tg(start: Int, end: Int) = ScalaPartitionRegion(TemplatePartitions.TEMPLATE_TAG, start, end)
  def p(start: Int, end: Int) = ScalaPartitionRegion(TemplatePartitions.TEMPLATE_PLAIN, start, end)

  @Test
  def simpleTokeniseTest() = {
    implicit val methodName = "simpleTokeniseTest"
    def t = testForTokenise _
    val p1 = "@(param: String)"
    t(List(d(0, 0), s(1, p1.length() - 1)), p1)
    val p2 = "@*comment*@"
    t(List(c(0, p2.length() - 1)), p2)
    val p3 = "<html_tag></html_tag>"
    t(List(tg(0, 9), tg(10, p3.length() - 1)), p3)
    val p4 = "Some plain text"
    t(List(p(0, p4.length() - 1)), p4)
    val p5 = "@methodCall(arg)"
    t(List(d(0, 0), s(1, p5.length() - 1)), p5)
    val p6 = """@{val blockOfScalaCode = ""}"""
    t(List(d(0, 0), s(1, p6.length() - 1)), p6)
  }
  @Test
  def complexTokeniseTest() = {
    implicit val methodName = "complexTokeniseTest"
    def t = testForTokenise _
    // bug #20
    // reusable block
    val p1 = """@test = {
  is viewed
}"""
    t(List(d(0, 0), s(1, 4), d(5, 8), p(9, 21), d(22, 22)), p1)
    // bug #20
    // reusable block with scala code
    val p2 = """@test = @{
val x = "x string"
}"""
    t(List(d(0, 0), s(1, 4), d(5, 8), s(9, 30)), p2)
    // bug #18
    // for statements without yield
    val p3 = """@for()"""
    t(List(d(0, 0), s(1, 5)), p3)
  }

  private def testForTokenise(expected: List[ScalaPartitionRegion], program: String)(implicit methodName: String) = {
    val actual = tokenise(program)
    def noOverlap: Boolean = {
      def testOverlap(prev: ScalaPartitionRegion, list: List[ScalaPartitionRegion]): Boolean = {
        list match {
          case hd :: tail => !hd.containsPosition(prev.start) && !hd.containsPosition(prev.end) && testOverlap(hd, tail)
          case _ => true
        }
      }
      if (actual.length < 2)
        true
      else {
        testOverlap(actual.head, actual.tail)
      }
    }

    def complete: Boolean = {
      def testComplete(start: Int, list: List[ScalaPartitionRegion]): Boolean = {
        list match {
          case hd :: tail => if (start == hd.start) testComplete(hd.end + 1, tail) else false
          case _ => start == program.length()
        }
      }
      testComplete(0, actual)
    }

    assertTrue(methodName + "() is not correct, as list has overlap", noOverlap)
    assertTrue(methodName + "() is not correct, as list is not complete", complete)
    assertEquals(methodName + "() is not correct", expected, actual)
  }

}