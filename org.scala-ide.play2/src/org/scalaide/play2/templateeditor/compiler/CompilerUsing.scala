package org.scalaide.play2.templateeditor.compiler

import play.templates.ScalaTemplateCompiler
import play.templates.ScalaTemplateCompiler._
import java.io.File
import play.templates.GeneratedSource
import play.templates.TemplateCompilationError
import scalax.file.Path
import org.scalaide.play2.PlayProject
/**
 * a helper for using template compiler
 */
object CompilerUsing {
  val templateCompiler = ScalaTemplateCompiler
  val additionalImports = """import play.templates._
import play.templates.TemplateMagic._
    
    
import play.api.templates._
import play.api.templates.PlayMagic._
import models._
import controllers._
import play.api.i18n._
import play.api.mvc._
import play.api.data._
import views.html._"""

  /**
   * invokes compile method of template compiler and returns generated source object or
   * in the case of error, returns appropriate exception
   */
  def compileTemplateToScalaVirtual(content: String, source: File, playProject: PlayProject) = {
    val sourcePath = playProject.sourceDir.getAbsolutePath()
    if (source.getAbsolutePath().indexOf(sourcePath) == -1)
      throw new Exception("Template files must locate in '" + sourcePath + "' or its subfolders!")
    try {
      templateCompiler.compileVirtual(content, source, playProject.sourceDir, "play.api.templates.Html", "play.api.templates.HtmlFormat", additionalImports)
    } catch {
      case e @ TemplateCompilationError(source: File, message: String, line: Int, column: Int) =>
        val offset = PositionHelper.convertLineColumnToOffset(content, line, column)
        throw new TemplateToScalaCompilationError(source, message, offset, line, column)
    }
  }

}

case class TemplateToScalaCompilationError(source: File, message: String, offset: Int, line: Int, column: Int) extends RuntimeException(message) {
  override def toString = source.getName + ": " + message + offset + " " + line + "." + column
}

object PositionHelper {
  def convertLineColumnToOffset(source: File, line: Int, column: Int): Int = {
    convertLineColumnToOffset(Path(source).string, line, column)
  }

  def convertLineColumnToOffset(content: String, line: Int, column: Int): Int = {
    // splitting the string will cause some problems
    var offset = 0
    for (i <- 1 until line) {
      offset = content.indexOf("\n", offset) + 1
    }
    offset += column - 1
    offset
  }

  def mapSourcePosition(matrix: Seq[(Int, Int)], sourcePosition: Int): Int = {
    val sortedMatrix = matrix.sortBy(_._2)
    sortedMatrix.indexWhere(p => p._2 > sourcePosition) match {
      case 0 => 0
      case i if i > 0 => {
        val pos = sortedMatrix(i - 1)
        pos._1 + (sourcePosition - pos._2)
      }
      case _ => {
        val pos = sortedMatrix.takeRight(1)(0)
        pos._1 + (sourcePosition - pos._2)
      }
    }
  }
}