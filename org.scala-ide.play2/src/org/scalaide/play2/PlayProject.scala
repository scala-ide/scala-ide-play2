package org.scalaide.play2

import java.io.File
import scala.tools.eclipse.ScalaPresentationCompiler
import scala.tools.eclipse.ScalaProject
import scala.tools.nsc.util.SourceFile
import org.eclipse.core.resources.IFile
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.scalaide.play2.templateeditor.compiler.TemplatePresentationCompiler
import org.scalaide.play2.util.AutoHashMap

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

  def initialize() {
    val templateCompilationUnits = for (
      r <- scalaProject.underlying.members() if r.isInstanceOf[IFile] if r.getFullPath().toString().endsWith(PlayPlugin.plugin.templateExtension)
    )  yield TemplateCompilationUnit.fromFile(r.asInstanceOf[IFile])
    templateCompilationUnits foreach (_.askReload())
    templateCompilationUnits.reverse foreach (_.askReload())
    // FIXME not works!!!
  }

  def dispose() {
    presentationCompiler.destroy()
  }

  lazy val sourceDir = new File(scalaProject.underlying.getLocation().toString() + "/app/views")
}

object PlayProject {
  private val projects = new AutoHashMap((scalaProject: ScalaProject) => new PlayProject(scalaProject))
  def apply(scalaProject: ScalaProject) = {
    projects(scalaProject)
  }
}