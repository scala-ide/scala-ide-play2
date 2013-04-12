package org.scalaide.play2

import scala.tools.eclipse.ScalaPlugin

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Status
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext

object PlayPlugin {
  @volatile
  private var plugin: PlayPlugin = _

  final val PluginId = "org.scala-ide.play2"
  final val RouteFormatterMarginId = PluginId + ".routeeditor.margin"
  final val TemplateExtension = "scala.html"

  /** Return the current plugin instace */
  def instance(): PlayPlugin = plugin

  /** Return the plugin-wide preference store */
  def preferenceStore: IPreferenceStore = plugin.getPreferenceStore

  def getImageDescriptor(path: String): ImageDescriptor = {
    AbstractUIPlugin.imageDescriptorFromPlugin(PluginId, path);
  }

  def log(status: Int, msg: String, ex: Throwable = null): Unit = {
    plugin.getLog.log(new Status(status, plugin.getBundle().getSymbolicName(), msg, ex))
  }
}

class PlayPlugin extends AbstractUIPlugin {
  override def start(context: BundleContext): Unit = {
    super.start(context)
    PlayPlugin.plugin = this
    initializeProjects()
  }

  override def stop(context: BundleContext): Unit = {
    PlayPlugin.plugin = null
    super.stop(context)
  }

  def asPlayProject(project: IProject): Option[PlayProject] = {
    val scalaProject = ScalaPlugin.plugin.asScalaProject(project)
    scalaProject map (PlayProject(_))
  }

  private def initializeProjects(): Unit = {
    for {
      iProject <- ResourcesPlugin.getWorkspace.getRoot.getProjects
      if iProject.isOpen
      playProject <- asPlayProject(iProject)
    } playProject.initialize()
  }
}
