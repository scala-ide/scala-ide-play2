package org.scalaide.play2.templateeditor.compiler

import org.eclipse.jdt.core.compiler.IProblem
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem
import scala.tools.nsc.util.SourceFile
import scala.tools.nsc.util.BatchSourceFile
import play.twirl.compiler.GeneratedSource
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualFile
import java.io.File
import org.scalaide.core.compiler.ScalaPresentationCompiler
import play.twirl.compiler.GeneratedSourceVirtual
import org.scalaide.logging.HasLogger
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.collection.mutable
import org.scalaide.play2.PlayProject

/**
 * presentation compiler for template files
 */
class TemplatePresentationCompiler(playProject: PlayProject) extends HasLogger {
  /**
   * Returns scala batch source file (which is a virtual file) associated to
   * the given generated source.
   */
  def scalaFileFromGen(gen: GeneratedSourceVirtual): BatchSourceFile = {
    val fileName = gen.path
    val file = ScalaFileManager.scalaFile(fileName)
    new BatchSourceFile(file, gen.content)
  }

  /**
   * Returns scala batch source file (which is a virtual file) which is 
   * the result of compiling the given template compilation unit
   */
  def scalaFileFromTCU(tcu: TemplateCompilationUnit): Try[BatchSourceFile] = {
    tcu.generatedSource() map scalaFileFromGen
  }

  private val scalaProject = playProject.scalaProject

  def problemsOf(tcu: TemplateCompilationUnit): List[IProblem] = {
    tcu.generatedSource() match {
      case Success(generatedSource) =>
        val src = scalaFileFromGen(generatedSource)
        val problems = scalaProject.presentationCompiler(pc => pc.problemsOf(src.file)).getOrElse(Nil)
        def mapOffset(offset: Int) = generatedSource.mapPosition(offset)
        def mapLine(line: Int) = generatedSource.mapLine(line)
        problems map {
          // problems of the generated scala file
          case problem: DefaultProblem => new DefaultProblem(
            tcu.getTemplateFullPath.toCharArray(),
            problem.getMessage(),
            problem.getID(),
            problem.getArguments(),
            ProblemSeverities.Error,
            mapOffset(problem.getSourceStart()),
            mapOffset(problem.getSourceEnd()),
            mapLine(problem.getSourceLineNumber()),
            1)
        }

      case Failure(parseError: TemplateToScalaCompilationError) =>
        List(parseError.toProblem)

      case Failure(error) =>
        logger.error(s"Unexpected error while parsing template ${tcu.file.name}", error)
        List(unknownError(tcu, error))
    }
  }
  
  private def unknownError(tcu: TemplateCompilationUnit, error: Throwable): IProblem = {
    val severityLevel = ProblemSeverities.Error
    val message = s"${error.getMessage()} - ${error.getClass()}"
    new DefaultProblem(
      tcu.getTemplateFullPath.toCharArray(),
      message,
      0,
      Array.empty[String],
      severityLevel,
      0,
      1,
      1,
      1)
  }

  /**
   * Reload the template compilation unit
   *
   * FIXME: `content` seems to be ignored
   */
  def askReload(tcu: TemplateCompilationUnit, content: Array[Char]) {
    for (generatedSource <- tcu.generatedSource()) {
      val src = scalaFileFromGen(generatedSource)
      val sourceList = List(src)
      scalaProject.presentationCompiler (pc => {
        pc.withResponse((response: pc.Response[Unit]) => {
          pc.askReload(sourceList, response)
          response.get
        })
      })
    }
  }

  def withSourceFile[T](tcu: TemplateCompilationUnit)(op: (SourceFile, ScalaPresentationCompiler) => T): Option[T] =
    scalaProject.presentationCompiler(pc => {
      scalaFileFromTCU(tcu).map(op(_, pc)).toOption
    }).flatten

  def destroy() = {
    CompilerUsing.templateCompiler.TemplateAsFunctionCompiler.PresentationCompiler.shutdown()
  }
}

object ScalaFileManager {
  val scalaFile = new mutable.HashMap[String, AbstractFile] withDefault (fileName => {
    new VirtualFile(fileName)
  })
}