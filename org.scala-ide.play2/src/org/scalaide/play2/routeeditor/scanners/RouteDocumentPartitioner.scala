package org.scalaide.play2.routeeditor.scanners

import org.eclipse.jface.text.rules.FastPartitioner

class RouteDocumentPartitioner extends FastPartitioner(new RoutePartitionScanner(), RoutePartitions.getTypes)