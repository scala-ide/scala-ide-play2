package org.scalaide.play2.templateeditor

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
import org.scalaide.play2.templateeditor.hyperlink.TemplateDeclarationHyperlinkDetector
import org.eclipse.jface.text.DefaultTextHover
import org.eclipse.jface.text.ITextViewerExtension2
import org.eclipse.jface.text.contentassist.IContentAssistant
import org.eclipse.jface.text.contentassist.ContentAssistant
import org.scalaide.play2.templateeditor.completion.CompletionProposalComputer
import org.eclipse.jface.text.IDocument
import org.scalaide.play2.templateeditor.lexical.TemplateDefaultScanner
import org.scalaide.play2.templateeditor.hyperlink.LocalTemplateHyperlinkComputer
import org.eclipse.jface.text.ITextHover
import org.eclipse.jface.text.IAutoEditStrategy
import org.eclipse.jdt.ui.text.IJavaPartitions
import org.eclipse.jdt.internal.ui.text.java.SmartSemicolonAutoEditStrategy
import org.eclipse.jface.text.source.Annotation
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants
import org.eclipse.jface.util.IPropertyChangeListener
import org.scalaide.core.lexical.ScalaCodeScanners
import org.scalaide.ui.editor.hover.IScalaHover

class TemplateConfiguration(prefStore: IPreferenceStore, templateEditor: AbstractTemplateEditor)
  extends TextSourceViewerConfiguration(prefStore)
  with IPropertyChangeListener {

  private val templateDoubleClickStrategy =
    new RouteDoubleClickStrategy()

  private val defaultScanner = new TemplateDefaultScanner(prefStore)

  private val plainScanner = ScalaCodeScanners.singleTokenScanner(prefStore, TemplateSyntaxClasses.PLAIN)

  private val scalaScanner = ScalaCodeScanners.scalaCodeScanner(prefStore, ScalaVersions.Scala_2_10)

  private val commentScanner = ScalaCodeScanners.singleTokenScanner(prefStore, TemplateSyntaxClasses.COMMENT)

  private val tagScanner = new HtmlTagScanner(prefStore)

  override def getDoubleClickStrategy(sourceViewer: ISourceViewer, contentType: String) = {
    templateDoubleClickStrategy
  }

  override def getConfiguredContentTypes(sourceViewer: ISourceViewer) = {
    TemplatePartitions.getTypes()
  }

  /** Necessary for code completion
   */
  override def getContentAssistant(sourceViewer: ISourceViewer): IContentAssistant = {
    val assistant = new ContentAssistant
    assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer))
    assistant.setContentAssistProcessor(new CompletionProposalComputer(), TemplatePartitions.TEMPLATE_SCALA)
    assistant.setContentAssistProcessor(new CompletionProposalComputer(), TemplatePartitions.TEMPLATE_PLAIN)
    assistant.setContentAssistProcessor(new CompletionProposalComputer(), IDocument.DEFAULT_CONTENT_TYPE)
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

  lazy val strategy = TemplateReconcilingStrategy(templateEditor)

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

  override def getAnnotationHover(viewer: ISourceViewer): IAnnotationHover = {
    new DefaultAnnotationHover(true) {
      override def isIncluded(a: Annotation): Boolean = TemplateEditor.annotationsShownInHover(a.getType)
    }
  }

  override def getTextHover(sv: ISourceViewer, contentType: String, stateMask: Int): ITextHover = {
    if (templateEditor != null && contentType.equals(TemplatePartitions.TEMPLATE_SCALA)) {
      val cu = templateEditor.getInteractiveCompilationUnit()
      IScalaHover(templateEditor)
    }
    else new DefaultTextHover(sv)
  }

  // should be added, because this one is called by default one
  override def getTextHover(sv: ISourceViewer, contentType: String) = {
    getTextHover(sv, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
  }

  /** Add a few auto-edit strategies for Scala code.
   *
   *  @see scala.tools.eclipse.ScalaSourceViewerConfiguration
   */
  override def getAutoEditStrategies(sourceViewer: ISourceViewer, contentType: String): Array[IAutoEditStrategy] = {
    val partitioning = getConfiguredDocumentPartitioning(sourceViewer)
    contentType match {
      case TemplatePartitions.TEMPLATE_SCALA | IDocument.DEFAULT_CONTENT_TYPE =>
        Array(
          new SmartSemicolonAutoEditStrategy(partitioning),
          new BracketAutoEditStrategy(prefStore),
          new TemplateAutoIndentStrategy(getTabWidth(sourceViewer), useSpacesForTabs))
      case _ =>
        Array(new TemplateAutoIndentStrategy(getTabWidth(sourceViewer), useSpacesForTabs))
    }
  }

  private def useSpacesForTabs: Boolean = {
    prefStore != null && prefStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS)
  }

  def propertyChange(event: PropertyChangeEvent) {
    defaultScanner.adaptToPreferenceChange(event)
    tagScanner.adaptToPreferenceChange(event)
    plainScanner.adaptToPreferenceChange(event)
    scalaScanner.adaptToPreferenceChange(event)
    commentScanner.adaptToPreferenceChange(event)
  }

}
