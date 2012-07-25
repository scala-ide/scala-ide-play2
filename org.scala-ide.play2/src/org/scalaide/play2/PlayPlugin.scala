package org.scalaide.play2

import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext

object PlayPlugin {
  var plugin: PlayPlugin = _
  val PLUGIN_ID = "Play Plugin"

  def getDefault = PlayPlugin.plugin

  def getImageDescriptor(path: String) = {
    AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
  }
}

class PlayPlugin extends AbstractUIPlugin {
  override def start(context: BundleContext) = {
    super.start(context);
    PlayPlugin.plugin = this;
  }

  override def stop(context: BundleContext) = {
    PlayPlugin.plugin = null;
    super.stop(context);
  }

}