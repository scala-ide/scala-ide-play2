package org.scalaide.play2.templateeditor.sse.lexical

import org.junit.Test
import org.junit.Assert._
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import scala.annotation.tailrec
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.sse.core.internal.text.BasicStructuredDocumentRegion
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext._
import org.scalaide.play2.templateeditor.sse.lexical.TemplateDocumentRegions._
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses._
import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClasses

class TemplateRegionParserTest {
  
  private def docR(length: Int, tpe: String, textRegions: Seq[ITextRegion] = List()): IStructuredDocumentRegion = {
    val region = new BasicStructuredDocumentRegion { override def getType() = tpe }
    region.setLength(length)
    addTextRegions(region, textRegions)
    region
  }
  
  private def tr[T <: ITextRegion](textRegions: T*): List[T] = {
    textRegions.sliding(2) foreach {
      case Seq(l, r) => r.adjustStart(l.getEnd())
      case _ =>
    }
    textRegions.toList
  }
  
  private def comment(length: Int) = new ScalaTextRegion(COMMENT, 0, length, length)
  private def at = new ScalaTextRegion(MAGIC_AT, 0, 1, 1)
  private def bk = new ScalaTextRegion(ScalaSyntaxClasses.BRACKET, 0, 1, 1)
  private def bc = new ScalaTextRegion(BRACE, 0, 1, 1)
  private def op(length: Int = 1) = new ScalaTextRegion(ScalaSyntaxClasses.OPERATOR, 0, length, length)
  private def kw(length: Int) = new ScalaTextRegion(ScalaSyntaxClasses.KEYWORD, 0, length, length)
  private def oth(length: Int) = new ScalaTextRegion(ScalaSyntaxClasses.DEFAULT, 0, length, length)
  private def str(length: Int) = new ScalaTextRegion(ScalaSyntaxClasses.STRING, 0, length, length)


  private def addTextRegions(doc: IStructuredDocumentRegion, textRegions: Seq[ITextRegion]): Unit =
    textRegions.foreach(doc.addRegion(_))
    
  @Test
  def commentTest() = {
    val p1 = "@*comment*@"
    val e1 = tr(docR(11, COMMENT_DOC_REGION, tr(comment(11))))
    performChecks(e1, p1)
  }
  
  @Test
  def htmlTest() = {
    val p2 = "<html></html>"
    val e2 = tr(docR(6, XML_TAG_NAME), docR(7, XML_TAG_NAME))
    performChecks(e2, p2)
  }
    
  @Test
  def simpleScalaTest() = {
    val p3 = """@obj.method("s")"""
    val e3 = tr(docR(16, SCALA_DOC_REGION, tr(at, oth(3), op(), oth(6), bk, str(3), bk)))
    performChecks(e3, p3)
  }
    
  @Test
  def simpleScalaBlockTest() = {
    val p4 = """@{val foo = ""}"""
    val e4 = tr(docR(15, SCALA_DOC_REGION, tr(at, bk, kw(3), oth(1), oth(3), oth(1), op(), oth(1), str(2), bk)))
    performChecks(e4, p4)
  }
    
  @Test
  def zeroLengthTest() = {
    val p5 = ""
    val e5 = tr(docR(0, "UNDEFINED"))
    performChecks(e5, p5)
  }

  @Test
  def whitespaceTest() = {
    val p6 = " "
    val e6 = tr(docR(1, XML_CONTENT))
    performChecks(e6, p6)
  }
  
  @Test
  def complexTest() = {
    val p =
      """@defining("test") { uuid =>
<body data-ws-url="@routes.Application.listen(uuid).webSocketURL(request)" data-uuid="@uuid">
}"""
    val e =
      tr(docR(17, SCALA_DOC_REGION, tr(at, oth(8), bk, str(6), bk)),
         docR(2,  SCALA_DOC_REGION, tr(bc)),
         docR(8,  SCALA_DOC_REGION, tr(oth(1), oth(4), oth(1), op(2))),
         docR(1,  XML_CONTENT),
         docR(19, XML_TAG_NAME),
         docR(54, SCALA_DOC_REGION, tr(at, oth(6), op(), oth(11), op(), oth(6), op(), oth(12), bk, oth(7), bk)),
         docR(13, XML_CONTENT), // a known bug
         docR(5, SCALA_DOC_REGION, tr(at, oth(4))),
         docR(3, XML_CONTENT), // same part of the known bug
         docR(1, SCALA_DOC_REGION, tr(bc)))
  }
  
  @Test
  def jsScriptTest() = {
    val p = """<script type="text/javascript">function f() { return 0 }</script>"""
    performChecks(tr(docR(31, XML_TAG_NAME), docR(25, BLOCK_TEXT), docR(9, XML_TAG_NAME)), p)
  }
  
  private def performChecks(expected: List[IStructuredDocumentRegion], code: String) = {
    val parser = new TemplateRegionParser
    parser.reset(code)
    val actual = parser.computeRegions(code).toList
    
    @tailrec
    def noOverlap(list: List[ITextRegion]): Boolean = {
      list match {
        case first :: second :: tail => {
          val overlaps = first.getStart() < second.getEnd() && second.getStart() < first.getEnd()
          !overlaps && noOverlap(second :: tail)
        }
        case _ => true
      }
    }
    
    def complete(regions: List[ITextRegion], expectedLength: Int): Boolean = {
      def testComplete(start: Int, list: List[ITextRegion]): Boolean = {
        list match {
          case head :: tail => if (start == head.getStart()) testComplete(head.getEnd(), tail) else false
          case _ => start == expectedLength
        }
      }
      testComplete(0, regions)
    }
    
    def regionsAreSame(left: ITextRegion, right: ITextRegion): Boolean =
      left.getStart() == right.getStart() && left.getEnd() == right.getEnd() && left.getType() == right.getType()
    
    assertTrue("Document regions has overlap", noOverlap(actual))
    assertTrue("Document regions are not complete", complete(actual, code.length()))
    if (actual.size == expected.size) {
      for ((actualDocRegion, expectedDocRegion) <- (actual zip expected)) {
        assertTrue(s"Document region: '$actualDocRegion' does not match expected: '$expectedDocRegion'", regionsAreSame(actualDocRegion, expectedDocRegion))

        val actualTextRegions = actualDocRegion.getRegions().toArray().toList
        assertTrue(s"Document region, $actualDocRegion, has overlap in child text regions", noOverlap(actualTextRegions))
        assertTrue(s"Document region, $actualDocRegion, has incomplete child text regions", complete(actualTextRegions, actualDocRegion.getLength()))

        // We'll assume the html text regions are always correct
        val tpe = actualDocRegion.getType()
        if ((tpe == COMMENT_DOC_REGION || tpe == SCALA_DOC_REGION)) {
          val expectedTextRegions = expectedDocRegion.getRegions().toArray().toList
          if (actualTextRegions.size == expectedTextRegions.size) {
            for ((actualTextRegion, expectedTextRegion) <- (actualTextRegions zip expectedTextRegions)) {
              assertTrue(s"In document region: '$actualDocRegion', actual text region: '$actualTextRegion' does not match expected: '$expectedTextRegion'", regionsAreSame(actualTextRegion, expectedTextRegion))
            }
          }
        }
      }
    }
  }

}