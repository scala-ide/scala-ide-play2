package org.scalaide.play2.lexical

import scala.collection.mutable.ListBuffer
import scala.math.max
import scala.math.min
import org.eclipse.jface.text._
import org.scalaide.util.eclipse.RegionUtils.RichTypedRegion

/**
 * Partitions the document according to given tokeniser
 */
abstract class PlayDocumentPartitioner(tokensiser: PlayPartitionTokeniser, protected val defaultPartition: String, conservative: Boolean = false) extends IDocumentPartitioner with IDocumentPartitionerExtension with IDocumentPartitionerExtension2 {

  protected var partitionRegions: List[TypedRegion] = Nil

  def connect(document: IDocument) {
    partitionRegions = tokensiser.tokenise(document)
  }

  def disconnect() {
    partitionRegions = Nil
  }

  def documentAboutToBeChanged(event: DocumentEvent) {}

  def documentChanged(event: DocumentEvent): Boolean = documentChanged2(event) != null

  def documentChanged2(event: DocumentEvent): IRegion = {
    val oldPartitions = partitionRegions
    val newPartitions = tokensiser.tokenise(event.getDocument)
    partitionRegions = newPartitions
    if (conservative)
      new Region(0, event.getDocument.getLength)
    else
      calculateDirtyRegion(oldPartitions, newPartitions, event.getOffset, event.getLength, event.getText)
  }

  private def calculateDirtyRegion(oldPartitions: List[TypedRegion], newPartitions: List[TypedRegion], offset: Int, length: Int, text: String): IRegion =
    if (newPartitions.isEmpty)
      new Region(0, 0)
    else if (oldPartitions == newPartitions)
      null
    else {
      // Scan outside-in from both the beginning and the end of the document to match up undisturbed partitions:
      val unchangedLeadingRegionCount = commonPrefixLength(oldPartitions, newPartitions)
      val adjustedOldPartitions =
        for (region <- oldPartitions if region.getOffset() > offset + length)
          yield region.shift(text.length - length)
      val unchangedTrailingRegionCount = commonPrefixLength(adjustedOldPartitions.reverse, newPartitions.reverse)
      val dirtyOldPartitionCount = oldPartitions.size - unchangedTrailingRegionCount - unchangedLeadingRegionCount
      val dirtyNewPartitionCount = newPartitions.size - unchangedTrailingRegionCount - unchangedLeadingRegionCount

      // A very common case is changing the size of a single partition, which we want to optimise:
      val singleDirtyPartitionWithUnchangedContentType = dirtyOldPartitionCount == 1 && dirtyNewPartitionCount == 1 &&
        oldPartitions(unchangedLeadingRegionCount).getType() == newPartitions(unchangedLeadingRegionCount).getType()
      if (singleDirtyPartitionWithUnchangedContentType)
        null
      else if (dirtyNewPartitionCount == 0) // i.e. a deletion of partitions
        new Region(offset, 0)
      else {
        // Otherwise just the dirty region:
        val firstDirtyPartition = newPartitions(unchangedLeadingRegionCount)
        val lastDirtyPartition = newPartitions(unchangedLeadingRegionCount + dirtyNewPartitionCount - 1)
        new Region(firstDirtyPartition.getOffset(), lastDirtyPartition.getOffset() + lastDirtyPartition.getLength() - firstDirtyPartition.getOffset())
      }
    }

  private def commonPrefixLength[X](xs: List[X], ys: List[X]) = xs.zip(ys).takeWhile(p => p._1 == p._2).size

  def getContentType(offset: Int) = getToken(offset) map { _.getType() } getOrElse defaultPartition

  private def getToken(offset: Int) = partitionRegions.find(_.containsPositionInclusive(offset))

  def computePartitioning(offset: Int, length: Int): Array[ITypedRegion] = {
    val regions = new ListBuffer[ITypedRegion]
    var searchingForStart = true
    for (partitionRegion <- partitionRegions)
      if (searchingForStart) {
        if (partitionRegion containsPositionInclusive offset) {
          searchingForStart = false
          regions += partitionRegion.crop(offset, length)
        }
      } else {
        if (partitionRegion.getOffset() > offset + length - 1)
          return regions.toArray
        else
          regions += partitionRegion.crop(offset, length)
      }
    regions.toArray
  }

<<<<<<< HEAD
||||||| merged common ancestors
  private def cropRegion(region: TypedRegion, offset: Int, length: Int): TypedRegion = {
    
    val newOffset = max(region.getOffset(), offset)
    val newLength = min(region.getOffset() + region.getLength(), offset + length) - newOffset
    
    new TypedRegion(newOffset, newLength, region.getType())
  }

=======
  private def cropRegion(region: TypedRegion, offset: Int, length: Int): TypedRegion = {

    val newOffset = max(region.getOffset(), offset)
    val newLength = min(region.getOffset() + region.getLength(), offset + length) - newOffset

    new TypedRegion(newOffset, newLength, region.getType())
  }

>>>>>>> Adapt to new Utils API
  def getPartition(offset: Int): ITypedRegion = getToken(offset) getOrElse {
    val surroundingTokens = ((null :: partitionRegions).sliding(2).find{ case List(_, r) => r.getOffset > offset }).map { case List(a,b) => (a,b) }
    surroundingTokens match {
      // Fill the gap between previous and next
      case Some((previousToken: TypedRegion, nextToken: TypedRegion)) => {
        val offset = previousToken.getOffset + previousToken.getLength
        new TypedRegion(offset, nextToken.getOffset - offset, defaultPartition)
      }
      // Fill the gap between beginning of doc and next
      case Some((null, r: TypedRegion)) =>
        new TypedRegion(0, r.getOffset, defaultPartition)
      // Either there are no tokens or end of file (in both cases we don't have a way of computing a length)
      case _ =>
        new TypedRegion(offset, 0, defaultPartition)
    }
  }


  def getManagingPositionCategories = null

  def getContentType(offset: Int, preferOpenPartitions: Boolean) = getPartition(offset, preferOpenPartitions).getType

  def getPartition(offset: Int, preferOpenPartitions: Boolean): ITypedRegion = {
    getPartition(offset)
  }

  def computePartitioning(offset: Int, length: Int, includeZeroLengthPartitions: Boolean) = computePartitioning(offset, length)

}


