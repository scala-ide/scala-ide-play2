package org.scalaide.play2.routeeditor.scanners


object RoutePartitions {
  val ROUTE_PARTITIONING = "___route_partitioning";
  val ROUTE_URI = "__route_uri"
  val ROUTE_ACTION = "__route_action"
  val ROUTE_COMMENT = "__route_comment"

  def getTypes() = {
    Array(ROUTE_URI, ROUTE_ACTION, ROUTE_COMMENT);
  }

  def isRouteAction(typeString: String) = {
    typeString == ROUTE_ACTION;
  }
}
