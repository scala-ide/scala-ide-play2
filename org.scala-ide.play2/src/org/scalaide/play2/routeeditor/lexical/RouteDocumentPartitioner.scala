package org.scalaide.play2.routeeditor.lexical

import org.scalaide.play2.lexical.PlayDocumentPartitioner

import RoutePartitions.ROUTE_ACTION
import RoutePartitions.ROUTE_COMMENT
import RoutePartitions.ROUTE_DEFAULT
import RoutePartitions.ROUTE_URI

class RouteDocumentPartitioner(conservative: Boolean = false) extends PlayDocumentPartitioner(new RoutePartitionTokeniser, ROUTE_DEFAULT, conservative) {

  import RouteDocumentPartitioner._

  override def getLegalContentTypes = Array(ROUTE_DEFAULT, ROUTE_URI, ROUTE_ACTION, ROUTE_COMMENT) //RoutePartitions.getTypes
}