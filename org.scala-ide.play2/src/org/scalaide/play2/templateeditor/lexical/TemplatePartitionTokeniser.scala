package org.scalaide.play2.templateeditor.lexical

import org.eclipse.jface.text._
import org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE
import scala.annotation.{ switch, tailrec }
import scala.collection.mutable.{ Stack, ListBuffer }
import scala.xml.parsing.TokenTests
import org.eclipse.jface.text.TypedRegion
import scala.tools.eclipse.lexical.ScalaPartitionTokeniser
import scala.tools.eclipse.lexical.ScalaPartitions
import scala.util.parsing.input.OffsetPosition
import org.scalaide.play2.lexical.PlayPartitionTokeniser
import scala.collection.mutable.ArrayBuffer
import org.scalaide.editor.util.RegionHelper._

class TemplatePartitionTokeniser extends PlayPartitionTokeniser {

  /** Calculates XML tag regions by using scala partition tokensier. */
  private def getXMLTagRegions(templateCode: String): List[TypedRegion] = {
    val tokens = ScalaPartitionTokeniser.tokenise(templateCode)
    tokens.filter(_.contentType == ScalaPartitions.XML_TAG).map(t => {
      new TypedRegion(t.start, t.length, TemplatePartitions.TEMPLATE_TAG)
    })
  }

  /** Calculates scala, comment, and plain regions using template parser. */
  private def getScalaCommentAndPlainRegions(templateCode: String): (List[TypedRegion], List[TypedRegion]) = {
    val parts = TemplateParsing.handleTemplateCode(templateCode)
    import TemplateParsing._
    val tokens: List[TypedRegion] = parts.map(t => {
      val contentType = t match {
        case ScalaCode(_) => TemplatePartitions.TEMPLATE_SCALA
        case DefaultCode(_) => TemplatePartitions.TEMPLATE_PLAIN 
        case CommentCode(_) => TemplatePartitions.TEMPLATE_COMMENT
      }
      new TypedRegion(t.offset, t.length, contentType)
    })
    val sortedUsefulRegions = tokens.filter(e => (e.getOffset() != -1) && (e.getLength() > 0)).sortWith((a, b) => a.getOffset() < b.getOffset())
    val plainRegions = sortedUsefulRegions.filter(e => e.getType() == TemplatePartitions.TEMPLATE_PLAIN)
    val scalaCommentRegions = sortedUsefulRegions.filter(e => e.getType() != TemplatePartitions.TEMPLATE_PLAIN)
    (scalaCommentRegions, plainRegions)
  }

  /** 
   * Calculates the template partitions.
   * 
   * @param xmlTagPlainRegions 			tags which are plain text, which means are represented to user
   * @param scalaCommentRegions			scala and comment regions
   * @param plainWithoutXmlTagRegions 	plain texts which are not part of tag
   * @param templateCode				template code which we'd like to tokenise
   */
  private def calculateAllRegions(xmlTagPlainRegions: List[TypedRegion], scalaCommentRegions: List[TypedRegion], plainWithoutXmlTagRegions: List[TypedRegion], templateCode: String): List[TypedRegion] = {
    def defaultRegion(start: Int, offset: Int) =
      new TypedRegion(start, offset, TemplatePartitions.TEMPLATE_DEFAULT)
    val allRegions = (xmlTagPlainRegions U scalaCommentRegions) U plainWithoutXmlTagRegions
    val globalRegion = defaultRegion(0, templateCode.length())
    val defaultRegions = List(globalRegion) \ allRegions
    allRegions U defaultRegions
  }

  override final def tokenise(template: IDocument): List[TypedRegion] = 
    tokenise(template.get)

  def tokenise(templateCode: String): List[TypedRegion] = {
    val xmlTagRegions = getXMLTagRegions(templateCode)
    val (scalaCommentRegions, plainRegions) = getScalaCommentAndPlainRegions(templateCode)
    val xmlTagPlainRegions = xmlTagRegions ^ plainRegions
    val plainWithoutXmlTagRegions = plainRegions \ xmlTagRegions
    calculateAllRegions(xmlTagPlainRegions, scalaCommentRegions, plainWithoutXmlTagRegions, templateCode)
  }
}
