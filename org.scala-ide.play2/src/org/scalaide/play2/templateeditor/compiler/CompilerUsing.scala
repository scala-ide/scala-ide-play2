package org.scalaide.play2.templateeditor.compiler

import play.templates.ScalaTemplateCompiler
import play.templates.ScalaTemplateCompiler._
import java.io.File
import play.templates.GeneratedSource
import play.templates.TemplateCompilationError
import scala.util.parsing.input.OffsetPosition
import scalax.file.Path

object CompilerUsing {
  lazy val sourceDir = new File("app/views")
  val generatedDir = new File("target/test/generated-templates")
  val generatedClasses = new File("target/test/generated-classes")
  val templateCompiler = ScalaTemplateCompiler
  val additionalImports = """import play.api.templates._
import play.api.templates.PlayMagic._"""

  def compileTemplateToScala(templateName: String) = {
    val templateFile = new File(sourceDir, templateName)
    try {
      templateCompiler.compile(templateFile, sourceDir, generatedDir, "play.api.templates.Html", "play.api.templates.HtmlFormat", additionalImports) match {
        case Some(generated) => GeneratedSource(generated)
        case _ => generatedFile(templateFile, sourceDir, generatedDir)._2
      }
    } catch {
      case TemplateCompilationError(source: File, message: String, line: Int, column: Int) =>
        val offset = PositionHelper.convertLineColumnToOffset(source, line, column)
        throw new TemplateToScalaCompilationError(source, message, offset, line, column)
    }
  }

  def compile(templateName: String) = {
    compileTemplateToScala(templateName)
  }

  def main(args: Array[String]): Unit = {
    val result = compile("index.scala.html")
    //    val result2 = compile("main.scala.html")
    println(result.matrix)
    //    println(result2.matrix)
    TemplateAsFunctionCompiler.CompilerInstance.compiler.askShutdown
  }

}

case class TemplateToScalaCompilationError(source: File, message: String, offset: Int, line: Int, column: Int) extends RuntimeException(message) {
  override def toString = source.getName + ": " + message + offset + " " + line + "."+column
}

object PositionHelper {
  def convertLineColumnToOffset(source: File, line: Int, column: Int) = {
    val content = Path(source).slurpString
    val lines = content.split("\n")
    var offset = column
    for (i <- 1 until line) {
      offset += lines(i - 1).length + 1
    }
    offset
  }
}