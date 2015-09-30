package org.scalaide.play2.templateeditor.sse.lexical

import scala.annotation.tailrec
import scala.collection.JavaConverters

import org.eclipse.wst.sse.core.internal.parser.ContextRegion
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.sse.core.internal.text.BasicStructuredDocumentRegion
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeEqualsRegion
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeNameRegion
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeValueRegion
import org.eclipse.wst.xml.core.internal.parser.regions.TagCloseRegion
import org.eclipse.wst.xml.core.internal.parser.regions.TagNameRegion
import org.eclipse.wst.xml.core.internal.parser.regions.TagOpenRegion
import org.eclipse.wst.xml.core.internal.parser.regions.XMLContentRegion
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext.BLOCK_TEXT
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext.XML_CONTENT
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext.XML_TAG_NAME
import org.junit.Assert.assertTrue
import org.junit.Test
import org.scalaide.play2.templateeditor.BeforeAfterTemplateVersion
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses.BRACE
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses.COMMENT
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses.MAGIC_AT
import org.scalaide.play2.templateeditor.sse.lexical.TemplateDocumentRegions.COMMENT_DOC_REGION
import org.scalaide.play2.templateeditor.sse.lexical.TemplateDocumentRegions.SCALA_DOC_REGION
import org.scalaide.ui.syntax.ScalaSyntaxClasses

class TemplateRegionParserTest extends BeforeAfterTemplateVersion {
  
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
  
  private def comment(length: Int) = new TemplateTextRegion(COMMENT, 0, length, length)
  private def at = new TemplateTextRegion(MAGIC_AT, 0, 1, 1)
  private def bk = new TemplateTextRegion(ScalaSyntaxClasses.BRACKET, 0, 1, 1)
  private def bc = new TemplateTextRegion(BRACE, 0, 1, 1)
  private def op(length: Int = 1) = new TemplateTextRegion(ScalaSyntaxClasses.OPERATOR, 0, length, length)
  private def kw(length: Int) = new TemplateTextRegion(ScalaSyntaxClasses.KEYWORD, 0, length, length)
  private def oth(length: Int) = new TemplateTextRegion(ScalaSyntaxClasses.DEFAULT, 0, length, length)
  private def str(length: Int) = new TemplateTextRegion(ScalaSyntaxClasses.STRING, 0, length, length)
    
  private def xmlcontent(length: Int) = new XMLContentRegion(0, length)
  private def xmlto(length: Int) = new TagOpenRegion(0, length, length)
  private def xmltc = new TagCloseRegion(0)
  private def xmltn(length: Int) = new TagNameRegion(0, length, length)
  private def xmltan(length: Int) = new AttributeNameRegion(0, length, length)
  private def xmltav(length: Int) = new AttributeValueRegion(0, length, length)
  private def xmltae = new AttributeEqualsRegion(0, 1, 1)
  
  private def ctx(length: Int) = new ContextRegion("UNDEFINED", 0, length, length)

  private def addTextRegions(doc: IStructuredDocumentRegion, textRegions: Seq[ITextRegion]): Unit =
    textRegions.foreach(doc.addRegion(_))

  @Test
  def negativeStyleRange() = {
    import scala.collection.JavaConverters._
    val p = "@a() {\n\t<f\n\t@b.c()\n}"
    val parser = new TemplateRegionParser
    parser.reset(p)
    for (region <- parser.templateRegions.textRegions.asScala)
      assertTrue(s"ITextRegion found with negative text length: $region", region.getTextLength >= 0)
  }
    
  @Test
  def reuseableCodeBlock() = {
    val p = "@foo = {\n}"
    val e = tr(docR(10, oth(1).getType(), tr(at, oth(3), xmlcontent(3), bc, xmlcontent(1), bc)))
    performChecks(e, p)
  }
  
  @Test
  def pureReuseableCodeBlock() = {
    val p = "@foo = @{\n}"
    val e = tr(docR(11, oth(1).getType(), tr(at, oth(3), xmlcontent(3), at, bk, oth(1), bk)))
    performChecks(e, p)
  }
  
  @Test
  def multipleSpannedDocRegions() = {
    val p = "foo@{true && false} "
    val e = 
      tr(docR(10, at.getType(), tr(xmlcontent(3), at, bk, kw(4), oth(1))),
         docR(1, op().getType(), tr(op())),
         docR(9, oth(1).getType(), tr(op(), oth(1), kw(5), bk, xmlcontent(1))))
    performChecks(e, p)
  }
    
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
    performChecks(e2, p2, true)
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
    val e5 = tr(docR(0, "UNDEFINED", tr(ctx(0))))
    performChecks(e5, p5)
  }

  @Test
  def whitespaceTest() = {
    val p6 = " "
    val e6 = tr(docR(1, XML_CONTENT))
    performChecks(e6, p6, true)
  }
  
  @Test
  def complexTest() = {
    val p =
      """@defining("test") { uuid =>
<body data-ws-url="@routes.Application.listen(uuid).webSocketURL(request)" data-uuid="@uuid">
}"""
    // XMLStructuredDocumentRegions use the second text region of its children at its own type
    val e =
      tr(docR(28, oth(1).getType(), tr(at, oth(8), bk, str(6), bk, bc, oth(1), oth(4), oth(1), op(2), xmlcontent(1))),
         docR(90, xmltan(1).getType(),
              tr(xmlto(1), xmltn(5), xmltan(11), xmltae, xmltav(1), at, oth(6), op(), oth(11), op(), oth(6), bk, oth(4), bk,
                 op(), oth(12), bk, kw(4), bk, xmltav(2), xmltan(9), xmltae, xmltav(1), at, oth(4), xmltav(1), xmltc)),
         docR(2, bc.getType(), tr(xmlcontent(1), bc)))
  }
  
  @Test
  def jsScriptTest() = {
    val p = """<script type="text/javascript">function f() { return 0 }</script>"""
    performChecks(tr(docR(31, XML_TAG_NAME), docR(25, BLOCK_TEXT), docR(9, XML_TAG_NAME)), p, true)
  }
  
  /** ticket github#186
   *  `@importSomething` should not be parsed as an import statement.
   */
  @Test
  def importIdentifierTest() = {
    val p ="@importId text "
    val e = tr(docR(15, oth(0).getType(), tr(at, oth(8), xmlcontent(1), xmlcontent(4), xmlcontent(1))))
    performChecks(e, p)
  }
  
  private def performChecks(expected: List[IStructuredDocumentRegion], code: String, pureHTML: Boolean = false) = {
    val parser = new TemplateRegionParser
    parser.reset(code)
    val actual = parser.templateRegions.structuredRegions.toList
    
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
        assertTrue(s"Document region: '$actualDocRegion/${actualDocRegion.getType()}' does not match expected: '$expectedDocRegion'/${expectedDocRegion.getType}", regionsAreSame(actualDocRegion, expectedDocRegion))

        val actualTextRegions = actualDocRegion.getRegions().toArray().toList
        assertTrue(s"Document region, $actualDocRegion, has overlap in child text regions", noOverlap(actualTextRegions))
        assertTrue(s"Document region, $actualDocRegion, has incomplete child text regions", complete(actualTextRegions, actualDocRegion.getLength()))

        // Don't bother checking text regions if we haven't injected any of our own text regions
        if (!pureHTML) {
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
