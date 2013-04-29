package org.scalaide.play2.routeeditor.lexical

import scala.collection.mutable.ArrayBuffer
import scala.tools.eclipse.lexical.ScalaPartitionRegion
import org.scalaide.play2.lexical.PlayPartitionTokeniser
import RoutePartitions.ROUTE_ACTION
import RoutePartitions.ROUTE_COMMENT
import RoutePartitions.ROUTE_DEFAULT
import RoutePartitions.ROUTE_HTTP
import RoutePartitions.ROUTE_URI
import org.eclipse.jface.text.IDocument

class RoutePartitionTokeniser extends PlayPartitionTokeniser {

  private def tokeniseEachLine(document: IDocument, line: ScalaPartitionRegion): List[ScalaPartitionRegion] = {
    val tokens = new ArrayBuffer[ScalaPartitionRegion]
    val ScalaPartitionRegion(_, start, end) = line
    var offset = start

    def charAt(offset: Int): Char = document.getChar(offset)

    @inline def checkBound = offset < end

    def proceedComment() = { // this line is a comment line
      if (checkBound && charAt(offset) == '#') {
        tokens += ScalaPartitionRegion(ROUTE_COMMENT, start, end)
        true
      } else {
        false
      }
    }

    def proceedAction() {
      if (end > offset) {
        tokens += ScalaPartitionRegion(ROUTE_ACTION, offset, end)
      }
    }

    def proceedHTTPVerb() = {
      var startIndex = offset
      // skip any leading whitespaces
      proceedWhitespace()
      val offsetBeforeWord = offset
      while (checkBound && !Character.isWhitespace(charAt(offset))) offset += 1
      // if a word is found, then update the `startIndex` to the beginning of the word.
      // Otherwise, partition the whole line (which contains only whitespaces) as a ROUTE_HTTP.
      if(offset != offsetBeforeWord) startIndex = offsetBeforeWord
      if (offset >= startIndex) {
        val length = offset - startIndex
        val word = document.get(startIndex, length)
        tokens += ScalaPartitionRegion(ROUTE_HTTP, startIndex, offset)
        true
      } else {
        false
      }
    }

    def proceedURI() = {
      val startIndex = offset
      while (checkBound && !Character.isWhitespace(charAt(offset))) offset += 1
      if (offset > startIndex) {
        tokens += ScalaPartitionRegion(ROUTE_URI, startIndex, offset)
        true
      } else {
        false
      }
    }

    def proceedWhitespace() = {
      while (checkBound && Character.isWhitespace(charAt(offset))) offset += 1
    }
    
    if (!proceedComment()) { // this line is not comment line
      if (proceedHTTPVerb()) {
        proceedWhitespace()
        if (proceedURI()) {
          proceedWhitespace()
          proceedAction()
        }
      }
    }
    tokens.toList
  }

  private def convertToSeperateLines(document: IDocument): List[ScalaPartitionRegion] = {
    (for (line <- 0 until document.getNumberOfLines) yield {
      val region = document.getLineInformation(line)
      ScalaPartitionRegion(ROUTE_DEFAULT, region.getOffset(), region.getOffset() + region.getLength())
    })(collection.breakOut)
  }

  override def tokenise(document: IDocument): List[ScalaPartitionRegion] = {
    val lines = convertToSeperateLines(document)
    lines.flatMap(tokeniseEachLine(document, _))
  }
}