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

class TemplatePresentationCompiler(playProject: PlayProject) {
  private val sourceFiles = new AutoHashMap((tcu: TemplateCompilationUnit) => tcu.sourceFile())
  def generatedSource(tcu: TemplateCompilationUnit) = {
    val sourceFile = tcu.file.file.getAbsoluteFile() // TODO look more closely
//    val sourceFile = tcu.sourceFile(tcu.getTemplateContents).file.file.getAbsoluteFile()
    val gen = CompilerUsing.compileTemplateToScala(sourceFile, playProject)
    gen
  }
  //  private val scalaCompilationUnits = new AutoHashMap[TemplateCompilationUnit, ScalaCompilationUnit]((tcu: TemplateCompilationUnit) => {
  //    def relativePath(first: String, second: String) = {
  //      first.substring(first.indexOf(second))
  //    }
  //    val gen = generatedSource(tcu)
  //    val relPath = relativePath(gen.file.getAbsolutePath(), playProject.scalaProject.underlying.getFullPath().toString())
  //    val scu = ScalaSourceFile.createFromPath(relPath) match {
  //      case Some(v) => v
  //      case None => throw new Exception("Configuration error!")
  //    }
  //    playProject.scalaProject.withSourceFile(scu) ((x, y) => ()) (()); // in order to make it load!
  //    scu
  //  })
  //  val scalaFiles = new AutoHashMap[TemplateCompilationUnit, SourceFile]((tcu: TemplateCompilationUnit) => {
  //	val gen = generatedSource(tcu)
  //    val src = new BatchSourceFile(gen.file.getAbsolutePath(), Path(gen.file).slurpString)
  //	src
  //  })

  def scalaFileFromGen(gen: GeneratedSource) = {
    val fileName = gen.file.getAbsolutePath()
    val file = ScalaFileManager.scalaFile(fileName)
    new BatchSourceFile(file, Path(gen.file).slurpString)
  }

  def scalaFileFromTCU(tcu: TemplateCompilationUnit) = {
    val gen = generatedSource(tcu)
    scalaFileFromGen(gen)
  }

  private val scalaProject = playProject.scalaProject

  def problemsOf(tcu: TemplateCompilationUnit): List[IProblem] = {
    try {
      //      val scu = scalaCompilationUnits(tcu).asInstanceOf[ScalaCompilationUnit]
      val gen = generatedSource(tcu)
      val src = scalaFileFromGen(gen)
      val problems = scalaProject.withPresentationCompiler(pc => pc.problemsOf(src.file))()
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
          offset - 1,
          offset - 1,
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

  def askReload(tcu: TemplateCompilationUnit, content: Array[Char]) {
    sourceFiles.get(tcu) match {
      case Some(f) =>
        val newF = tcu.batchSourceFile(content)
        synchronized {
          sourceFiles(tcu) = newF
        }

      case None =>
        synchronized {
          sourceFiles.put(tcu, tcu.sourceFile(content))
        }
    }
    try {
      //      val scu = scalaCompilationUnits(tcu).asInstanceOf[ScalaCompilationUnit]
      val gen = generatedSource(tcu)
      val src = scalaFileFromGen(gen)
      val sourceList = List(src)
      scalaProject.withPresentationCompiler(pc => {
                pc.withResponse((response: pc.Response[Unit]) => {
//        val contents = Path(gen.file).slurpString
//        val response = pc.askReload(scu, contents.toCharArray)
//        response.get
                	pc.askReload(sourceList, response)
                	response.get
                })
      })()
    } catch {
      case _ => // TODO think more!
    }
  }

  def withSourceFile[T](tcu : TemplateCompilationUnit)(op : (SourceFile, ScalaPresentationCompiler) => T) : T =
    scalaProject.withPresentationCompiler(pc =>{
    	op(scalaFileFromTCU(tcu), pc)
    })()
}

object ScalaFileManager {
  val scalaFile = new AutoHashMap[String, AbstractFile](fileName => {
//    new PlainFile(scala.tools.nsc.io.Path.string2path(fileName))
    EclipseResource.fromString(fileName) match {
      case Some(v) => v match {
      	case ef@EclipseFile(_) => ef
      	case _ => null
      }
      case None => null
    }
  }) 
}