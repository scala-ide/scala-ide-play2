package org.scalaide.play2

import scala.collection.mutable
import scala.tools.eclipse.ScalaProject
import org.scalaide.play2.templateeditor.compiler.TemplatePresentationCompiler
import org.scalaide.play2.util.AutoHashMap
import java.io.File

class PlayProject private (val scalaProject: ScalaProject) {
  private val presentationCompiler = new TemplatePresentationCompiler(this)

  def withPresentationCompiler[T](op: TemplatePresentationCompiler => T): T = {
    op(presentationCompiler)
  }
  
  lazy val sourceDir = new File("app/views")
  lazy val generatedDir = new File("target/test/generated-templates")
  lazy val generatedClasses = new File("target/test/generated-classes")
}

object PlayProject {
  private val projects = new AutoHashMap((scalaProject: ScalaProject) => new PlayProject(scalaProject))
  def apply(scalaProject: ScalaProject) = {
    projects(scalaProject)
  }
}