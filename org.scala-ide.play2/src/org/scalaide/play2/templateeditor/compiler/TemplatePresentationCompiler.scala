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
import org.scalaide.core.compiler.IScalaPresentationCompiler
import play.twirl.compiler.GeneratedSourceVirtual
import org.scalaide.logging.HasLogger
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.collection.mutable
import org.scalaide.play2.PlayProject
import org.scalaide.core.compiler.InteractiveCompilationUnit
import org.scalaide.core.IScalaProject

/**
 * presentation compiler for template files
 */
class TemplatePresentationCompiler(playProject: PlayProject) extends HasLogger {

  import TemplatePresentationCompiler._

  private val scalaProject = playProject.scalaProject

  def problemsOf(tcu: TemplateCompilationUnit): List[IProblem] = {
    tcu.generatedSource() match {
      case Success(generatedSource) =>
        val icu = new TranslatedTemplateInteractiveCompilationUnit(generatedSource, scalaProject)
        val problems = scalaProject.presentationCompiler(pc => pc.problemsOf(icu)).getOrElse(Nil)
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
   */
  def askReload(tcu: TemplateCompilationUnit) {
    import org.scalaide.core.compiler.IScalaPresentationCompiler.Implicits._
    for (generatedSource <- tcu.generatedSource()) {
      scalaProject.presentationCompiler { pc =>
        val icu = new TranslatedTemplateInteractiveCompilationUnit(generatedSource, scalaProject)
        pc.askReload(icu, icu.getContents()).getOption()
      }
    }
  }

  def destroy() = {
    CompilerUsing.templateCompiler.TemplateAsFunctionCompiler.PresentationCompiler.shutdown()
  }
}

object TemplatePresentationCompiler {

  private class TranslatedTemplateInteractiveCompilationUnit(gen: GeneratedSourceVirtual, override val scalaProject: IScalaProject) extends InteractiveCompilationUnit {
    override def currentProblems(): List[org.eclipse.jdt.core.compiler.IProblem] = throw new UnsupportedOperationException
    override def exists(): Boolean = throw new UnsupportedOperationException
    override def reconcile(newContents: String): List[org.eclipse.jdt.core.compiler.IProblem] = throw new UnsupportedOperationException
    override def initialReconcile(): scala.tools.nsc.interactive.Response[Unit] = throw new UnsupportedOperationException
    override def workspaceFile: org.eclipse.core.resources.IFile = throw new UnsupportedOperationException

    override def file: tools.nsc.io.AbstractFile = {
      new VirtualFile(gen.path)
    }

    override def getContents(): Array[Char] = {
      gen.content.toArray
    }

    override def sourceFile(contents: Array[Char]): tools.nsc.util.SourceFile = {
      new BatchSourceFile(file, contents)
    }
  }

}

