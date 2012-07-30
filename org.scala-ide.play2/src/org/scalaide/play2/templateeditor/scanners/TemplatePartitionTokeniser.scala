package org.scalaide.play2.templateeditor.scanners

import org.eclipse.jface.text._
import org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE
import scala.annotation.{ switch, tailrec }
import scala.collection.mutable.{ Stack, ListBuffer }
import scala.xml.parsing.TokenTests
import scala.tools.eclipse.lexical.ScalaPartitionRegion

object TemplatePartitionTokeniser {

  def tokenise(text: String): List[ScalaPartitionRegion] = {
    val tokens = new ListBuffer[ScalaPartitionRegion]
    val tokeniser = new TemplatePartitionTokeniser(text)
    while (tokeniser.tokensRemain) {
      val nextToken = tokeniser.nextToken()
      tokens += nextToken
    }
    tokens.toList
  }

}

class TemplatePartitionTokeniser(text: String) {
  import TemplateDocumentPartitioner.EOF

  private val contentArr = TemplatePartitions.getTypes
  private val iterations = contentArr.length
  private val lengthOfToken = text.length / iterations

  private var pos = 0

  def tokensRemain = pos < iterations

  def nextToken(): ScalaPartitionRegion = {

    val contentType = contentArr(pos)
    val tokenStart = lengthOfToken * pos
    val tokenEnd = lengthOfToken * (pos + 1) - 1
    pos += 1
    ScalaPartitionRegion(contentType, tokenStart, tokenEnd)
  }

}
