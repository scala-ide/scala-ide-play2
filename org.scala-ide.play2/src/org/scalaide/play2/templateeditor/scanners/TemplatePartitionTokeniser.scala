package org.scalaide.play2.templateeditor.scanners

import org.eclipse.jface.text._
import org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE
import scala.annotation.{ switch, tailrec }
import scala.collection.mutable.{ Stack, ListBuffer }
import scala.xml.parsing.TokenTests
import scala.tools.eclipse.lexical.ScalaPartitionRegion
import scala.tools.eclipse.lexical.ScalaPartitionTokeniser
import scala.tools.eclipse.lexical.ScalaPartitions
import scala.util.parsing.input.OffsetPosition

object TemplatePartitionTokeniser {

  def getXMLTagRegions(text: String): List[ScalaPartitionRegion] = {
    val toks = ScalaPartitionTokeniser.tokenise(text)
    val newToks = toks.filter(_.contentType == ScalaPartitions.XML_TAG).map(t => {
      ScalaPartitionRegion(TemplatePartitions.TEMPLATE_TAG, t.start, t.end)
    })
    newToks
  }

  def getScalaCommentRegions(text: String): List[ScalaPartitionRegion] = {
    val parts = TemplateParsing.handleTemplateCode(text)
    import TemplateParsing._
    var prevOffset = 0
    val tokens: List[ScalaPartitionRegion] = parts.map(t => {
      val contentType = t match {
        case ScalaCode(_) => TemplatePartitions.TEMPLATE_SCALA
        case DefaultCode(_) => TemplatePartitions.TEMPLATE_PLAIN
        case CommentCode(_) => TemplatePartitions.TEMPLATE_COMMENT
      }
      // TODO a bit of hack for comment part. it should be removed!
//      if (contentType != TemplatePartitions.TEMPLATE_COMMENT) {
//        prevOffset = t.length + t.offset
        ScalaPartitionRegion(contentType, t.offset, t.length + t.offset - 1)
//      } else {
//        val offset = text.indexOf("@*", prevOffset)
//        prevOffset = offset + t.length
//        ScalaPartitionRegion(contentType, offset, offset + t.length - 1)
//      }
    })
    tokens.filter(e => (e.start != -1 && e.contentType != TemplatePartitions.TEMPLATE_PLAIN)).sort((a, b) => a.start < b.start)
  }

  // TODO It's O(m*n). It should be changed to O(m+n) 
  def calculateXMLTagRegions(xmlTagRegions: List[ScalaPartitionRegion], scalaCommentRegions: List[ScalaPartitionRegion]): List[ScalaPartitionRegion] = {
    def tagRegion(start: Int, end: Int) =
      ScalaPartitionRegion(TemplatePartitions.TEMPLATE_TAG, start, end)
    val notFlatten = xmlTagRegions.map(x => {
      val elem = {
        if (scalaCommentRegions.exists(_.containsRange(x.start, x.end - x.start)))
          List()
        else {
          val elems = scalaCommentRegions.filter(e => x.containsRange(e.start, e.end - e.start + 1))
          if (!elems.isEmpty) {
            var startIndex = x.start
            val newElems = elems.foldLeft[List[ScalaPartitionRegion]](Nil)((prev, n) => {
              val newElem = tagRegion(startIndex, n.start - 1)
              startIndex = n.end + 1
              prev ::: List(newElem)
            })
            val notFiltered = newElems ::: List(tagRegion(startIndex, x.end))
            notFiltered.filter(s => (s.start <= s.end))
          } else {
            List(x)
          }
        }
      }
      elem
    })
    notFlatten.flatten
  }

  def merge[T](aList: List[T], bList: List[T], lt: (T, T) => Boolean): List[T] = bList match {
    case Nil => aList
    case _ =>
      aList match {
        case Nil => bList
        case x :: xs =>
          if (lt(x, bList.head))
            x :: merge(xs, bList, lt)
          else
            bList.head :: merge(aList, bList.tail, lt)
      }
  }

  def calculateAllRegions(slicedXmlTagRegions: List[ScalaPartitionRegion], scalaCommentRegions: List[ScalaPartitionRegion], text: String): List[ScalaPartitionRegion] = {
    def lt(r1: ScalaPartitionRegion, r2: ScalaPartitionRegion) =
      r1.start < r2.start
    def plainRegion(start: Int, end: Int) =
      ScalaPartitionRegion(TemplatePartitions.TEMPLATE_PLAIN, start, end)
    val tokens = merge(slicedXmlTagRegions, scalaCommentRegions, lt)
    var prevEnd = 0
    val newTokens = tokens.foldLeft[List[ScalaPartitionRegion]](Nil)((prev, elem) => {
      val newElems =
        if (elem.start != prevEnd) {
          val newElem = plainRegion(prevEnd, elem.start - 1)
          List(newElem, elem)
        } else
          List(elem)
      prevEnd = elem.end + 1
      prev ::: newElems
    })
    // Handles last unspecified partition
    if (prevEnd != text.length) {
      newTokens ::: List(plainRegion(prevEnd, text.length - 1))
    } else
      newTokens
  }

  def tokenise(text: String): List[ScalaPartitionRegion] = {
    val xmlTagRegions = getXMLTagRegions(text)
    val scalaCommentRegions = getScalaCommentRegions(text)
    val slicedXmlTagRegions = calculateXMLTagRegions(xmlTagRegions, scalaCommentRegions)
    calculateAllRegions(slicedXmlTagRegions, scalaCommentRegions, text)
  }

}
