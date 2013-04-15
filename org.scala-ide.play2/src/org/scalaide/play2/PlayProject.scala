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
import org.scalaide.play2.properties.PlayPreferences

class PlayProject private (val scalaProject: ScalaProject) {
  private val presentationCompiler = new TemplatePresentationCompiler(this)

  val cachedPreferenceStore = new SyncedScopedPreferenceStore(scalaProject.underlying, PlayPlugin.PluginId)

  /** Return additional imports that are automatically added to template files.
   *
   *  @return The additional imports, or the empty string if none defined.
   */
  def additionalTemplateImports(extension: String): String = {
    cachedPreferenceStore.getString(PlayPreferences.TemplateImports).replace("%format%", extension);
  }

  /** Return a new project-scoped preference store for this project. */
  def generateScopedPreferenceStore: IPreferenceStore = new ScopedPreferenceStore(new ProjectScope(scalaProject.underlying), PlayPlugin.PluginId)

  def withPresentationCompiler[T](op: TemplatePresentationCompiler => T): T = {
    op(presentationCompiler)
  }

  def withSourceFile[T](tcu: TemplateCompilationUnit)(op: (SourceFile, ScalaPresentationCompiler) => T): Option[T] = {
    withPresentationCompiler { compiler =>
      compiler.withSourceFile(tcu)(op)
    }
  }

  /** Tries to load the scala template files
   */
  def initialize() {
    val templateCompilationUnits = for (
      r <- scalaProject.underlying.members() if r.isInstanceOf[IFile] && r.getFullPath().toString().endsWith("." + PlayPlugin.TemplateExtension)
    ) yield TemplateCompilationUnit(r.asInstanceOf[IFile])
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
   *  @return the absolute location of the project directory
   */
  lazy val sourceDir = scalaProject.underlying.getLocation().toFile()
}

object PlayProject {
  private val projects = new AutoHashMap((scalaProject: ScalaProject) => new PlayProject(scalaProject))
  def apply(scalaProject: ScalaProject): PlayProject = {
    projects(scalaProject)
  }
}