package org.scalaide.play2.routeeditor.lexical

import scala.tools.eclipse.lexical.ScalaPartitionRegion

import org.scalaide.play2.lexical.PlayDocumentPartitioner

import RoutePartitions.ROUTE_DEFAULT
import RoutePartitions.ROUTE_URI

class RouteDocumentPartitioner(conservative: Boolean = false) extends PlayDocumentPartitioner(new RoutePartitionTokeniser, ROUTE_DEFAULT, conservative) {

  import RouteDocumentPartitioner._

  override def getLegalContentTypes: Array[String] = RoutePartitions.getTypes
  
  def uriPartitions: List[ScalaPartitionRegion] = partitionRegions.filter(_.getType == ROUTE_URI) 
}