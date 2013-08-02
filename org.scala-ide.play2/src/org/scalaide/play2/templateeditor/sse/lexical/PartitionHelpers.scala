package org.scalaide.play2.templateeditor.sse.lexical

import org.eclipse.jface.text.ITypedRegion
import org.eclipse.jface.text.TypedRegion
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions
import scala.collection.mutable.ArrayBuffer
import scala.annotation.tailrec

object PartitionHelpers {

  def isMagicAt(token: ITypedRegion, codeString: String) = {
    def tokenText = textOf(token, codeString).trim
    token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && tokenText == "@"
  }
  
  def isBrace(token: ITypedRegion, codeString: String) = {
    def tokenText = textOf(token, codeString).trim
    token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && tokenText.length > 0 && tokenText.foldLeft(true)((r, c) => r && (c == '{' || c == '}' || c.isWhitespace))
  }

  private def textOf(token: ITypedRegion, documentContent: String): String = 
    documentContent.substring(token.getOffset(), token.getOffset() + token.getLength())

  /**
   * Detect each "character sequence function" in turn, and return the offset in `codeString` at which the `sequenceFuncs(marked)` was detected (returned true)
   *  If you do not particularly care where the offset of a specific sequence function was detected, the value passed to `marked` does not matter.
   *  @return Sequence detection success will be marked of a `Some` value being returned, with the codeString offset corresponding to `marked` being the value of the `Some`.
   */
  private def detectSequence(codeString: String, sequenceFuncs: List[Char => Boolean], marked: Int, isIgnored: Char => Boolean): Option[Int] = {

    @tailrec
    def loop(code: List[Char], sequenceFuncs: List[Char => Boolean], toMarked: Int, index: Int, indexMarked: Int): Option[Int] = {
      code match {
        case Nil =>
          if (sequenceFuncs.isEmpty) {
            // codeString and sequence terminated at the same time, all good
            Some(indexMarked)
          } else {
            // codeString terminated, but not the sequence
            None
          }
        case c :: codeTail =>
          sequenceFuncs match {
            case Nil =>
              // found all elements of the sequence, check if all the remainder chars are to be ignored
              if (code.forall(isIgnored)) {
                Some(indexMarked)
              } else {
                None
              }
            case f :: fTail =>
              if (f(c)) {
                // right character for the sequence
                if (toMarked == 0) { // keep track of the index if needed
                  loop(codeTail, fTail, -1, index + 1, index)
                } else {
                  loop(codeTail, fTail, toMarked - 1, index + 1, indexMarked)
                }
              } else if (isIgnored(c)) { // continue if the char can be ignored
                loop(codeTail, sequenceFuncs, toMarked, index + 1, indexMarked)
              } else { // char not in sequence and not to be ignored
                None
              }
          }
      }
    }

    loop(codeString.toList, sequenceFuncs, marked, 0, -1)
  }

  /**
   * Check if the given token has the following characteristics:
   *  - Represents a "Template default" partition.
   *  - Corresponding text matches the following pseudo-regex: (spaceOrTab?)('{' | '}')(spaceOrTab?)('@')(spaceOrTab?)
   */
  def isCombinedBraceMagicAt(token: ITypedRegion, codeString: String): Boolean = {
    def tokenText = textOf(token, codeString)
    if(token.getType() == TemplatePartitions.TEMPLATE_DEFAULT) {
      val isBrace = {c: Char => c == '{' || c == '}'}
      val isAt = {c: Char => c == '@'}
      val ignored = {c: Char => c == ' ' || c == '\t'}
      detectSequence(tokenText, List(isBrace, isAt), 0, ignored).isDefined
    }
    else false
  }

  /* Combines neighbouring regions using `exploder` based on some user provided criteria, `test`. */
  def explodeAdjacent[T](partitions: Seq[ITypedRegion])(exploder: (ITypedRegion, ITypedRegion, T) => Seq[ITypedRegion])(test: (ITypedRegion, ITypedRegion) => Option[T]): IndexedSeq[ITypedRegion] = {
    val accum = new ArrayBuffer[ITypedRegion]
    for (region <- partitions) {
      if (accum.isEmpty)
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

  private def merge(l: ITypedRegion, r: ITypedRegion, t: String): List[TypedRegion] =
    List(new TypedRegion(l.getOffset, l.getLength + r.getLength, t))
    
  private val htmlPartitions: Set[String] = Set(TemplatePartitions.TEMPLATE_PLAIN, TemplatePartitions.TEMPLATE_TAG)
  
  /* Combines neighbouring regions that have the same type */
  def mergeAdjacentWithSameType(partitions: Seq[ITypedRegion]): IndexedSeq[ITypedRegion] = {
    explodeAdjacent(partitions)(merge) { (previousRegion, region) =>
      if (((htmlPartitions contains region.getType) && (htmlPartitions contains previousRegion.getType)) ||
         (region.getType == TemplatePartitions.TEMPLATE_SCALA && previousRegion.getType == TemplatePartitions.TEMPLATE_SCALA))
        Some(region.getType())
      else None
    }
  }

  /* Combines "magic ats" with scala code partitions that directly follow it */
  def combineMagicAt(partitions: Seq[ITypedRegion], codeString: String): IndexedSeq[ITypedRegion] = {
    explodeAdjacent(partitions)(merge) { (left, right) =>
      if ((isMagicAt(left, codeString) && right.getType() == TemplatePartitions.TEMPLATE_SCALA) ||
          (left.getType() == TemplatePartitions.TEMPLATE_SCALA && isMagicAt(right, codeString)))
        Some(TemplatePartitions.TEMPLATE_SCALA)
      else None
    }
  }
  
  /** If there is a default partition token that matches the following psuedo-regex: (spaceOrTab?)('=')(spaceOrTab?)('{' || '@')(spaceOrTab?) then split the token into two somewhere in the middle white space area */
  def separateBraceOrMagicAtFromEqual(partitions: Seq[ITypedRegion], codeString: String): IndexedSeq[ITypedRegion] = {
    def explode(l: ITypedRegion, r: ITypedRegion, splitIndex: Int): Seq[ITypedRegion] = {
      val first = new TypedRegion(l.getOffset(), splitIndex - l.getOffset(), l.getType())
      val second = new TypedRegion(splitIndex, l.getLength() - first.getLength(), l.getType())
      Seq(first, second, r)
    }
    
    explodeAdjacent(partitions)(explode) { (left, _) =>
      if (left.getType() == TemplatePartitions.TEMPLATE_DEFAULT) {
        def code = textOf(left, codeString)
        def isEquals = {c: Char => c == '='}
        def isBraceOrAt = {c: Char => c == '{' || c == '@'}
        def ignored = {c: Char => c == ' ' || c == '\t'}
        detectSequence(code, List(isEquals, isBraceOrAt), 1, ignored) map (_ + left.getOffset())
      }
      else None
    }
  }
}