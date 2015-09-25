package org.scalaide.play2.templateeditor.processing

import java.io.File

import scala.io.Codec
import scala.util.Try
import scala.util.parsing.input.Positional

import org.scalaide.play2.templateeditor.lexical.TemplateParsing.PlayTemplate

/**
 * Thought as the interface to template parser and compiler as long as Twirl library does not define
 * the abstraction for these two functionalities. Implemented by extensions of [[TemplateProcessingProvider.ExtensionPointId]]
 */
trait TemplateProcessing {
  /**
   * Parses and returns list of different types of region of the template code.
   * @param templateCode template code to parse
   * @return a list of [[PlayTemplate]] objects
   */
  def parse(templateCode: String): List[PlayTemplate]

  /**
   * Gets length of parsed token.
   * @param token as [[Positional]]
   * @return length of token
   */
  def length(input: Positional): Int

  /**
   * Invokes compile method of template compiler and returns generated source object or
   * in the case of error, returns appropriate exception.
   * @param content with template code
   * @param source keeps the template file
   * @param sourceDirectory
   * @param additionalImports
   * @param codec to decode charset used in template
   * @param inclusiveDot
   * @return generated scala source
   */
  def compile(content: String, source: File, sourceDirectory: File, additionalImports: String, codec: Codec, inclusiveDot: Boolean): Try[GeneratedSource]
}

trait GeneratedSource {
  def content: String
  def matrix: Seq[(Int, Int)]
  def mapPosition(generatedPosition: Int): Int
}
