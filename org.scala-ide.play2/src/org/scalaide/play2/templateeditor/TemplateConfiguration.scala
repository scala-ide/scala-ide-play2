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
import org.scalaide.play2.templateeditor.lexical.TemplateDefaultScanner
import org.scalaide.play2.templateeditor.hyperlink.LocalTemplateHyperlinkComputer
import org.eclipse.jface.text.ITextHover

class TemplateConfiguration(prefStore: IPreferenceStore, templateEditor: TemplateEditor) extends TextSourceViewerConfiguration with PropertyChangeHandler {

  val colorManager = new JavaColorManager()
  private val templateDoubleClickStrategy =
    new RouteDoubleClickStrategy()

  private val defaultScanner = new TemplateDefaultScanner(colorManager, prefStore)

  private val plainScanner = new SingleTokenScanner(TemplateSyntaxClasses.PLAIN, colorManager, prefStore)

  private val scalaScanner = new ScalaCodeScanner(colorManager, prefStore, ScalaVersions.DEFAULT)

  private val commentScanner = new SingleTokenScanner(TemplateSyntaxClasses.COMMENT, colorManager, prefStore)

  private val tagScanner = new HtmlTagScanner(colorManager, prefStore)

  override def getDoubleClickStrategy(sourceViewer: ISourceViewer, contentType: String) = {
    templateDoubleClickStrategy
  }

  override def getConfiguredContentTypes(sourceViewer: ISourceViewer) = {
    TemplatePartitions.getTypes()
  }

  /** Necessary for hover over annotations
   */
  override def getAnnotationHover(viewer: ISourceViewer): IAnnotationHover = {
    new DefaultAnnotationHover(true)
  }

  /** Necessary for code completion
   */
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
    val localDetector = new LocalTemplateHyperlinkComputer
    if (templateEditor != null) {
      detector.setContext(templateEditor)
      localDetector.setContext(templateEditor)
    }

    Array(detector, localDetector)
  }

  override def getTextHover(sv: ISourceViewer, contentType: String, stateMask: Int): ITextHover = {
    if (contentType.equals(TemplatePartitions.TEMPLATE_SCALA)) {
      val cu = TemplateCompilationUnit.fromEditor(templateEditor)
      new TemplateHover(cu)
    } 
    else new DefaultTextHover(sv)
  }

  // should be added, because this one is called by default one
  override def getTextHover(sv: ISourceViewer, contentType: String) = {
    getTextHover(sv, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
  }

  def handlePropertyChangeEvent(event: PropertyChangeEvent) {
    defaultScanner.adaptToPreferenceChange(event)
    tagScanner.adaptToPreferenceChange(event)
    plainScanner.adaptToPreferenceChange(event)
    scalaScanner.adaptToPreferenceChange(event)
    commentScanner.adaptToPreferenceChange(event)
  }

}