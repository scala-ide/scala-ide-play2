package org.scalaide.play2.routeeditor.lexical

import org.eclipse.jface.text.IDocument

object RoutePartitions {
  val ROUTE_PARTITIONING = "___route_partitioning"
  val ROUTE_URI = "__route_uri"
  val ROUTE_ACTION = "__route_action"
  val ROUTE_COMMENT = "__route_comment"
  val ROUTE_HTTP = "__route_http"
  val ROUTE_DEFAULT = IDocument.DEFAULT_CONTENT_TYPE

  val getTypes: Array[String] =
    Array(ROUTE_URI, ROUTE_ACTION, ROUTE_COMMENT, ROUTE_HTTP, ROUTE_DEFAULT);

  def isRouteAction(typeString: String): Boolean = typeString == ROUTE_ACTION
}
