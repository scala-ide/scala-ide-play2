package org.scalaide.play2.routeeditor

import scala.Array.canBuildFrom
import scala.tools.eclipse.lexical.SingleTokenScanner

import org.eclipse.jdt.internal.ui.text.JavaColorManager
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.presentation.PresentationReconciler
import org.eclipse.jface.text.rules.DefaultDamagerRepairer
import org.eclipse.jface.text.rules.ITokenScanner
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.jface.util.PropertyChangeEvent
import org.scalaide.play2.routeeditor.scanners.RouteActionScanner
import org.scalaide.play2.routeeditor.scanners.RoutePartitions
import org.scalaide.play2.routeeditor.scanners.RouteScanner
import org.scalaide.play2.routeeditor.scanners.RouteURIScanner

class RouteConfiguration(prefStore: IPreferenceStore, routeEditor: RouteEditor) extends SourceViewerConfiguration {
  val reconciler = new PresentationReconciler();
  val colorManager = new JavaColorManager()
  private val routeDoubleClickStrategy: RouteDoubleClickStrategy =
    new RouteDoubleClickStrategy()
  private val scanner: RouteScanner = {
    val result = new RouteScanner(prefStore, colorManager)
    //    result.setDefaultReturnToken(RouteColorConstants.getToken("DEFAULT",
    //      colorManager))
    result
  }

  private val uriScanner: RouteURIScanner = {
    val result = new RouteURIScanner(prefStore, colorManager)
    //    result.setDefaultReturnToken(RouteColorConstants.getToken("ROUTE_URI",
    //      colorManager))
    result
  }

  private val actionScanner: RouteActionScanner = {
    val result = new RouteActionScanner(prefStore, colorManager)
    //    result.setDefaultReturnToken(RouteColorConstants.getToken("ROUTE_ACTION",
    //      colorManager))
    result
  }

  private val commentScanner: SingleTokenScanner = {
    val result = new SingleTokenScanner(RouteSyntaxClasses.COMMENT, colorManager, prefStore)
    result
  }

  override def getDoubleClickStrategy(sourceViewer: ISourceViewer, contentType: String) = {
    routeDoubleClickStrategy
  }

  def handlePartition(partitionType: String, tokenScanner: ITokenScanner) {
    val dr = new DefaultDamagerRepairer(tokenScanner)
    reconciler.setDamager(dr, partitionType)
    reconciler.setRepairer(dr, partitionType)
  }

  override def getConfiguredContentTypes(sourceViewer: ISourceViewer) = {
    RoutePartitions.getTypes() ++ Array(IDocument.DEFAULT_CONTENT_TYPE)
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
    handlePartition(uriScanner, RoutePartitions.ROUTE_URI)
    handlePartition(actionScanner, RoutePartitions.ROUTE_ACTION)
    handlePartition(commentScanner, RoutePartitions.ROUTE_COMMENT)
    //    handlePartition(scanner, RoutePartitionScanner.ROUTE_COMMENT)

    reconciler
  }

  def handlePropertyChangeEvent(event: PropertyChangeEvent) {
    scanner.adaptToPreferenceChange(event)
    uriScanner.adaptToPreferenceChange(event)
    actionScanner.adaptToPreferenceChange(event)
    commentScanner.adaptToPreferenceChange(event)
  }
}