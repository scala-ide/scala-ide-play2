package org.scalaide.play2.templates

import java.io.File

import scala.util.Failure
import scala.util.Try

import org.scalaide.play2.properties.PlayPreferences
import org.scalaide.play2.templateeditor.compiler.PositionHelper
import org.scalaide.play2.templateeditor.compiler.TemplateToScalaCompilationError
import org.scalaide.play2.templateeditor.processing.GeneratedSource

import play.twirl.compiler.TemplateCompilationError
import play.twirl.compiler.TwirlCompiler

object TemplateCompiler {
  private val templateCompiler = TwirlCompiler

  def compile(content: String, source: File, sourceDirectory: File, additionalImports: String, inclusiveDot: Boolean): Try[GeneratedSource] =
    Try {
      templateCompiler.compileVirtual(
        content,
        source,
        sourceDirectory,
        "Html",
        "HtmlFormat",
        PlayPreferences.deserializeImports(additionalImports),
        inclusiveDot = inclusiveDot)
    } map {
      TemplateGeneratedSource
    } recoverWith {
      case TemplateCompilationError(source, message, line, column) =>
        val offset = PositionHelper.convertLineColumnToOffset(content, line, column)
        Failure(TemplateToScalaCompilationError(source, message, offset, line, column))
    }
}
