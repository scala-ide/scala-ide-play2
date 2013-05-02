package org.scalaide.play2.routeeditor

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.ContentAssistant
import org.eclipse.jface.text.contentassist.IContentAssistant
import org.eclipse.jface.text.formatter.MultiPassContentFormatter
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector
import org.eclipse.jface.text.presentation.IPresentationReconciler
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.jface.util.PropertyChangeEvent
import org.scalaide.play2.properties.PropertyChangeHandler
import org.scalaide.play2.routeeditor.completion.HttpMethodCompletionComputer
import org.scalaide.play2.routeeditor.completion.UriCompletionComputer
import org.scalaide.play2.routeeditor.formatter.RouteFormattingStrategy
import org.scalaide.play2.routeeditor.hyperlink.RouteHyperlinkDetector
import org.scalaide.play2.routeeditor.lexical.RoutePartitions

class RouteConfiguration(prefStore: IPreferenceStore, routeEditor: RouteEditor) extends SourceViewerConfiguration with PropertyChangeHandler {
  private val reconciler: RoutePresentationReconciler = new RoutePresentationReconciler(prefStore)

  override def getDoubleClickStrategy(sourceViewer: ISourceViewer, contentType: String) = {
    new RouteDoubleClickStrategy()
  }

  override def getConfiguredContentTypes(sourceViewer: ISourceViewer) = {
    RoutePartitions.getTypes
  }

  override def getContentAssistant(sourceViewer: ISourceViewer): IContentAssistant = {
    val assistant = new ContentAssistant
    assistant.setAutoActivationDelay(50)
    assistant.enableAutoActivation(true)
    assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer))
    assistant.setContentAssistProcessor(new HttpMethodCompletionComputer, RoutePartitions.ROUTE_HTTP)
    assistant.setContentAssistProcessor(new UriCompletionComputer, RoutePartitions.ROUTE_URI)
    assistant
  }

  override def getHyperlinkDetectors(sourceViewer: ISourceViewer): Array[IHyperlinkDetector] = Array(new RouteHyperlinkDetector(routeEditor))

  override def getPresentationReconciler(sourceViewer: ISourceViewer): IPresentationReconciler = reconciler
  
  override def getContentFormatter(viewer: ISourceViewer) = {
    val formatter = new MultiPassContentFormatter(getConfiguredDocumentPartitioning(viewer), IDocument.DEFAULT_CONTENT_TYPE)
    formatter.setMasterStrategy(new RouteFormattingStrategy(routeEditor))
    formatter
  }

  override def handlePropertyChangeEvent(event: PropertyChangeEvent): Unit = reconciler.handlePropertyChangeEvent(event)
}