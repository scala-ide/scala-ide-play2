package org.scalaide.play2.routeeditor

import scala.Array.canBuildFrom
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.presentation.PresentationReconciler
import org.eclipse.jface.text.rules.DefaultDamagerRepairer
import org.eclipse.jface.text.rules.ITokenScanner
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.scalaide.play2.routeeditor.scanners.RouteActionScanner
import org.scalaide.play2.routeeditor.scanners.RoutePartitionScanner
import org.scalaide.play2.routeeditor.scanners.RouteScanner
import org.scalaide.play2.routeeditor.scanners.RouteURIScanner
import org.scalaide.play2.routeeditor.scanners.RouteCommentScanner

class RouteConfiguration(colorManager: ColorManager, routeEditor: RouteEditor) extends SourceViewerConfiguration {
  val reconciler = new PresentationReconciler();
  private val xmlDoubleClickStrategy: RouteDoubleClickStrategy =
    new RouteDoubleClickStrategy()
  private val scanner: RouteScanner = {
    val result = new RouteScanner(colorManager)
//    result.setDefaultReturnToken(RouteColorConstants.getToken("DEFAULT",
//      colorManager))
    result
  }

  private val uriScanner: RouteURIScanner = {
    val result = new RouteURIScanner(colorManager)
//    result.setDefaultReturnToken(RouteColorConstants.getToken("ROUTE_URI",
//      colorManager))
    result
  }

  private val actionScanner: RouteActionScanner = {
    val result = new RouteActionScanner(colorManager)
//    result.setDefaultReturnToken(RouteColorConstants.getToken("ROUTE_ACTION",
//      colorManager))
    result
  }
  
  private val commentScanner: RouteCommentScanner = {
    val result = new RouteCommentScanner(RouteColorConstants.getToken("ROUTE_COMMENT",
      colorManager))
    result
  }

  override def getDoubleClickStrategy(sourceViewer: ISourceViewer, contentType: String) = {
    xmlDoubleClickStrategy
  }

  def handlePartition(partitionType: String, tokenScanner: ITokenScanner) {
    val dr = new DefaultDamagerRepairer(tokenScanner)
    reconciler.setDamager(dr, partitionType)
    reconciler.setRepairer(dr, partitionType)
  }

  override def getConfiguredContentTypes(sourceViewer: ISourceViewer) = {
    RoutePartitionScanner.getTypes() ++ Array(IDocument.DEFAULT_CONTENT_TYPE)
  }

  override def getHyperlinkDetectors(sourceViewer: ISourceViewer) = {
    Array(new RouteHyperlinkDetector(routeEditor));
  }

  def handlePartition(scan: ITokenScanner, token: String) = {
    val dr = new DefaultDamagerRepairer(scan);
    reconciler.setDamager(dr, token);
    reconciler.setRepairer(dr, token);
  }

  override def getPresentationReconciler(
    sourceViewer: ISourceViewer) = {

    handlePartition(scanner, IDocument.DEFAULT_CONTENT_TYPE)
    handlePartition(uriScanner, RoutePartitionScanner.ROUTE_URI)
    handlePartition(actionScanner, RoutePartitionScanner.ROUTE_ACTION)
    handlePartition(commentScanner, RoutePartitionScanner.ROUTE_COMMENT)
//    handlePartition(scanner, RoutePartitionScanner.ROUTE_COMMENT)
    
    reconciler
  }
}