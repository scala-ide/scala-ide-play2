package org.scalaide.play2.templateeditor.sse.lexical

import org.eclipse.jface.text.ITypedRegion
import org.eclipse.jface.text.TypedRegion
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions

object PartitionHelpers {
  def isMagicAt(token: ITypedRegion, codeString: String) = {
    val s = codeString.substring(token.getOffset(), token.getOffset() + token.getLength()).trim
    token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && s.length == 1 && s == "@"
  }
  
  def isBrace(token: ITypedRegion, codeString: String) = {
    val s = codeString.substring(token.getOffset(), token.getOffset() + token.getLength()).trim
    token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && s.length == 1 && (s == "}" || s == "{")
  }
  
  /* Combines neighbouring regions that have the same type */
  def mergeAdjacent[Repr <: Seq[ITypedRegion]](partitions: Repr)(test: (ITypedRegion, ITypedRegion) => Option[String]): IndexedSeq[ITypedRegion] = {
    partitions.foldLeft(Array[ITypedRegion]())((accum, region) => {
      accum match {
        case Array() => Array(region)
        case _       => {
          def merge(l: ITypedRegion, r: ITypedRegion, t: String) = 
            new TypedRegion(l.getOffset, l.getLength + r.getLength, t)
          val htmlPartitions = Set(TemplatePartitions.TEMPLATE_PLAIN, TemplatePartitions.TEMPLATE_TAG)
          val previousRegion = accum.last
          test(previousRegion, region) match {
            case Some(tpe) => accum.dropRight(1) :+ merge(previousRegion, region, tpe)
            case None      => accum :+ region
          }
        }
      }
    })
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

  def combineMagicAt[Repr <: Seq[ITypedRegion]](partitions: Repr, codeString: String): IndexedSeq[ITypedRegion] = {
    mergeAdjacent(partitions) { (left, right) =>
      if ((isMagicAt(left, codeString) && right.getType() == TemplatePartitions.TEMPLATE_SCALA) ||
          (left.getType() == TemplatePartitions.TEMPLATE_SCALA && isMagicAt(right, codeString)))
        Some(TemplatePartitions.TEMPLATE_SCALA)
      else None
    }
  }
}