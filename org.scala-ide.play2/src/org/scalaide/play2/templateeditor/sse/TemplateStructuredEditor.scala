package org.scalaide.play2.templateeditor.sse

import scala.concurrent.Future

import org.eclipse.core.resources.IFile
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.DocumentEvent
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IDocumentListener
import org.eclipse.jface.text.ITextInputListener
import org.eclipse.jface.text.TextViewer
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.text.source.IVerticalRuler
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.IEditorInput
import org.eclipse.ui.IEditorSite
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.wst.html.ui.internal.HTMLUIPlugin
import org.eclipse.wst.sse.ui.StructuredTextEditor
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.templateeditor.AbstractTemplateEditor
import org.scalaide.play2.templateeditor.processing.TemplateVersionExhibitor
import org.scalaide.play2.templateeditor.processing.TemplateVersionExtractor

class TemplateStructuredEditor extends StructuredTextEditor with AbstractTemplateEditor {

  override protected lazy val preferenceStore: IPreferenceStore =
    new ChainedPreferenceStore(Array(HTMLUIPlugin.getDefault.getPreferenceStore, EditorsUI.getPreferenceStore, PlayPlugin.preferenceStore))

  /* This is a nasty hack.
   * The problem:  The TemplateStructuredTextViewerConfiguration needs the pref store and a reference to the editor.
   *               However, the viewer configuration is instantiated through an extension point, so we don't have the opportunity to give it the pref store and a reference to the editor.
   * The solution: Intercept the instance of the TemplateStructuredTextViewerConfiguration and inject it/create a new one with the
   *               pref store and reference to the editor (self)
   * Note: the TemplateStructuredTextViewerConfiguration has additional logic to support this hack.
   */
  override def setSourceViewerConfiguration(config: SourceViewerConfiguration) = {
    config match {
      case templateConfig: TemplateStructuredTextViewerConfiguration =>
        templateConfig.initialize(preferenceStore, this)
      case _ =>
    }
    super.setSourceViewerConfiguration(config)
  }

  override def init(site: IEditorSite, input: IEditorInput): Unit = {
    TemplateVersionExhibitor.set(TemplateVersionExtractor.fromIFile(input.getAdapter(classOf[IFile]).asInstanceOf[IFile]))
    super.init(site, input)
  }

  override def createSourceViewer(parent: Composite, verticalRuler: IVerticalRuler, styles: Int): ISourceViewer = {
    val sourceViewer = super.createSourceViewer(parent, verticalRuler, styles)
    sourceViewer match {
      case tv: TextViewer =>
        tv.setData("scalaide.play.editor", this)
      case _ =>
    }
    getInteractiveCompilationUnit().initialReconcile()
    sourceViewer.addTextInputListener(textInputListener)
    sourceViewer
  }

  object documentListener extends IDocumentListener {
    override def documentChanged(event: DocumentEvent) {
      import scala.concurrent.ExecutionContext.Implicits._
      Future { getInteractiveCompilationUnit().scheduleReconcile(event.getDocument.get.toCharArray) }
    }

    override def documentAboutToBeChanged(event: DocumentEvent) {}
  }

  object textInputListener extends ITextInputListener {
    def inputDocumentAboutToBeChanged(oldInput: IDocument, newInput: IDocument): Unit = {
      if (oldInput ne null)
        oldInput.removeDocumentListener(documentListener)
    }

    def inputDocumentChanged(oldInput: IDocument, newInput: IDocument): Unit = {
      getInteractiveCompilationUnit().initialReconcile()
      if (newInput ne null) newInput.addDocumentListener(documentListener)
    }
  }
}

