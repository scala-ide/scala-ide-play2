package org.scalaide.play2.templateeditor

import scala.tools.eclipse.lexical.SingleTokenScanner

import org.eclipse.jdt.internal.ui.text.JavaColorManager
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.presentation.PresentationReconciler
import org.eclipse.jface.text.rules.DefaultDamagerRepairer
import org.eclipse.jface.text.rules.ITokenScanner
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.jface.util.PropertyChangeEvent
import org.scalaide.play2.routeeditor.RouteDoubleClickStrategy
import org.scalaide.play2.templateeditor.scanners.TemplatePartitions

class TemplateConfiguration(prefStore: IPreferenceStore, templateEditor: TemplateEditor) extends SourceViewerConfiguration {
  val reconciler = new PresentationReconciler();
  val colorManager = new JavaColorManager()
  private val templateDoubleClickStrategy: RouteDoubleClickStrategy =
    new RouteDoubleClickStrategy()

  private val plainScanner: SingleTokenScanner = {
    val result = new SingleTokenScanner(TemplateSyntaxClasses.PLAIN, colorManager, prefStore)
    result
  }
  private val scalaScanner: SingleTokenScanner = {
    val result = new SingleTokenScanner(TemplateSyntaxClasses.SCALA, colorManager, prefStore)
    result
  }
  private val commentScanner: SingleTokenScanner = {
    val result = new SingleTokenScanner(TemplateSyntaxClasses.COMMENT, colorManager, prefStore)
    result
  }

  override def getDoubleClickStrategy(sourceViewer: ISourceViewer, contentType: String) = {
    templateDoubleClickStrategy
  }

  def handlePartition(partitionType: String, tokenScanner: ITokenScanner) {
    val dr = new DefaultDamagerRepairer(tokenScanner)
    reconciler.setDamager(dr, partitionType)
    reconciler.setRepairer(dr, partitionType)
  }

  override def getConfiguredContentTypes(sourceViewer: ISourceViewer) = {
    TemplatePartitions.getTypes()
  }

  //  override def getHyperlinkDetectors(sourceViewer: ISourceViewer) = { TODO
  //    Array(new RouteHyperlinkDetector(routeEditor));
  //  }

  def handlePartition(scan: ITokenScanner, token: String) = {
    val dr = new DefaultDamagerRepairer(scan);
    reconciler.setDamager(dr, token);
    reconciler.setRepairer(dr, token);
  }

  override def getPresentationReconciler(
    sourceViewer: ISourceViewer) = {

    handlePartition(plainScanner, TemplatePartitions.TEMPLATE_PLAIN)
    handlePartition(scalaScanner, TemplatePartitions.TEMPLATE_SCALA)
    handlePartition(commentScanner, TemplatePartitions.TEMPLATE_COMMENT)

    reconciler
  }

  def handlePropertyChangeEvent(event: PropertyChangeEvent) {
    plainScanner.adaptToPreferenceChange(event)
    scalaScanner.adaptToPreferenceChange(event)
    commentScanner.adaptToPreferenceChange(event)
  }
}