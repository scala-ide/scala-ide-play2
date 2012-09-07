package org.scalaide.play2.templateeditor

import scala.tools.eclipse.lexical.ScalaCodeScanner
import scala.tools.eclipse.lexical.SingleTokenScanner
import org.eclipse.jdt.internal.ui.text.JavaColorManager
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.presentation.PresentationReconciler
import org.eclipse.jface.text.reconciler.IReconciler
import org.eclipse.jface.text.reconciler.MonoReconciler
import org.eclipse.jface.text.rules.DefaultDamagerRepairer
import org.eclipse.jface.text.rules.ITokenScanner
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration
import org.scalaide.play2.routeeditor.RouteDoubleClickStrategy
import org.scalaide.play2.templateeditor.reconciler.TemplateReconcilingStrategy
import org.scalaide.play2.templateeditor.lexical.HtmlTagScanner
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions
import scalariform.ScalaVersions
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.jface.text.source.IAnnotationHover
import org.eclipse.jface.text.source.DefaultAnnotationHover
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector
import scala.tools.eclipse.hyperlink.text.detector.DeclarationHyperlinkDetector
import org.scalaide.play2.templateeditor.hyperlink.TemplateDeclarationHyperlinkDetector
import org.scalaide.play2.templateeditor.hover.TemplateHover
import org.eclipse.jface.text.DefaultTextHover
import org.eclipse.jface.text.ITextViewerExtension2
import org.eclipse.jface.text.contentassist.IContentAssistant
import org.eclipse.jface.text.contentassist.ContentAssistant
import org.scalaide.play2.templateeditor.completion.CompletionProposalComputer
import org.eclipse.jface.text.IDocument
import org.scalaide.play2.properties.PropertyChangeHandler

class TemplateConfiguration(prefStore: IPreferenceStore, templateEditor: TemplateEditor) extends TextSourceViewerConfiguration with PropertyChangeHandler {

  val colorManager = new JavaColorManager()
  private val templateDoubleClickStrategy =
    new RouteDoubleClickStrategy()

  private val defaultScanner = new SingleTokenScanner(TemplateSyntaxClasses.DEFAULT, colorManager, prefStore)
  
  private val plainScanner: SingleTokenScanner = {
    val result = new SingleTokenScanner(TemplateSyntaxClasses.PLAIN, colorManager, prefStore)
    result
  }
  private val scalaScanner: ScalaCodeScanner = {
    val result = new ScalaCodeScanner(colorManager, prefStore, ScalaVersions.DEFAULT)
    result
  }
  private val commentScanner: SingleTokenScanner = {
    val result = new SingleTokenScanner(TemplateSyntaxClasses.COMMENT, colorManager, prefStore)
    result
  }
  private val tagScanner: HtmlTagScanner = {
    val result = new HtmlTagScanner(colorManager, prefStore)
    result
  }

  override def getDoubleClickStrategy(sourceViewer: ISourceViewer, contentType: String) = {
    templateDoubleClickStrategy
  }

  override def getConfiguredContentTypes(sourceViewer: ISourceViewer) = {
    TemplatePartitions.getTypes()
  }

  override def getAnnotationHover(viewer: ISourceViewer): IAnnotationHover = {
    new DefaultAnnotationHover(true)

  }

  override def getContentAssistant(sourceViewer: ISourceViewer): IContentAssistant = {
    val assistant = new ContentAssistant
    assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer))
    assistant.setContentAssistProcessor(new CompletionProposalComputer(templateEditor), TemplatePartitions.TEMPLATE_SCALA)
    assistant.setContentAssistProcessor(new CompletionProposalComputer(templateEditor), TemplatePartitions.TEMPLATE_PLAIN)
    assistant.setContentAssistProcessor(new CompletionProposalComputer(templateEditor), IDocument.DEFAULT_CONTENT_TYPE)
    assistant
  }

  override def getPresentationReconciler(
    sourceViewer: ISourceViewer) = {
    val reconciler = super.getPresentationReconciler(sourceViewer).asInstanceOf[PresentationReconciler]
    def handlePartition(scan: ITokenScanner, token: String) = {

      val dr = new DefaultDamagerRepairer(scan);
      reconciler.setDamager(dr, token);
      reconciler.setRepairer(dr, token);
    }
    handlePartition(defaultScanner, TemplatePartitions.TEMPLATE_DEFAULT)
    handlePartition(plainScanner, TemplatePartitions.TEMPLATE_PLAIN)
    handlePartition(scalaScanner, TemplatePartitions.TEMPLATE_SCALA)
    handlePartition(commentScanner, TemplatePartitions.TEMPLATE_COMMENT)
    handlePartition(tagScanner, TemplatePartitions.TEMPLATE_TAG)

    reconciler
  }

  lazy val strategy = new TemplateReconcilingStrategy(templateEditor)

  override def getReconciler(sourceViewer: ISourceViewer): IReconciler = {
    val reconciler = new MonoReconciler(strategy, /*isIncremental = */ false)
    reconciler.install(sourceViewer)
    reconciler
  }

  override def getHyperlinkDetectors(sv: ISourceViewer): Array[IHyperlinkDetector] = {
    val detector = TemplateDeclarationHyperlinkDetector()
    if (templateEditor != null) detector.setContext(templateEditor)
    Array(detector)
  }

  override def getTextHover(sv: ISourceViewer, contentType: String, stateMask: Int) = {
    if (contentType.equals(TemplatePartitions.TEMPLATE_SCALA)) {
      TemplateCompilationUnit.fromEditor(templateEditor) match {
        case Some(tcu) => new TemplateHover(tcu)
        case None => new DefaultTextHover(sv)
      }
    } else {
      new DefaultTextHover(sv)
    }
  }

  // should be added, because this one is called by default one
  override def getTextHover(sv: ISourceViewer, contentType: String) = {
    getTextHover(sv, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
  }

  def handlePropertyChangeEvent(event: PropertyChangeEvent) {
    plainScanner.adaptToPreferenceChange(event)
    scalaScanner.adaptToPreferenceChange(event)
    commentScanner.adaptToPreferenceChange(event)
  }

}