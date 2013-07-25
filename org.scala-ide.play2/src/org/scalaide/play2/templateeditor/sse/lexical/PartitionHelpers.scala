package org.scalaide.play2.templateeditor.sse.lexical

import org.eclipse.jface.text.ITypedRegion
import org.eclipse.jface.text.TypedRegion
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions
import scala.collection.mutable.ArrayBuffer

object PartitionHelpers {
  def isMagicAt(token: ITypedRegion, codeString: String) = {
    val s = codeString.substring(token.getOffset(), token.getOffset() + token.getLength()).trim
    token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && s.length == 1 && s == "@"
  }
  
  def isBrace(token: ITypedRegion, codeString: String) = {
    val s = codeString.substring(token.getOffset(), token.getOffset() + token.getLength()).trim
    token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && s.length > 0 && s.foldLeft(true)((r, c) => r && (c == '{' || c == '}' || c.isWhitespace))
  }

  /**
   * Check if the given token has the following characteristics:
   *  - Represents a "Template default" partition.
   *  - Corresponding text matches the following pseudo-regex: (spaceOrTab?)('{' | '}')(spaceOrTab?)('@')(spaceOrTab?)
   */
  def isCombinedBraceMagicAt(token: ITypedRegion, codeString: String): Boolean = {
    val s = codeString.substring(token.getOffset(), token.getOffset() + token.getLength())
    if(token.getType() == TemplatePartitions.TEMPLATE_DEFAULT) {
      var seenBrace = false
      var seenAt = false
      var seenBadChar = false
      for (c <- s) {
        if (!seenBrace) {
          if (c == '{' || c == '}') seenBrace = true
          else if (!(c == ' ' || c == '\t')) seenBadChar = true
        }
        else if (seenBrace) {
          if (c == '@') seenAt = true
          else if (!(c == ' ' || c == '\t')) seenBadChar = true
        }
        else if (!(c == ' ' || c == '\t')) seenBadChar = true
      }
      seenBrace && seenAt && !seenBadChar
    }
    else false
  }

  /* Combines neighbouring regions based on some user provided criteria */
  def mergeAdjacent[Repr <: Seq[ITypedRegion]](partitions: Repr)(test: (ITypedRegion, ITypedRegion) => Option[String]): IndexedSeq[ITypedRegion] = {
    val htmlPartitions = Set(TemplatePartitions.TEMPLATE_PLAIN, TemplatePartitions.TEMPLATE_TAG)
    val accum = new ArrayBuffer[ITypedRegion]
    for (region <- partitions) {
      if (accum.length == 0)
        accum += region
      else {
        def merge(l: ITypedRegion, r: ITypedRegion, t: String) =
          new TypedRegion(l.getOffset, l.getLength + r.getLength, t)
        val previousRegion = accum.last
        test(previousRegion, region) match {
          case Some(tpe) =>
            accum(accum.length - 1) = merge(previousRegion, region, tpe)
          case None =>
            accum += region
        }
      }
    }
    accum
  }
  
  /* Combines neighbouring regions that have the same type */
  def mergeAdjacentWithSameType[Repr <: Seq[ITypedRegion]](partitions: Repr): IndexedSeq[ITypedRegion] = {
    val htmlPartitions = Set(TemplatePartitions.TEMPLATE_PLAIN, TemplatePartitions.TEMPLATE_TAG)
    mergeAdjacent(partitions) { (previousRegion, region) =>
      if (((htmlPartitions contains region.getType) && (htmlPartitions contains previousRegion.getType)) ||
         (region.getType == TemplatePartitions.TEMPLATE_SCALA && previousRegion.getType == TemplatePartitions.TEMPLATE_SCALA))
        Some(region.getType())
      else None
    }
  }

  /* Combine magic at with scala code partitions */
  def combineMagicAt[Repr <: Seq[ITypedRegion]](partitions: Repr, codeString: String): IndexedSeq[ITypedRegion] = {
    mergeAdjacent(partitions) { (left, right) =>
      if ((isMagicAt(left, codeString) && right.getType() == TemplatePartitions.TEMPLATE_SCALA) ||
          (left.getType() == TemplatePartitions.TEMPLATE_SCALA && isMagicAt(right, codeString)))
        Some(TemplatePartitions.TEMPLATE_SCALA)
      else None
    }
  }
}