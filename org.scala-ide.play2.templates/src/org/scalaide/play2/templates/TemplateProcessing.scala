package org.scalaide.play2.templates

import java.io.File

import scala.io.Codec
import scala.util.Try
import scala.util.parsing.input.Positional

import org.scalaide.play2.templateeditor.lexical.TemplateParsing.PlayTemplate
import org.scalaide.play2.templateeditor.processing.GeneratedSource
import org.scalaide.play2.templateeditor.processing.{ TemplateProcessing => PlayProcessing }

class TemplateProcessing extends PlayProcessing {
  def compile(content: String, source: File, sourceDirectory: File, additionalImports: String, codec: Codec, inclusiveDot: Boolean): Try[GeneratedSource] =
    TemplateCompiler.compile(content, source, sourceDirectory, additionalImports, inclusiveDot)

  def parse(templateCode: String): List[PlayTemplate] = TemplateParser.parse(templateCode)

  def length(input: Positional): Int = TemplateParser.length(input)
}
