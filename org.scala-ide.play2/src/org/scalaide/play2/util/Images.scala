package org.scalaide.play2.util

import org.scalaide.play2.PlayPlugin
import org.eclipse.jface.resource.ImageDescriptor

object Images {
  final val ROUTES_ICON = "routes.icon"
  final val HTTP_METHODS_ICON = "web.icon"
  val ROUTES_ICON_DESCRIPTOR: ImageDescriptor = PlayPlugin.getImageDescriptor("icons/routes.png")
  val HTTP_METHODS_ICON_DESCRIPTOR: ImageDescriptor = PlayPlugin.getImageDescriptor("icons/web.png")
}