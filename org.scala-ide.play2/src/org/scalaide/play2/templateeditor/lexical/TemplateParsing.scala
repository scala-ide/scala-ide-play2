package org.scalaide.play2.templateeditor.lexical

import scala.util.parsing.input.CharSequenceReader
import scala.util.parsing.input.OffsetPosition
import scala.util.parsing.input.Positional

import org.scalaide.play2.templateeditor.processing.TemplateProcessingProvider

/**
 * A helper for using template parser
 */
object TemplateParsing {
  implicit def stringToCharSeq(str: String) = new CharSequenceReader(str)

  sealed abstract class PlayTemplate(input: Positional, kind: String) {
    val offset = input.pos match {
      case offsetPosition: OffsetPosition =>
        offsetPosition.offset
      case _ =>
        -1
    }
    val length = TemplateProcessingProvider.templateProcessing.length(input)

    override def toString = kind + "[" + offset + " - " + length + "]: " + input
  }
  case class ScalaCode(input: Positional) extends PlayTemplate(input, "sc")
  case class DefaultCode(input: Positional) extends PlayTemplate(input, "df")
  case class CommentCode(input: Positional) extends PlayTemplate(input, "cm")

  /**
   * Returns list of different types of region of the template code
   */
  def handleTemplateCode(templateCode: String) =
    TemplateProcessingProvider.templateProcessing.parse(templateCode)
}
