package org.scalaide.play2.routeeditor.lexical

import scala.collection.mutable.ArrayBuffer
import scala.tools.eclipse.lexical.ScalaPartitionRegion

import org.scalaide.play2.lexical.PlayPartitionTokeniser

import RoutePartitions.ROUTE_ACTION
import RoutePartitions.ROUTE_COMMENT
import RoutePartitions.ROUTE_DEFAULT
import RoutePartitions.ROUTE_HTTP
import RoutePartitions.ROUTE_URI

object RoutePartitionTokeniser extends PlayPartitionTokeniser {

  def tokeniseEachLine(chars: Array[Char], line: ScalaPartitionRegion): List[ScalaPartitionRegion] = {
    val tokens = new ArrayBuffer[ScalaPartitionRegion]
    val ScalaPartitionRegion(_, start, end) = line
    var offset = start

    def ch(index: Int) = if (index <= end) chars(index) else RouteDocumentPartitioner.EOF

    @inline def checkBound = offset <= end

    def proceedComment = { // this line is a comment line
      if (ch(offset) == '#') {
        tokens += ScalaPartitionRegion(ROUTE_COMMENT, start, end)
        true
      } else {
        false
      }
    }

    def proceedAction {
      if (end > offset) {
        tokens += ScalaPartitionRegion(ROUTE_ACTION, offset, end)
      }
    }

    def proceedHTTPVerb = {
      val startIndex = offset
      while (checkBound && !Character.isWhitespace(ch(offset))) offset += 1
      if (offset > startIndex) {
        val word = chars.subSequence(startIndex, offset).toString()
        val contentType =
          if (HTTPKeywords.words.exists(_ equals word)) {
            ROUTE_HTTP
          } else { ROUTE_DEFAULT }
        tokens += ScalaPartitionRegion(contentType, startIndex, offset - 1)
        true
      } else {
        false
      }
    }

    def proceedURI = {
      val startIndex = offset
      while (checkBound && !Character.isWhitespace(ch(offset))) offset += 1
      if (offset > startIndex) {
        tokens += ScalaPartitionRegion(ROUTE_URI, startIndex, offset - 1)
        true
      } else {
        false
      }
    }

    def proceedWhitespace = {
      while (checkBound && Character.isWhitespace(ch(offset))) offset += 1
    }

    if (!proceedComment) { // this line is not comment line
      var startIndex = start
      if (proceedHTTPVerb) {
        proceedWhitespace
        if (proceedURI) {
          proceedWhitespace
          proceedAction
        }
      }
    }
    tokens.toList
  }

  def convertToSeperateLines(text: String): List[ScalaPartitionRegion] = {
    var startIndex = 0
    val textLength = text.length
    val result = new ArrayBuffer[ScalaPartitionRegion]
    def getNextLine = {
      val index = text.indexOf("\n", startIndex)
      val start = startIndex
      val end = if (index != -1) {
        startIndex = index + 1
        index - 1
      } else {
        startIndex = textLength
        textLength - 1
      }
      ScalaPartitionRegion(ROUTE_DEFAULT, start, end)
    }
    while (startIndex < textLength) {
      result += getNextLine
    }
    result.toList
  }

  def tokenise(text: String): List[ScalaPartitionRegion] = {
    val lines = convertToSeperateLines(text)
    lines.flatMap(tokeniseEachLine(text.toCharArray, _))
  }

}
