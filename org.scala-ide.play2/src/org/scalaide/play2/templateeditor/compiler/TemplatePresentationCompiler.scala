package org.scalaide.play2.templateeditor.compiler

import org.eclipse.jdt.core.compiler.IProblem
import org.scalaide.play2.PlayProject
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.scalaide.play2.util.AutoHashMap
import scala.tools.eclipse.javaelements.ScalaSourceFile
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem
import scala.tools.eclipse.javaelements.ScalaCompilationUnit

class TemplatePresentationCompiler(playProject: PlayProject) {
  private val sourceFiles = new AutoHashMap((tcu: TemplateCompilationUnit) => tcu.sourceFile())
  def generatedSource(tcu: TemplateCompilationUnit) = {
    val sourceFile = tcu.file.file.getAbsoluteFile()
    val gen = CompilerUsing.compileTemplateToScala(sourceFile, playProject)
    gen
  }
  private val scalaCompilationUnits = new AutoHashMap((tcu: TemplateCompilationUnit) => {
    val gen = generatedSource(tcu)
    ScalaSourceFile.createFromPath(gen.file.getAbsolutePath())
  })
  //  private def scalaCompilationUnits(tcu: TemplateCompilationUnit) = {
  //    val sourceFile = tcu.file.file.getAbsoluteFile()
  //    val gen = CompilerUsing.compileTemplateToScala(sourceFile, playProject)
  //    ScalaSourceFile.createFromPath(gen.file.getAbsolutePath())
  //  }

  private val scalaProject = playProject.scalaProject

  def problemsOf(tcu: TemplateCompilationUnit): List[IProblem] = {
    try {
      val scu = scalaCompilationUnits(tcu).asInstanceOf[ScalaCompilationUnit]
      val problems = scalaProject.withPresentationCompiler(pc => pc.problemsOf(scu))()
      val gen = generatedSource(tcu)
      def mapOffset(offset: Int) = gen.mapPosition(offset)
      def mapLine(line: Int) = gen.mapLine(line)
      problems map (p => p match {
        case problem: DefaultProblem => new DefaultProblem(
          tcu.file.file.getAbsolutePath().toCharArray,
          problem.getMessage(),
          problem.getID(),
          problem.getArguments(),
          ProblemSeverities.Error,
          mapOffset(problem.getSourceStart()),
          mapOffset(problem.getSourceEnd()),
          mapLine(problem.getSourceLineNumber()),
          1)
      })
    } catch {
      case TemplateToScalaCompilationError(source, message, offset, line, column) => {
        val severityLevel = ProblemSeverities.Error
        val p = new DefaultProblem(
          source.getAbsolutePath().toCharArray,
          message,
          0,
          new Array[String](0),
          severityLevel,
          offset,
          offset + 1,
          line,
          column)
        List(p)
      }
      case e: Exception => {
        val severityLevel = ProblemSeverities.Error
        val message = e.getMessage()
        val p = new DefaultProblem(
          tcu.file.file.getAbsolutePath().toCharArray,
          message,
          0,
          new Array[String](0),
          severityLevel,
          0,
          1,
          1,
          1)
        List(p)
      }
    }
  }

  def askReload(tcu: TemplateCompilationUnit, content: Array[Char]) = {
    sourceFiles.get(tcu) match {
      case Some(f) =>
        val newF = tcu.batchSourceFile(content)
        synchronized {
          sourceFiles(tcu) = newF
        }
        try {
          val scu = scalaCompilationUnits(tcu).asInstanceOf[ScalaCompilationUnit]
          val sourceList = List(scu.sourceFile())
          scalaProject.withPresentationCompiler(pc => {
            val response = new pc.Response[Unit]
            pc.askReload(sourceList, response)
            response.get
          })()
        } catch {
          case _ => () // TODO think more!
        }
      case None =>
    }
  }

}
