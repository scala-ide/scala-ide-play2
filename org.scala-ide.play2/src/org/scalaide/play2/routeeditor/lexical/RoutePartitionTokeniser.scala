package org.scalaide.play2.routeeditor.lexical

import scala.collection.mutable.ArrayBuffer
import org.eclipse.jface.text.TypedRegion
import org.scalaide.play2.lexical.PlayPartitionTokeniser
import RoutePartitions.ROUTE_ACTION
import RoutePartitions.ROUTE_COMMENT
import RoutePartitions.ROUTE_DEFAULT
import RoutePartitions.ROUTE_HTTP
import RoutePartitions.ROUTE_URI
import org.eclipse.jface.text.IDocument
import scala.annotation.tailrec
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions

class RoutePartitionTokeniser extends PlayPartitionTokeniser {

  private def tokeniseEachLine(document: IDocument, line: TypedRegion): List[TypedRegion] = {
    val tokens = new ArrayBuffer[TypedRegion]
    val start = line.getOffset()
    val end = start + line.getLength()
    var offset = start

    def charAt(offset: Int): Char = document.getChar(offset)

    @inline def checkBound = offset < end

    def proceedComment() = { // this line is a comment line
      if (checkBound && charAt(offset) == '#') {
        tokens += new TypedRegion(start, end - start, ROUTE_COMMENT)
        true
      } 
      else false
    }

    def proceed(partitionType: String): Boolean = {
      // Increment offset to split partitions. The increment is not needed for the first column (i.e., ROUTE_HTTP), of course.
      if (partitionType == ROUTE_URI) offset += 1

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
        tokens += new TypedRegion(startIndex, length, partitionType)
        true
      }
      else false
    }
    
    def proceedAction() {
      val startIndex = offset + 1
      
      proceedWhitespace()
      
      if (offset == end) {
        // only white spaces, return everything
        tokens += new TypedRegion(startIndex, end - startIndex, ROUTE_ACTION)
      } else {
        // everything remaining but the leading and trailing whitespace is part of the action
        tokens += new TypedRegion(offset, firstTrailingWhitespace(offset) - offset, ROUTE_ACTION)
      }
      
    }

    def proceedWhitespace() = {
      while (checkBound && Character.isWhitespace(charAt(offset))) offset += 1
    }
    
    def firstTrailingWhitespace(upTo: Int): Int = {
      @tailrec
      def find(current: Int): Int = {
        if (current <= upTo) {
          upTo
        } else if (Character.isWhitespace(charAt(current - 1))) {
          find(current - 1)
        } else {
          current
        }
      }
      find(end)
    }

    if (!proceedComment()) { // this line is not comment line
      if (proceed(ROUTE_HTTP)) {
        if (checkBound && proceed(ROUTE_URI)) {
          if(checkBound) proceedAction()
        }
      }
    }
    tokens.toList
  }

  private def convertToSeperateLines(document: IDocument): List[TypedRegion] = {
    (for (line <- 0 until document.getNumberOfLines) yield {
      val region = document.getLineInformation(line)
      new TypedRegion(region.getOffset(), region.getLength(), ROUTE_DEFAULT)
    })(collection.breakOut)
  }

  override def tokenise(document: IDocument): List[TypedRegion] =
    convertToSeperateLines(document).flatMap(tokeniseEachLine(document, _))
 
}