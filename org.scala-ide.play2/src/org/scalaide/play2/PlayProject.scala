package org.scalaide.play2

import scala.collection.mutable
import scala.tools.eclipse.ScalaProject
import org.scalaide.play2.templateeditor.compiler.TemplatePresentationCompiler
import org.scalaide.play2.util.AutoHashMap
import java.io.File
import scala.tools.nsc.util.SourceFile
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import scala.tools.eclipse.ScalaPresentationCompiler

class PlayProject private (val scalaProject: ScalaProject) {
  private val presentationCompiler = new TemplatePresentationCompiler(this)

  def withPresentationCompiler[T](op: TemplatePresentationCompiler => T): T = {
    op(presentationCompiler)
  }
  
  def withSourceFile[T](tcu: TemplateCompilationUnit)(op: (SourceFile, ScalaPresentationCompiler) => T): T = {
    withPresentationCompiler { compiler =>
      compiler.withSourceFile(tcu)(op)
    } 
  }
  
  lazy val sourceDir = new File(scalaProject.underlying.getLocation().toString()+"app/views")
  lazy val generatedDir = new File(scalaProject.underlying.getLocation().toString()+"/target/test/generated-templates")
  lazy val generatedClasses = new File(scalaProject.underlying.getLocation().toString()+"/target/test/generated-classes")
}

object PlayProject {
  private val projects = new AutoHashMap((scalaProject: ScalaProject) => new PlayProject(scalaProject))
  def apply(scalaProject: ScalaProject) = {
    projects(scalaProject)
  }
}