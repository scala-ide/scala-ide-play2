package org.scalaide.play2.templateeditor.sse

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.wst.sse.ui.StructuredTextEditor
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.templateeditor.AbstractTemplateEditor
import org.eclipse.jface.text.source.IVerticalRuler
import org.eclipse.swt.widgets.Composite
import org.eclipse.jface.text.TextViewer
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.text.IDocumentListener
import org.eclipse.jface.text.DocumentEvent
import org.eclipse.jface.text.ITextListener
import org.eclipse.jface.text.ITextInputListener
import org.eclipse.jface.text.IDocument

import scala.concurrent._

class TemplateStructuredEditor extends StructuredTextEditor with AbstractTemplateEditor {

  override protected lazy val preferenceStore: IPreferenceStore =
    new ChainedPreferenceStore(Array((EditorsUI.getPreferenceStore()), PlayPlugin.preferenceStore))

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
      import ExecutionContext.Implicits._
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

