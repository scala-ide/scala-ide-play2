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

  def getXMLTagRegions(text: String): List[ScalaPartitionRegion] = {
    val toks = ScalaPartitionTokeniser.tokenise(text)
    val newToks = toks.filter(_.contentType == ScalaPartitions.XML_TAG).map(t => {
      ScalaPartitionRegion(TemplatePartitions.TEMPLATE_TAG, t.start, t.end)
    })
    newToks
  }

  def getScalaCommentAndPlainRegions(text: String): (List[ScalaPartitionRegion], List[ScalaPartitionRegion]) = {
    val parts = TemplateParsing.handleTemplateCode(text)
    import TemplateParsing._
    var prevOffset = 0
    val tokens: List[ScalaPartitionRegion] = parts.map(t => {
      val contentType = t match {
        case ScalaCode(_) => TemplatePartitions.TEMPLATE_SCALA
        case DefaultCode(_) => TemplatePartitions.TEMPLATE_PLAIN
        case CommentCode(_) => TemplatePartitions.TEMPLATE_COMMENT
      }
      ScalaPartitionRegion(contentType, t.offset, t.length + t.offset - 1)
    })
    val sortedUsefulRegions = tokens.filter(e => (e.start != -1)).sort((a, b) => a.start < b.start)
    val plainRegions = sortedUsefulRegions.filter(e => e.contentType == TemplatePartitions.TEMPLATE_PLAIN)
    val scalaCommentRegions = sortedUsefulRegions.filter(e => e.contentType != TemplatePartitions.TEMPLATE_PLAIN)
    (scalaCommentRegions, plainRegions)
  }

  

  def calculateAllRegions(xmlTagPlainRegions: List[ScalaPartitionRegion], scalaCommentRegions: List[ScalaPartitionRegion], plainWithoutXmlTagRegions: List[ScalaPartitionRegion], text: String): List[ScalaPartitionRegion] = {
    def defaultRegion(start: Int, end: Int) =
      ScalaPartitionRegion(TemplatePartitions.TEMPLATE_DEFAULT, start, end)
    val allRegions = (xmlTagPlainRegions U scalaCommentRegions) U plainWithoutXmlTagRegions
    val globalRegion = defaultRegion(0, text.length() - 1)
    val defaultRegions = List(globalRegion) \ allRegions
    allRegions U defaultRegions
  }

  def tokenise(text: String): List[ScalaPartitionRegion] = {
    val xmlTagRegions = getXMLTagRegions(text)
    val (scalaCommentRegions, plainRegions) = getScalaCommentAndPlainRegions(text)
    val xmlTagPlainRegions = xmlTagRegions ^ plainRegions
    val plainWithoutXmlTagRegions = plainRegions \ xmlTagRegions
    calculateAllRegions(xmlTagPlainRegions, scalaCommentRegions, plainWithoutXmlTagRegions, text)
  }

}
