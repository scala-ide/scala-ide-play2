package org.scalaide.play2.routeeditor.scanners

import org.eclipse.jface.text.IDocument


object RoutePartitions {
  val ROUTE_PARTITIONING = "___route_partitioning"
  val ROUTE_URI = "__route_uri"
  val ROUTE_ACTION = "__route_action"
  val ROUTE_COMMENT = "__route_comment"
  val ROUTE_DEFAULT = IDocument.DEFAULT_CONTENT_TYPE

  def getTypes() = {
    Array(ROUTE_URI, ROUTE_ACTION, ROUTE_COMMENT);
  }

  def isRouteAction(typeString: String) = {
    typeString == ROUTE_ACTION;
  }
}
