package org.scalaide.play2.templateeditor.compiler

import java.io.File
import scala.util.Failure
import scala.util.Try

import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities
import org.scalaide.logging.HasLogger
import org.scalaide.play2.PlayProject
import play.twirl.compiler.GeneratedSourceVirtual
import play.twirl.compiler.TwirlCompiler
import play.twirl.compiler.TwirlCompiler._
import play.twirl.compiler.TemplateCompilationError
import org.scalaide.play2.properties.PlayPreferences
import org.scalaide.logging.HasLogger
import scala.io.Codec
/**
 * a helper for using template compiler
 */
object CompilerUsing extends HasLogger {
  val templateCompiler = TwirlCompiler
  val defaultTemplateImports = """
import models._
import controllers._
import play.api.i18n._
import play.api.mvc._
import play.api.data._
import views.html._
"""

  /**
   * invokes compile method of template compiler and returns generated source object or
   * in the case of error, returns appropriate exception
   */
  def compileTemplateToScalaVirtual(content: String, source: File, playProject: PlayProject, inclusiveDot: Boolean): Try[GeneratedSourceVirtual] = {
    val sourcePath = playProject.sourceDir.getAbsolutePath()
    if (source.getAbsolutePath().indexOf(sourcePath) == -1)
      logger.debug(s"Template file '${source.getAbsolutePath}' must be located in '$sourcePath' or one of its subfolders!")

    val extension = source.getName.split('.').last

    Try {
      templateCompiler.compileVirtual(
        content,
        source,
        playProject.sourceDir,
        "play.api.templates.Html",
        "play.api.templates.HtmlFormat",
        defaultTemplateImports + playProject.additionalTemplateImports(extension),
        Codec.default,
        inclusiveDot
      )
    } recoverWith {
      case TemplateCompilationError(source, message, line, column) =>
        val offset = PositionHelper.convertLineColumnToOffset(content, line, column)
        Failure(TemplateToScalaCompilationError(source, message, offset, line, column))
      case ex: Exception => {
        val error = s"Caught unknown exception: '${ex.getMessage()}'\n${ex.getStackTraceString}"
        Failure(TemplateToScalaCompilationError(source, error, 0, 0, 0))
      }
    }
  }

}

case class TemplateToScalaCompilationError(source: File, message: String, offset: Int, line: Int, column: Int) extends RuntimeException(message) {
  override def toString = source.getName + ": " + message + offset + " " + line + "." + column

  import org.eclipse.jdt.core.compiler.IProblem
  import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities
  import org.eclipse.jdt.internal.compiler.problem.DefaultProblem

  def toProblem: IProblem = {
    val severityLevel = ProblemSeverities.Error
    new DefaultProblem(
      source.getAbsolutePath().toCharArray,
      message,
      0,
      Array.empty[String],
      severityLevel,
      Math.max(offset - 1, 0),
      Math.max(offset - 1, 0),
      line,
      column)
  }
}

object PositionHelper {
  def convertLineColumnToOffset(source: File, line: Int, column: Int): Int = {
    convertLineColumnToOffset(scala.io.Source.fromFile(source).mkString, line, column)
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
