package org.scalaide.play2.templateeditor.lexical

import org.eclipse.jface.text._
import org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE
import scala.annotation.{ switch, tailrec }
import scala.collection.mutable.{ Stack, ListBuffer }
import scala.xml.parsing.TokenTests
import scala.tools.eclipse.lexical.ScalaPartitionRegion
import scala.tools.eclipse.lexical.ScalaPartitionTokeniser
import scala.tools.eclipse.lexical.ScalaPartitions
import scala.util.parsing.input.OffsetPosition
import org.scalaide.play2.lexical.PlayPartitionTokeniser
import scala.collection.mutable.ArrayBuffer
import org.scalaide.play2.util.ScalaPartitionRegionUtils
import org.scalaide.play2.util.ScalaPartitionRegionUtils.union
import org.scalaide.play2.util.ScalaPartitionRegionUtils.subtract
import org.scalaide.play2.util.ScalaPartitionRegionUtils.intersect
import org.scalaide.play2.util.ScalaPartitionRegionUtils.advanceScalaPartitionRegionList

object TemplatePartitionTokeniser extends PlayPartitionTokeniser {

  /**
   * calculates XML tag regions by using scala partition tokensier
   */
  def getXMLTagRegions(templateCode: String): List[ScalaPartitionRegion] = {
    val toks = ScalaPartitionTokeniser.tokenise(templateCode)
    val newToks = toks.filter(_.contentType == ScalaPartitions.XML_TAG).map(t => {
      ScalaPartitionRegion(TemplatePartitions.TEMPLATE_TAG, t.start, t.end)
    })
    newToks
  }

  /**
   * calculates scala, comment, and plain regions using template parser
   */
  def getScalaCommentAndPlainRegions(templateCode: String): (List[ScalaPartitionRegion], List[ScalaPartitionRegion]) = {
    val parts = TemplateParsing.handleTemplateCode(templateCode)
    import TemplateParsing._
    val tokens: List[ScalaPartitionRegion] = parts.map(t => {
      val contentType = t match {
        case ScalaCode(_) => TemplatePartitions.TEMPLATE_SCALA
        case DefaultCode(_) => TemplatePartitions.TEMPLATE_PLAIN 
        case CommentCode(_) => TemplatePartitions.TEMPLATE_COMMENT
      }
      ScalaPartitionRegion(contentType, t.offset, t.length + t.offset - 1)
    })
    val sortedUsefulRegions = tokens.filter(e => (e.start != -1)&&(e.end >= e.start)).sortWith((a, b) => a.start < b.start)
    val plainRegions = sortedUsefulRegions.filter(e => e.contentType == TemplatePartitions.TEMPLATE_PLAIN)
    val scalaCommentRegions = sortedUsefulRegions.filter(e => e.contentType != TemplatePartitions.TEMPLATE_PLAIN)
    (scalaCommentRegions, plainRegions)
  }

  /**
   * calculates the template partitions.
   * 
   * @param xmlTagPlainRegions 			tags which are plain text, which means are represented to user
   * @param scalaCommentRegions			scala and comment regions
   * @param plainWithoutXmlTagRegions 	plain texts which are not part of tag
   * @param templateCode				template code which we'd like to tokenise
   */
  def calculateAllRegions(xmlTagPlainRegions: List[ScalaPartitionRegion], scalaCommentRegions: List[ScalaPartitionRegion], plainWithoutXmlTagRegions: List[ScalaPartitionRegion], templateCode: String): List[ScalaPartitionRegion] = {
    def defaultRegion(start: Int, end: Int) =
      ScalaPartitionRegion(TemplatePartitions.TEMPLATE_DEFAULT, start, end)
    val allRegions = (xmlTagPlainRegions U scalaCommentRegions) U plainWithoutXmlTagRegions
    val globalRegion = defaultRegion(0, templateCode.length() - 1)
    val defaultRegions = List(globalRegion) \ allRegions
    allRegions U defaultRegions
  }

  def tokenise(template: IDocument): List[ScalaPartitionRegion] = {
    val templateCode = template.get
    val xmlTagRegions = getXMLTagRegions(templateCode)
    val (scalaCommentRegions, plainRegions) = getScalaCommentAndPlainRegions(templateCode)
    val xmlTagPlainRegions = xmlTagRegions ^ plainRegions
    val plainWithoutXmlTagRegions = plainRegions \ xmlTagRegions
    calculateAllRegions(xmlTagPlainRegions, scalaCommentRegions, plainWithoutXmlTagRegions, templateCode)
  }

}
