package org.scalaide.play2.routeeditor

import scala.tools.eclipse.testsetup.TestProjectSetup
import org.junit.Test
import org.eclipse.jface.text.IRegion
import org.junit.Assert._

class RouteHyperlinkDetectorTest {

  private val detector = new RouteHyperlinkDetector(null);

  @Test
  def findWordTest() = {
    def t = testForFindWord _
    val d1 = "test.Controller.show()";
    val e1 = "test.Controller.show"
    for (i <- 0 until e1.length)
      t(d1, i, e1)
  }

  def testForFindWord(document: String, offset: Int, expected: String) = {
    val region = detector.findWord(document, offset);
    val actual = document.substring(region.getOffset(), region.getLength() + region.getOffset())
    assertEquals("findWord() does not work", expected, actual)
  }

  @Test
  def findParameterTypesTest() = {
    val d1 = "test.Controller.show()";
    val d2 = "test.Controller.show(id)";
    val d3 = "test.Controller.show(id:Integer)";
    val d4 = "test.Controller.show(id:   test.Long = 2, name)";
    val d5 = "test.Controller.show(id:		int ?= 2)";
    val d6 = "test.Controller.show_for_me()";
    val d7 = "test.Controller.show(a1_2: Object)";
    val d8 = "test.Controller.show(a2__5: Object, a3: Integer)";

    def t = testForFindParameterTypes _

    t(d1, Array())
    t(d2, Array("String"))
    t(d3, Array("Integer"))
    t(d4, Array("test.Long", "String"))
    t(d5, Array("int"))
    t(d6, Array())
    t(d7, Array("Object"))
    t(d8, Array("Object", "Integer"))
  }

  private def testForFindParameterTypes(document: String, expected: Array[String]) = {
    val r = detector.findWord(document, 0);
    val endOfMethodNameIndex = r.getLength() + r.getOffset()
    val actual = detector.findParameterTypes(document, endOfMethodNameIndex)
    def sa2s = stringArrayToString _
    assertEquals("findParameterTypes() does not work", sa2s(expected), sa2s(actual))
  }

  private def stringArrayToString(stringArray: Array[String]): String = {
    if (stringArray == null || stringArray.length == 0) {
      return "()"
    }
    val tmp = stringArray.fold("")((prev, n) => prev + n + ",")
    "(" + tmp.substring(0, tmp.length - 1) + ")"
  }
}