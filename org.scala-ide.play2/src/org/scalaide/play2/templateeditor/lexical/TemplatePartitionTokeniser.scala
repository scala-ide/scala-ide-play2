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

object TemplatePartitionTokeniser extends PlayPartitionTokeniser {

  def getXMLTagRegions(text: String): List[ScalaPartitionRegion] = {
    val toks = ScalaPartitionTokeniser.tokenise(text)
    val newToks = toks.filter(_.contentType == ScalaPartitions.XML_TAG).map(t => {
      ScalaPartitionRegion(TemplatePartitions.TEMPLATE_TAG, t.start, t.end)
    })
    newToks
  }

  def subtractSortedRegions(first: List[ScalaPartitionRegion], second: List[ScalaPartitionRegion]): List[ScalaPartitionRegion] = {
    def subtract(a: List[ScalaPartitionRegion], b: List[ScalaPartitionRegion]): List[ScalaPartitionRegion] = {
      (a, b) match {
        case (x :: xs, y :: ys) =>
          if (x.end < y.start)
            //x: ___
            //y:      +++
            x :: subtract(xs, b)
          else if (y.end < x.start)
            //x:      ___
            //y: +++
            subtract(a, ys)
          else if (x.containsRange(y.start, y.end - y.start)) { // x contains y
            //x:   -------
            //y:    +++++
            val newElem =
              if (x.start == y.start)
                //x:  -------
                //y:  ++
                Nil
              else
                //x:  -------
                //y:    ++
                List(x.copy(end = y.start - 1))
            val producedElem =
              if (x.end == y.end)
                //x:  -------
                //y:       ++
                Nil
              else
                //x:  -------
                //y:      ++
                List(x.copy(start = y.end + 1))
            newElem ::: subtract(producedElem ::: xs, ys)
          } else if (y.containsRange(x.start, x.end - x.start)) { // y contains x
            //x:    -----
            //y:   +++++++
            subtract(xs, b)
          } else if (x.containsPosition(y.end)) {
            //x:  -------
            //y: ++++
            val producedElem = x.copy(start = y.end + 1)
            subtract(producedElem :: xs, ys)
          } else if (x.containsPosition(y.start)) {
            //x:  -------
            //y:      ++++++
            val newElem = x.copy(end = y.start - 1)
            newElem :: subtract(xs, b)
          } else {
            throw new RuntimeException("Unhandled case! Impossible!")
          }
        case (xl, Nil) => xl
        case (Nil, _) => Nil
      }
    }
    subtract(first, second)
  }

  def unionSortedRegions(regions: List[ScalaPartitionRegion]): List[ScalaPartitionRegion] = {
    if (regions.length < 2)
      return regions
    val result = new ArrayBuffer[ScalaPartitionRegion]()
    var tempRegion: ScalaPartitionRegion = regions.head

    @inline def expand(newEnd: Int) {
      tempRegion = tempRegion.copy(end = newEnd)
    }
    regions.tail.foreach { region =>
      if (region.start <= tempRegion.end) {
        if (region.end > tempRegion.end)
          expand(region.end)
      } else {
        result += tempRegion
        tempRegion = region
      }
    }
    result += tempRegion
    result.toList
  }

  def getScalaCommentAndUnionRegions(text: String): (List[ScalaPartitionRegion], List[ScalaPartitionRegion]) = {
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
    val unionRegions = unionSortedRegions(sortedUsefulRegions)
    val scalaCommentRegions = sortedUsefulRegions.filter(e => e.contentType != TemplatePartitions.TEMPLATE_PLAIN)
    (scalaCommentRegions, unionRegions)
  }

  //It uses substract method which is O(m+n) 
  def calculateXMLTagRegions(xmlTagRegions: List[ScalaPartitionRegion], scalaCommentRegions: List[ScalaPartitionRegion]): List[ScalaPartitionRegion] = {
    subtractSortedRegions(xmlTagRegions, scalaCommentRegions)
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
    def defaultRegion(start: Int, end: Int) =
      ScalaPartitionRegion(TemplatePartitions.TEMPLATE_DEFAULT, start, end)
    val tokens = merge(slicedXmlTagRegions, scalaCommentRegions, lt)
    var prevEnd = 0
    val newTokens = tokens.foldLeft[List[ScalaPartitionRegion]](Nil)((prev, elem) => {
      val newElems =
        if (elem.start != prevEnd) {
          val newElem = defaultRegion(prevEnd, elem.start - 1)
          List(newElem, elem)
        } else
          List(elem)
      prevEnd = elem.end + 1
      prev ::: newElems
    })
    // Handles last unspecified partition
    if (prevEnd != text.length) {
      newTokens ::: List(defaultRegion(prevEnd, text.length - 1))
    } else
      newTokens
  }

  def tokenise(text: String): List[ScalaPartitionRegion] = {
    val xmlTagRegions = getXMLTagRegions(text)
    val (scalaCommentRegions, unionRegions) = getScalaCommentAndUnionRegions(text)
    val slicedXmlTagRegions = calculateXMLTagRegions(xmlTagRegions, scalaCommentRegions)
    calculateAllRegions(slicedXmlTagRegions, scalaCommentRegions, text)
  }

}
