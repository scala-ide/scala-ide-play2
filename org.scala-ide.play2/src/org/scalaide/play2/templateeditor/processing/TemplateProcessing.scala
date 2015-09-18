package org.scalaide.play2.templateeditor.processing

import java.io.File

import scala.io.Codec
import scala.util.Try
import scala.util.parsing.input.Positional

import org.scalaide.play2.templateeditor.lexical.TemplateParsing.PlayTemplate

/**
 * Thought as the interface to template parser and compiler as Twirl library does not define
 * the abstraction for these two functionalities.
 */
trait TemplateProcessing {
  /**
   * Returns list of different types of region of the template code
   */
  def parse(templateCode: String): List[PlayTemplate]
  /**
   * Gets length of parsed token
   */
  def length(input: Positional): Int

  /**
   * invokes compile method of template compiler and returns generated source object or
   * in the case of error, returns appropriate exception
   */
  def compile(content: String, source: File, sourceDirectory: File, additionalImports: String, codec: Codec, inclusiveDot: Boolean): Try[GeneratedSource]
}

trait GeneratedSource {
  def content: String
  def matrix: Seq[(Int, Int)]
  def mapPosition(generatedPosition: Int): Int
}
