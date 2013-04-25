package org.scalaide.play2.routeeditor.lexical

import scala.collection.mutable.ArrayBuffer
import scala.tools.eclipse.lexical.ScalaPartitionRegion
import org.scalaide.play2.lexical.PlayPartitionTokeniser
import RoutePartitions.ROUTE_ACTION
import RoutePartitions.ROUTE_COMMENT
import RoutePartitions.ROUTE_DEFAULT
import RoutePartitions.ROUTE_HTTP
import RoutePartitions.ROUTE_URI
import org.scalaide.editor.Util

class RoutePartitionTokeniser extends PlayPartitionTokeniser {
  import RoutePartitionTokeniser.EOF
  
  private def tokeniseEachLine(chars: Array[Char], line: ScalaPartitionRegion): List[ScalaPartitionRegion] = {
    val tokens = new ArrayBuffer[ScalaPartitionRegion]
    val ScalaPartitionRegion(_, start, end) = line
    var offset = start

    def ch(index: Int) = if (index <= end && index < chars.length) chars(index) else EOF

    @inline def checkBound = offset <= end

    def proceedComment() = { // this line is a comment line
      if (ch(offset) == '#') {
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
      while (checkBound && !isWhitespaceOrEOF(ch(offset))) offset += 1
      // if a word is found, then update the `startIndex` to the beginning of the word.
      // Otherwise, partition the whole line (which contains only whitespaces) as a ROUTE_HTTP.
      if(offset != offsetBeforeWord) startIndex = offsetBeforeWord
      if (offset >= startIndex) {
        val word = chars.subSequence(startIndex, offset).toString()
        tokens += ScalaPartitionRegion(ROUTE_HTTP, startIndex, offset)
        true
      } else {
        false
      }
    }

    def proceedURI() = {
      val startIndex = offset
      while (checkBound && !isWhitespaceOrEOF(ch(offset))) offset += 1
      if (offset > startIndex) {
        tokens += ScalaPartitionRegion(ROUTE_URI, startIndex, offset)
        true
      } else {
        false
      }
    }

    def proceedWhitespace() = {
      while (checkBound && Character.isWhitespace(ch(offset)) && ch(offset).toString != Util.defaultLineSeparator) offset += 1
    }

    def isWhitespaceOrEOF(ch: Char): Boolean = Character.isWhitespace(ch) || ch == EOF
    
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

  private def convertToSeperateLines(text: String): List[ScalaPartitionRegion] = {
    var isEOF = false
    var startIndex = 0
    val textLength = text.length
    val result = new ArrayBuffer[ScalaPartitionRegion]
    def getNextLine = {
      val index = text.indexOf(Util.defaultLineSeparator, startIndex)
      val start = startIndex
      val end = if (index != -1) {
        startIndex = index + 1
        index
      } else {
        isEOF = true
        textLength
      }
      ScalaPartitionRegion(ROUTE_DEFAULT, start, end)
    }
    while (!isEOF) {
      result += getNextLine
    }
    result.toList
  }

  def tokenise(text: String): List[ScalaPartitionRegion] = {
    val lines = convertToSeperateLines(text) 
    lines.flatMap(tokeniseEachLine(text.toCharArray, _))
  }

}

object RoutePartitionTokeniser {
  final val EOF = '\u001A' 
}