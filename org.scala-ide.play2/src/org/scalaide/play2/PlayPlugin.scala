package org.scalaide.play2

import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext

object PlayPlugin {
  @volatile var plugin: PlayPlugin = _
  val PLUGIN_ID = "org.scala-ide.play2"

  def getDefault = PlayPlugin.plugin
  
  def prefStore = plugin.getPreferenceStore

  def getImageDescriptor(path: String) = {
    AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
  }
}

class PlayPlugin extends AbstractUIPlugin {
  import PlayPlugin._
  override def start(context: BundleContext) = {
    super.start(context);
    PlayPlugin.plugin = this;
  }

  override def stop(context: BundleContext) = {
    PlayPlugin.plugin = null;
    super.stop(context);
  }
  
  val problemMarkerId = PLUGIN_ID + ".templateProblem"

}