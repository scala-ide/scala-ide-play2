package org.scalaide.play2.templates24

import java.io.File

import scala.io.Codec
import scala.util.Try
import scala.util.parsing.input.Positional

import org.scalaide.play2.templateeditor.lexical.TemplateParsing.PlayTemplate
import org.scalaide.play2.templateeditor.processing.GeneratedSource
import org.scalaide.play2.templateeditor.processing.{ TemplateProcessing => PlayProcessing }

class TemplateProcessing extends PlayProcessing {
  def compile(content: String, source: File, sourceDirectory: File, additionalImports: String, codec: Codec, inclusiveDot: Boolean): Try[GeneratedSource] =
    Template24Compiler.compile(content, source, sourceDirectory, additionalImports, inclusiveDot)

  def parse(templateCode: String): List[PlayTemplate] = Template24Parser.parse(templateCode)

  def length(input: Positional): Int = Template24Parser.length(input)
}
