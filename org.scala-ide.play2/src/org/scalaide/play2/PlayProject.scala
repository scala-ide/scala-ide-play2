package org.scalaide.play2

import scala.collection.mutable

import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.ProjectScope
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.ui.preferences.ScopedPreferenceStore
import org.scalaide.core.IScalaProject
import org.scalaide.play2.properties.PlayPreferences
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.scalaide.play2.util.SyncedScopedPreferenceStore

class PlayProject private (val scalaProject: IScalaProject) {
  val cachedPreferenceStore = new SyncedScopedPreferenceStore(scalaProject.underlying, PlayPlugin.PluginId)

  /**
   * Return additional imports that are automatically added to template files.
   *
   *  @return The additional imports, or the empty string if none defined.
   */
  def templateImports(extension: String): String = {
    import org.scalaide.play2.properties.PlayPreferences._
    (defaultImports + cachedPreferenceStore.getString(TemplateImports)).replace("%format%", extension)
  }

  /** Return a new project-scoped preference store for this project. */
  def generateScopedPreferenceStore: IPreferenceStore = new ScopedPreferenceStore(new ProjectScope(scalaProject.underlying), PlayPlugin.PluginId)

  /**
   * Tries to load the scala template files
   */
  def initialize() {
    val templateCompilationUnits = for (
      r <- scalaProject.underlying.members() if r.isInstanceOf[IFile] && r.getFullPath().toString().endsWith("." + PlayPlugin.TemplateExtension)
    ) yield TemplateCompilationUnit(r.asInstanceOf[IFile], false)
    templateCompilationUnits foreach (_.initialReconcile())
    // TODO: Why was there a second round of ask reload here?
    // templateCompilationUnits.reverse foreach (_.askReload())
  }

  /**
   * FIXME: This method should probably not exist.
   *         Template files can be anywhere
   *
   *  @return the absolute location of the project directory
   */
  lazy val sourceDir = scalaProject.underlying.getLocation().toFile()
}

object PlayProject {
  private val projects = (new mutable.HashMap) withDefault { (scalaProject: IScalaProject) => new PlayProject(scalaProject) }
  def apply(scalaProject: IScalaProject): PlayProject = {
    projects(scalaProject)
  }
}
