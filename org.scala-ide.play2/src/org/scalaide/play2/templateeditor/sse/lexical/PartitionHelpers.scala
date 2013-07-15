package org.scalaide.play2.templateeditor.sse.lexical

import org.eclipse.jface.text.ITypedRegion
import org.eclipse.jface.text.TypedRegion
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer

object PartitionHelpers {
  def isMagicAt(token: ITypedRegion, codeString: String) = {
    val s = codeString.substring(token.getOffset(), token.getOffset() + token.getLength()).trim
    token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && s.length == 1 && s == "@"
  }
  
  def isBrace(token: ITypedRegion, codeString: String) = {
    val s = codeString.substring(token.getOffset(), token.getOffset() + token.getLength()).trim
    token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && s.length > 0 && s.foldLeft(true)((r, c) => r && (c == '{' || c == '}' || c.isWhitespace))
  }
  
  private def detectSequence(codeString: String, fs: ListBuffer[Char => Boolean], marked: Int, isIgnored: Char => Boolean): Option[Int] = {
    var sawBadChar = false
    var index = -1
    var currentF = 0
    for (i <- 0 to codeString.length() - 1) {
      val c = codeString(i)
      if (fs.isEmpty) 
        sawBadChar = sawBadChar || (!isIgnored(c))
      else {
        if (fs.head(c)) {
          if (currentF == marked)
            index = i
          fs.remove(0)
          currentF += 1
        }
        else
          sawBadChar = sawBadChar || (!isIgnored(c))
      }
    }
    
    if(fs.isEmpty && !sawBadChar)
      Some(index)
    else
      None
  }

  /**
   * Check if the given token has the following characteristics:
   *  - Represents a "Template default" partition.
   *  - Corresponding text matches the following pseudo-regex: (spaceOrTab?)('{' | '}')(spaceOrTab?)('@')(spaceOrTab?)
   */
  def isCombinedBraceMagicAt(token: ITypedRegion, codeString: String): Boolean = {
    val s = codeString.substring(token.getOffset(), token.getOffset() + token.getLength())
    if(token.getType() == TemplatePartitions.TEMPLATE_DEFAULT) {
      val firstBrace = {c: Char => c == '{' || c == '}'}
      val secondAt = {c: Char => c == '@'}
      val ignored = {c: Char => c == ' ' || c == '\t'}
      detectSequence(s, ListBuffer(firstBrace, secondAt), 0, ignored).isDefined
    }
    else false
  }

  /* Combines neighbouring regions based on some user provided criteria */
  def explodeAdjacent[T, Repr <: Seq[ITypedRegion]](partitions: Repr)(exploder: (ITypedRegion, ITypedRegion, T) => Seq[ITypedRegion])(test: (ITypedRegion, ITypedRegion) => Option[T]): IndexedSeq[ITypedRegion] = {
    val htmlPartitions = Set(TemplatePartitions.TEMPLATE_PLAIN, TemplatePartitions.TEMPLATE_TAG)
    val accum = new ArrayBuffer[ITypedRegion]
    for (region <- partitions) {
      if (accum.length == 0)
        accum += region
      else {
        val previousRegion = accum.last
        test(previousRegion, region) match {
          case Some(tpe) => {
            accum.remove(accum.length - 1)
            accum.insert(accum.length, exploder(previousRegion, region, tpe):_*)
          }
          case None =>
            accum += region
        }
      }
    }
    accum
  }

  private def merge(l: ITypedRegion, r: ITypedRegion, t: String) =
    List(new TypedRegion(l.getOffset, l.getLength + r.getLength, t))
  
  /* Combines neighbouring regions that have the same type */
  def mergeAdjacentWithSameType[Repr <: Seq[ITypedRegion]](partitions: Repr): IndexedSeq[ITypedRegion] = {
    val htmlPartitions = Set(TemplatePartitions.TEMPLATE_PLAIN, TemplatePartitions.TEMPLATE_TAG)
    explodeAdjacent(partitions)(merge) { (previousRegion, region) =>
      if (((htmlPartitions contains region.getType) && (htmlPartitions contains previousRegion.getType)) ||
         (region.getType == TemplatePartitions.TEMPLATE_SCALA && previousRegion.getType == TemplatePartitions.TEMPLATE_SCALA))
        Some(region.getType())
      else None
    }
  }

  /* Combine magic at with scala code partitions */
  def combineMagicAt[Repr <: Seq[ITypedRegion]](partitions: Repr, codeString: String): IndexedSeq[ITypedRegion] = {
    explodeAdjacent(partitions)(merge) { (left, right) =>
      if ((isMagicAt(left, codeString) && right.getType() == TemplatePartitions.TEMPLATE_SCALA) ||
          (left.getType() == TemplatePartitions.TEMPLATE_SCALA && isMagicAt(right, codeString)))
        Some(TemplatePartitions.TEMPLATE_SCALA)
      else None
    }
  }
  
  def separateBraceOrMagicAtFromEqual[Repr <: Seq[ITypedRegion]](partitions: Repr, codeString: String): IndexedSeq[ITypedRegion] = {
    def explode(l: ITypedRegion, r: ITypedRegion, splitIndex: Int) = {
      val first = new TypedRegion(l.getOffset(), splitIndex - l.getOffset(), l.getType())
      val second = new TypedRegion(splitIndex, l.getLength() - first.getLength(), l.getType())
      Array(first, second, r)
    }
    
    explodeAdjacent(partitions)(explode) { (left, _) =>
      if (left.getType() == TemplatePartitions.TEMPLATE_DEFAULT) {
        val code = codeString.substring(left.getOffset(), left.getOffset() + left.getLength())
        def firstEqual = {c: Char => c == '='}
        def secondBrace = {c: Char => c == '{' || c == '@'}
        def ignored = {c: Char => c == ' ' || c == '\t'}
        detectSequence(code, ListBuffer(firstEqual, secondBrace), 1, ignored) map (_ + left.getOffset())
      }
      else None
    }
  }
}