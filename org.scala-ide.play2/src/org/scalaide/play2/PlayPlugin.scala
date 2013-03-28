package org.scalaide.play2

import scala.tools.eclipse.ScalaPlugin
import org.eclipse.core.resources.IProject
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Status

object PlayPlugin {
  @volatile var plugin: PlayPlugin = _

  private final val PluginId = "org.scala-ide.play2"
  final val RouteFormatterMarginId = PluginId + ".routeeditor.margin"
  final val TemplateExtension = "scala.html"

  def prefStore = plugin.getPreferenceStore

  def getImageDescriptor(path: String) = {
    AbstractUIPlugin.imageDescriptorFromPlugin(PluginId, path);
  }

  def log(status: Int, msg: String, ex: Throwable = null) {
    plugin.getLog.log(new Status(status, plugin.getBundle().getSymbolicName(), msg, ex))
  }
}

class PlayPlugin extends AbstractUIPlugin {
  import PlayPlugin._
  override def start(context: BundleContext) = {
    super.start(context)
    PlayPlugin.plugin = this
    initializeProjects()
  }

  override def stop(context: BundleContext) = {
    PlayPlugin.plugin = null
    super.stop(context)
  }

  def asPlayProject(project: IProject): Option[PlayProject] = {
    val scalaProject = ScalaPlugin.plugin.asScalaProject(project)
    scalaProject map (PlayProject(_))
  }
  
  def initializeProjects() = {
    for {
      iProject <- ResourcesPlugin.getWorkspace.getRoot.getProjects
      if iProject.isOpen
      playProject <- asPlayProject(iProject)
    } playProject.initialize()
  }
}