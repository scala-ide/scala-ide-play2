package org.scalaide.play2.templates23

import java.io.File

import scala.io.Codec
import scala.util.Failure
import scala.util.Try

import org.scalaide.play2.templateeditor.compiler.PositionHelper
import org.scalaide.play2.templateeditor.compiler.TemplateToScalaCompilationError
import org.scalaide.play2.templateeditor.processing.GeneratedSource

import play.twirl.compiler.TemplateCompilationError
import play.twirl.compiler.TwirlCompiler

object Template23Compiler {
  private val templateCompiler = TwirlCompiler

  def compile(content: String, source: File, sourceDirectory: File, additionalImports: String, inclusiveDot: Boolean): Try[GeneratedSource] =
    Try {
      templateCompiler.compileVirtual(
        content,
        source,
        sourceDirectory,
        "Html",
        "HtmlFormat",
        additionalImports,
        Codec.default,
        inclusiveDot)
    } map {
      Template23GeneratedSource
    } recoverWith {
      case TemplateCompilationError(source, message, line, column) =>
        val offset = PositionHelper.convertLineColumnToOffset(content, line, column)
        Failure(TemplateToScalaCompilationError(source, message, offset, line, column))
    }
}
