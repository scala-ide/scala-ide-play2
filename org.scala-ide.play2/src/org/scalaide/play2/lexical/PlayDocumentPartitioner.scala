package org.scalaide.play2.lexical

import scala.collection.mutable.ListBuffer
import scala.math.max
import scala.math.min
import scala.tools.eclipse.lexical.ScalaPartitionRegion
import org.eclipse.jface.text._

/**
 * Partitions the document according to given tokeniser
 */
abstract class PlayDocumentPartitioner(tokensiser: PlayPartitionTokeniser, protected val defaultPartition: String, conservative: Boolean = false) extends IDocumentPartitioner with IDocumentPartitionerExtension with IDocumentPartitionerExtension2 {

  private var partitionRegions: List[ScalaPartitionRegion] = Nil

  def connect(document: IDocument) {
    partitionRegions = tokensiser.tokenise(document.get)
  }

  def disconnect() {
    partitionRegions = Nil
  }

  def documentAboutToBeChanged(event: DocumentEvent) {}

  def documentChanged(event: DocumentEvent): Boolean = documentChanged2(event) != null

  def documentChanged2(event: DocumentEvent): IRegion = {
    val oldPartitions = partitionRegions
    val newPartitions = tokensiser.tokenise(event.getDocument.get)
    partitionRegions = newPartitions
    if (conservative)
      new Region(0, event.getDocument.getLength)
    else
      calculateDirtyRegion(oldPartitions, newPartitions, event.getOffset, event.getLength, event.getText)
  }

  private def calculateDirtyRegion(oldPartitions: List[ScalaPartitionRegion], newPartitions: List[ScalaPartitionRegion], offset: Int, length: Int, text: String): IRegion =
    if (newPartitions.isEmpty)
      new Region(0, 0)
    else if (oldPartitions == newPartitions)
      null
    else {
      // Scan outside-in from both the beginning and the end of the document to match up undisturbed partitions:
      val unchangedLeadingRegionCount = commonPrefixLength(oldPartitions, newPartitions)
      val adjustedOldPartitions =
        for (region <- oldPartitions if region.start > offset + length)
          yield region.shift(text.length - length)
      val unchangedTrailingRegionCount = commonPrefixLength(adjustedOldPartitions.reverse, newPartitions.reverse)
      val dirtyOldPartitionCount = oldPartitions.size - unchangedTrailingRegionCount - unchangedLeadingRegionCount
      val dirtyNewPartitionCount = newPartitions.size - unchangedTrailingRegionCount - unchangedLeadingRegionCount

      // A very common case is changing the size of a single partition, which we want to optimise:
      val singleDirtyPartitionWithUnchangedContentType = dirtyOldPartitionCount == 1 && dirtyNewPartitionCount == 1 &&
        oldPartitions(unchangedLeadingRegionCount).contentType == newPartitions(unchangedLeadingRegionCount).contentType
      if (singleDirtyPartitionWithUnchangedContentType)
        null
      else if (dirtyNewPartitionCount == 0) // i.e. a deletion of partitions
        new Region(offset, 0)
      else {
        // Otherwise just the dirty region:
        val firstDirtyPartition = newPartitions(unchangedLeadingRegionCount)
        val lastDirtyPartition = newPartitions(unchangedLeadingRegionCount + dirtyNewPartitionCount - 1)
        new Region(firstDirtyPartition.start, lastDirtyPartition.end - firstDirtyPartition.start + 1)
      }
    }

  private def commonPrefixLength[X](xs: List[X], ys: List[X]) = xs.zip(ys).takeWhile(p => p._1 == p._2).size

  def getLegalContentTypes: Array[String]

  def getContentType(offset: Int) = getToken(offset) map { _.contentType } getOrElse defaultPartition

  private def getToken(offset: Int) = partitionRegions.find(_.containsPosition(offset))

  def computePartitioning(offset: Int, length: Int): Array[ITypedRegion] = {
    val regions = new ListBuffer[ITypedRegion]
    var searchingForStart = true
    for (partitionRegion <- partitionRegions)
      if (searchingForStart) {
        if (partitionRegion containsPosition offset) {
          searchingForStart = false
          regions += cropRegion(partitionRegion, offset, length)
        }
      } else {
        if (partitionRegion.start > offset + length - 1)
          return regions.toArray
        else
          regions += cropRegion(partitionRegion, offset, length)
      }
    regions.toArray
  }

  private def cropRegion(region: ScalaPartitionRegion, offset: Int, length: Int): ScalaPartitionRegion = {
    val ScalaPartitionRegion(_, start, end) = region
    if (start > offset + length - 1 || end < offset)
      region
    else
      region.copy(start = max(start, offset), end = min(end, offset + length - 1))
  }

  def getPartition(offset: Int): ITypedRegion = getToken(offset) getOrElse new TypedRegion(offset, 1, defaultPartition)

  def getManagingPositionCategories = null

  def getContentType(offset: Int, preferOpenPartitions: Boolean) = getPartition(offset, preferOpenPartitions).getType

  def getPartition(offset: Int, preferOpenPartitions: Boolean): ITypedRegion = {
    getPartition(offset)
  }

  def computePartitioning(offset: Int, length: Int, includeZeroLengthPartitions: Boolean) = computePartitioning(offset, length)

}


