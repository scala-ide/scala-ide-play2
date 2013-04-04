package org.scalaide.play2

import java.io.File
import scala.tools.eclipse.ScalaPresentationCompiler
import scala.tools.eclipse.ScalaProject
import scala.tools.nsc.util.SourceFile
import org.eclipse.core.resources.IFile
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.scalaide.play2.templateeditor.compiler.TemplatePresentationCompiler
import org.scalaide.play2.util.AutoHashMap
import org.eclipse.ui.preferences.ScopedPreferenceStore
import org.eclipse.core.resources.ProjectScope
import org.scalaide.play2.util.SyncedScopedPreferenceStore
import org.eclipse.jface.preference.IPreferenceStore

class PlayProject private (val scalaProject: ScalaProject) {
  private val presentationCompiler = new TemplatePresentationCompiler(this)

  val cachedPreferenceStore = new SyncedScopedPreferenceStore(scalaProject.underlying, PlayPlugin.PluginId)

  def generateScopedPreferenceStore: IPreferenceStore = new ScopedPreferenceStore(new ProjectScope(scalaProject.underlying), PlayPlugin.PluginId)

  def withPresentationCompiler[T](op: TemplatePresentationCompiler => T): T = {
    op(presentationCompiler)
  }

  def withSourceFile[T](tcu: TemplateCompilationUnit)(op: (SourceFile, ScalaPresentationCompiler) => T): Option[T] = {
    withPresentationCompiler { compiler =>
      compiler.withSourceFile(tcu)(op)
    }
  }

  /**
   * Tries to load the scala template files
   */
  def initialize() {
    val templateCompilationUnits = for (
      r <- scalaProject.underlying.members() if r.isInstanceOf[IFile] if r.getFullPath().toString().endsWith("." + PlayPlugin.TemplateExtension)
    )  yield TemplateCompilationUnit(r.asInstanceOf[IFile])
    templateCompilationUnits foreach (_.askReload())
    templateCompilationUnits.reverse foreach (_.askReload())
    // FIXME not works!!!
  }

  def dispose() {
    presentationCompiler.destroy()
  }

  /** FIXME: This method should probably not exist.
   *         Template files can be anywhere
   *
   *  @return the absolute location of the `/app/views` directory, below the project root
   */
  lazy val sourceDir = new File(scalaProject.underlying.getLocation().toString() + "/app/views")
}

object PlayProject {
  private val projects = new AutoHashMap((scalaProject: ScalaProject) => new PlayProject(scalaProject))
  def apply(scalaProject: ScalaProject): PlayProject = {
    projects(scalaProject)
  }
}