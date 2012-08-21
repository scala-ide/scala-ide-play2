package org.scalaide.play2.templateeditor.compiler

import play.templates.ScalaTemplateCompiler
import play.templates.ScalaTemplateCompiler._
import java.io.File
import play.templates.GeneratedSource
import play.templates.TemplateCompilationError
import scalax.file.Path
import org.scalaide.play2.PlayProject

object CompilerUsing{
  val templateCompiler = ScalaTemplateCompiler
  val additionalImports = """import play.api.templates._
import play.api.templates.PlayMagic._"""

  def compileTemplateToScala(templateFile: File, playProject: PlayProject) = {
    import playProject.{generatedClasses, sourceDir, generatedDir}
    
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

  def compile(templateName: String, playProject: PlayProject) = {
    import playProject.sourceDir
    val templateFile = new File(sourceDir, templateName)
    compileTemplateToScala(templateFile, playProject)
  }

  def main(args: Array[String]): Unit = {
    val playProject = PlayProject(null)
    val result = compile("a1.scala.html", playProject)
    val result2 = compile("a2.scala.html", playProject)
    println(result.matrix)
    println(result.file.getAbsolutePath())
    println(result2.matrix)
    TemplateAsFunctionCompiler.CompilerInstance.compiler.askShutdown
  }

}

case class TemplateToScalaCompilationError(source: File, message: String, offset: Int, line: Int, column: Int) extends RuntimeException(message) {
  override def toString = source.getName + ": " + message + offset + " " + line + "." + column
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