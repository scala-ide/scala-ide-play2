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
    import playProject.{generatedClasses, /*sourceDir, */generatedDir}
    val sourceDir = templateFile.getParentFile()
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
//    val result = compile("a1.scala.html", playProject)
    val result = compileTemplateToScala(new File("/Users/shaikhha/Documents/workspace-new/asd/a1.scala.html"), playProject)
    val result2 = compile("a2.scala.html", playProject)
    println(result.matrix)
    println(PositionHelper.mapSourcePosition(result.matrix, 58))
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
    // splitting the string will cause some problems
    var offset = 0
    for (i <- 1 until line) {
      offset = content.indexOf("\n", offset) + 1
    }
    offset += column - 1
    offset
  }
  
  def mapSourcePosition(matrix: Seq[(Int, Int)], sourcePosition: Int): Int = {
      matrix.indexWhere(p => p._2 > sourcePosition) match {
        case 0 => 0
        case i if i > 0 => {
          val pos = matrix(i - 1)
          pos._1 + (sourcePosition - pos._2)
        }
        case _ => {
          val pos = matrix.takeRight(1)(0)
          pos._1 + (sourcePosition - pos._2)
        }
      }
    }
}