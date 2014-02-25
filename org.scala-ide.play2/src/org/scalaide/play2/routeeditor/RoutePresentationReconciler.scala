package org.scalaide.play2.routeeditor

import org.scalaide.core.internal.lexical.SingleTokenScanner

import org.eclipse.jdt.internal.ui.text.JavaColorManager
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.presentation.PresentationReconciler
import org.eclipse.jface.text.rules.DefaultDamagerRepairer
import org.eclipse.jface.text.rules.ITokenScanner
import org.eclipse.jface.util.PropertyChangeEvent
import org.scalaide.play2.properties.PropertyChangeHandler
import org.scalaide.play2.routeeditor.lexical.RouteActionScanner
import org.scalaide.play2.routeeditor.lexical.RoutePartitions
import org.scalaide.play2.routeeditor.lexical.RouteURIScanner

class RoutePresentationReconciler(prefStore: IPreferenceStore) extends PresentationReconciler with PropertyChangeHandler {

  private val scanner =
    new SingleTokenScanner(RouteSyntaxClasses.DEFAULT, prefStore)

  private val httpScanner =
    new SingleTokenScanner(RouteSyntaxClasses.HTTP_KEYWORD, prefStore)

  private val uriScanner =
    new RouteURIScanner(prefStore)

  private val actionScanner =
    new RouteActionScanner(prefStore)

  private val commentScanner =
    new SingleTokenScanner(RouteSyntaxClasses.COMMENT, prefStore)

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
  
  override def handlePropertyChangeEvent(event: PropertyChangeEvent) {
    scanner.adaptToPreferenceChange(event)
    httpScanner.adaptToPreferenceChange(event)
    uriScanner.adaptToPreferenceChange(event)
    actionScanner.adaptToPreferenceChange(event)
    commentScanner.adaptToPreferenceChange(event)
  }
}