package org.scalaide.play2.templates24

import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext

object Activator {
  @volatile
  private var context: BundleContext = _

  def getContext: BundleContext = context
}

class Activator extends BundleActivator {

  override def start(bundleContext: BundleContext): Unit =
    Activator.context = bundleContext

  override def stop(bundleContext: BundleContext): Unit =
    Activator.context = null

}
