package org.scalaide.play2.templateeditor.scanners

import org.eclipse.jface.text._
import org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE
import scala.annotation.{ switch, tailrec }
import scala.collection.mutable.{ Stack, ListBuffer }
import scala.xml.parsing.TokenTests
import scala.tools.eclipse.lexical.ScalaPartitionRegion
import scala.tools.eclipse.lexical.ScalaPartitionTokeniser
import scala.tools.eclipse.lexical.ScalaPartitions
import scala.util.parsing.input.OffsetPosition

object TemplatePartitionTokeniser {

  def tokenise(text: String): List[ScalaPartitionRegion] = {
    val parts = TemplateParsing.handleTemplateCode(text)
    import TemplateParsing._
    var prevOffset = 0
    type ListRegion = List[ScalaPartitionRegion]
    type Elem = Either[ScalaPartitionRegion, ListRegion]
    val notFlattenedtokens: List[Elem] = parts.map(t => {
      val contentType = t match {
        case ScalaCode(_) => TemplatePartitions.TEMPLATE_SCALA
        case DefaultCode(_) => TemplatePartitions.TEMPLATE_PLAIN
        case CommentCode(_) => TemplatePartitions.TEMPLATE_COMMENT
      }
      // TODO a bit of hack for comment part. it should be removed!
      if (contentType != TemplatePartitions.TEMPLATE_COMMENT) { 
        prevOffset = t.length + t.offset
        if (contentType == TemplatePartitions.TEMPLATE_PLAIN) {
          t match {
            case DefaultCode(p) => {
              val code = p.text
              val offset = p.pos.asInstanceOf[OffsetPosition].offset
              val toks = ScalaPartitionTokeniser.tokenise(code)
              val newToks = toks.map(t => {
                val contType = {
                  if (t.contentType == ScalaPartitions.XML_TAG)
                    TemplatePartitions.TEMPLATE_TAG
                  else
                    TemplatePartitions.TEMPLATE_PLAIN
                }
                ScalaPartitionRegion(contType, t.start + offset, t.end + offset)
              })
              Right(newToks)
            }
            case _ => throw new RuntimeException("impossible!!")
          }
        } else
          Left(ScalaPartitionRegion(contentType, t.offset, t.length + t.offset - 1))
      } else {
        val offset = text.indexOf("@*", prevOffset)
        prevOffset = offset + t.length
        Left(ScalaPartitionRegion(contentType, offset, offset + t.length - 1))
      }
    })
    val tokens = notFlattenedtokens.foldLeft[ListRegion](Nil)((prev, e) => {
      e match {
        case Left(x) => prev ::: List(x) // e is not list
        case Right(x) => prev ::: x // e is list
      }
    })
    //    tokens.flatten
    val new_tokens = if (tokens.isEmpty) {
      Nil
    } else {
      val start = tokens.head.start
      if (start != 0) {
        ScalaPartitionRegion(TemplatePartitions.TEMPLATE_DEFAULT, 0, start - 1) :: tokens
      } else {
        tokens
      }
    }
    new_tokens.filter(_.start != -1).sort((a, b) => a.start < b.start)
  }

}

//object TemplatePartitionTokeniser {
//	
//	def tokenise(text: String): List[ScalaPartitionRegion] = {
//			val tokens = new ListBuffer[ScalaPartitionRegion]
//					val tokeniser = new TemplatePartitionTokeniser(text)
//			while (tokeniser.tokensRemain) {
//				val nextToken = tokeniser.nextToken()
//						tokens += nextToken
//			}
//			tokens.toList
//	}
//	
//}
//
//class TemplatePartitionTokeniser(text: String) {
//	import TemplateDocumentPartitioner.EOF
//	
//	private val contentArr = TemplatePartitions.getTypes
//	private val iterations = contentArr.length
//	private val lengthOfToken = text.length / iterations
//	
//	private var pos = 0
//	
//	def tokensRemain = pos < iterations
//	
//	def nextToken(): ScalaPartitionRegion = {
//			
//			val contentType = contentArr(pos)
//					val tokenStart = lengthOfToken * pos
//					val tokenEnd = lengthOfToken * (pos + 1) - 1
//					pos += 1
//					ScalaPartitionRegion(contentType, tokenStart, tokenEnd)
//	}
//	
//}
