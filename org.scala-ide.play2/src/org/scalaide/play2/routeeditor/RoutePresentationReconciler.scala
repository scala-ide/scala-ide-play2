package org.scalaide.play2.routeeditor

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.presentation.PresentationReconciler
import org.eclipse.jface.text.rules.DefaultDamagerRepairer
import org.eclipse.jface.text.rules.ITokenScanner
import org.eclipse.jface.util.PropertyChangeEvent
import org.scalaide.play2.routeeditor.lexical.RouteActionScanner
import org.scalaide.play2.routeeditor.lexical.RoutePartitions
import org.scalaide.play2.routeeditor.lexical.RouteURIScanner
import org.eclipse.jface.util.IPropertyChangeListener
import org.scalaide.core.lexical.ScalaCodeScanners

class RoutePresentationReconciler(prefStore: IPreferenceStore) extends PresentationReconciler with IPropertyChangeListener {

  private val scanner =
    ScalaCodeScanners.singleTokenScanner(prefStore, RouteSyntaxClasses.DEFAULT)

  private val httpScanner =
    ScalaCodeScanners.singleTokenScanner(prefStore, RouteSyntaxClasses.HTTP_KEYWORD)

  private val uriScanner =
    new RouteURIScanner(prefStore)

  private val actionScanner =
    new RouteActionScanner(prefStore)

  private val commentScanner =
    ScalaCodeScanners.singleTokenScanner(prefStore, RouteSyntaxClasses.COMMENT)

  handlePartition(scanner, RoutePartitions.ROUTE_DEFAULT)
  handlePartition(httpScanner, RoutePartitions.ROUTE_HTTP)
  handlePartition(uriScanner, RoutePartitions.ROUTE_URI)
  handlePartition(actionScanner, RoutePartitions.ROUTE_ACTION)
  handlePartition(commentScanner, RoutePartitions.ROUTE_COMMENT)

  private def handlePartition(scan: ITokenScanner, token: String) = {
    val dr = new DefaultDamagerRepairer(scan);
    setDamager(dr, token)
    setRepairer(dr, token)
  }
  
  override def propertyChange(event: PropertyChangeEvent) {
    scanner.adaptToPreferenceChange(event)
    httpScanner.adaptToPreferenceChange(event)
    uriScanner.adaptToPreferenceChange(event)
    actionScanner.adaptToPreferenceChange(event)
    commentScanner.adaptToPreferenceChange(event)
  }
}