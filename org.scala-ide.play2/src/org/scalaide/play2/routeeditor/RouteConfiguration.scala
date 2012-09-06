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
import org.scalaide.play2.routeeditor.lexical.RouteActionScanner
import org.scalaide.play2.routeeditor.lexical.RoutePartitions
import org.scalaide.play2.routeeditor.lexical.RouteURIScanner
import org.scalaide.play2.properties.PropertyChangeHandler
import org.eclipse.jface.text.formatter.MultiPassContentFormatter
import org.scalaide.play2.routeeditor.formatter.RouteFormattingStrategy
import org.scalaide.play2.routeeditor.lexical.RoutePartitions

class RouteConfiguration(prefStore: IPreferenceStore, routeEditor: RouteEditor) extends SourceViewerConfiguration  with PropertyChangeHandler{
  val reconciler = new PresentationReconciler();
  val colorManager = new JavaColorManager()
  private val routeDoubleClickStrategy: RouteDoubleClickStrategy =
    new RouteDoubleClickStrategy()
  private val scanner = {
//    val result = new RouteScanner(prefStore, colorManager)
//    result
     new SingleTokenScanner(RouteSyntaxClasses.DEFAULT, colorManager, prefStore)
  }
  
  private val httpScanner = 
    new SingleTokenScanner(RouteSyntaxClasses.HTTP_KEYWORD, colorManager, prefStore)

  private val uriScanner: RouteURIScanner = {
    val result = new RouteURIScanner(prefStore, colorManager)
    result
  }

  private val actionScanner: RouteActionScanner = {
    val result = new RouteActionScanner(prefStore, colorManager)
    result
  }

  private val commentScanner: SingleTokenScanner = {
    val result = new SingleTokenScanner(RouteSyntaxClasses.COMMENT, colorManager, prefStore)
    result
  }

  override def getDoubleClickStrategy(sourceViewer: ISourceViewer, contentType: String) = {
    routeDoubleClickStrategy
  }

  override def getConfiguredContentTypes(sourceViewer: ISourceViewer) = {
    RoutePartitions.getTypes()
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

    handlePartition(scanner, RoutePartitions.ROUTE_DEFAULT)
    handlePartition(httpScanner, RoutePartitions.ROUTE_HTTP)
    handlePartition(uriScanner, RoutePartitions.ROUTE_URI)
    handlePartition(actionScanner, RoutePartitions.ROUTE_ACTION)
    handlePartition(commentScanner, RoutePartitions.ROUTE_COMMENT)

    reconciler
  }
  
  override def getContentFormatter(viewer: ISourceViewer) = {
    val formatter = new MultiPassContentFormatter(getConfiguredDocumentPartitioning(viewer), IDocument.DEFAULT_CONTENT_TYPE)
    formatter.setMasterStrategy(new RouteFormattingStrategy(routeEditor))
    formatter
  }

  def handlePropertyChangeEvent(event: PropertyChangeEvent) {
    scanner.adaptToPreferenceChange(event)
    uriScanner.adaptToPreferenceChange(event)
    actionScanner.adaptToPreferenceChange(event)
    commentScanner.adaptToPreferenceChange(event)
  }
}