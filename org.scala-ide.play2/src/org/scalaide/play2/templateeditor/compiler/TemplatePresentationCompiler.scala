package org.scalaide.play2.templateeditor.compiler

import org.eclipse.jdt.core.compiler.IProblem
import org.scalaide.play2.PlayProject
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.scalaide.play2.util.AutoHashMap
import scala.tools.eclipse.javaelements.ScalaSourceFile
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem
import scala.tools.eclipse.javaelements.ScalaCompilationUnit
import scalax.file.Path
import scala.tools.nsc.util.SourceFile
import scala.tools.nsc.util.BatchSourceFile
import play.templates.GeneratedSource
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualFile
import scala.tools.nsc.io.PlainFile
import java.io.File
import scala.tools.eclipse.util.EclipseFile
import scala.tools.eclipse.util.EclipseResource
import scala.tools.eclipse.ScalaPresentationCompiler
import play.templates.GeneratedSourceVirtual
/**
 * presentation compiler for template files
 */
class TemplatePresentationCompiler(playProject: PlayProject) {
  /**
   * A map between compilation units and associated batch source files
   */
  private val sourceFiles = new AutoHashMap((tcu: TemplateCompilationUnit) => tcu.sourceFile())
  
  /**
   * Returns generated source of the given compilation unit
   */
  def generatedSource(tcu: TemplateCompilationUnit) = {
    CompilerUsing.compileTemplateToScalaVirtual(tcu.getTemplateContents.toString(), tcu.file.file, playProject)
  }
  
  /**
   * Returns scala batch source file (which is a virtual file) associated to
   * the given generated source.
   */
  def scalaFileFromGen(gen: GeneratedSourceVirtual) = {
    val fileName = gen.path
    val file = ScalaFileManager.scalaFile(fileName)
    new BatchSourceFile(file, gen.content)
  }

  /**
   * Returns scala batch source file (which is a virtual file) which is 
   * the result of compiling the given template compilation unit
   */
  def scalaFileFromTCU(tcu: TemplateCompilationUnit) = {
    val gen = generatedSource(tcu)
    scalaFileFromGen(gen)
  }

  private val scalaProject = playProject.scalaProject

  def problemsOf(tcu: TemplateCompilationUnit): List[IProblem] = {
    try {
      val gen = generatedSource(tcu)
      val src = scalaFileFromGen(gen)
      val problems = scalaProject.withPresentationCompiler(pc => pc.problemsOf(src.file))()
      def mapOffset(offset: Int) = gen.mapPosition(offset)
      def mapLine(line: Int) = gen.mapLine(line)
      problems map (p => p match {
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
      })
    } catch {
      // template file could not be compiled to scala file. So now there is only a single
      // problem which is the thrown exception
      case TemplateToScalaCompilationError(source, message, offset, line, column) => {
        val severityLevel = ProblemSeverities.Error
        val p = new DefaultProblem(
          source.getAbsolutePath().toCharArray,
          message,
          0,
          new Array[String](0),
          severityLevel,
          offset - 1,
          offset - 1,
          line,
          column)
        List(p)
      }
      // any other exception will be shown at first character of document
      case e: Exception => {
        val severityLevel = ProblemSeverities.Error
        val message = e.getMessage()
        val p = new DefaultProblem(
          tcu.getTemplateFullPath.toCharArray(),
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

  def askReload(tcu: TemplateCompilationUnit, content: Array[Char]) {
    sourceFiles.get(tcu) match {
      case Some(f) =>
        val newF = tcu.batchSourceFile(content)
        synchronized {
          sourceFiles(tcu) = newF
        }

      case None =>
        synchronized {
          sourceFiles.put(tcu, tcu.batchSourceFile(content))
        }
    }
    try {
      val gen = generatedSource(tcu)
      val src = scalaFileFromGen(gen)
      val sourceList = List(src)
      scalaProject.withPresentationCompiler(pc => {
        pc.withResponse((response: pc.Response[Unit]) => {
          pc.askReload(sourceList, response)
          response.get
        })
      })()
    } catch {
      case _ => 
    }
  }

  def withSourceFile[T](tcu: TemplateCompilationUnit)(op: (SourceFile, ScalaPresentationCompiler) => T): T =
    scalaProject.withPresentationCompiler(pc => {
      op(scalaFileFromTCU(tcu), pc)
    })()

  def destroy() = {
    CompilerUsing.templateCompiler.TemplateAsFunctionCompiler.CompilerInstance.compiler.askShutdown()
  }
}

object ScalaFileManager {
  val scalaFile = new AutoHashMap[String, AbstractFile](fileName => {
    new VirtualFile(fileName)
  })
}