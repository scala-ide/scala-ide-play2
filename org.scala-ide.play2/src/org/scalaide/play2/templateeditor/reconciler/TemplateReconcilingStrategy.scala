package org.scalaide.play2.templateeditor.reconciler

import org.eclipse.jface.text.DocumentEvent
import org.eclipse.jface.text.IDocumentListener
import org.scalaide.editor.ReconcilingStrategy
import org.scalaide.editor.SourceCodeEditor
import org.scalaide.play2.templateeditor.AbstractTemplateEditor

private class TemplateReconcilingStrategy(templateEditor: SourceCodeEditor, documentListener: IDocumentListener) extends ReconcilingStrategy(templateEditor, documentListener)

object TemplateReconcilingStrategy {

  def apply(templateEditor: AbstractTemplateEditor): ReconcilingStrategy = new TemplateReconcilingStrategy(templateEditor, new Reloader(templateEditor))

  private class Reloader(templateEditor: AbstractTemplateEditor) extends IDocumentListener {
    def documentChanged(event: DocumentEvent) {
      templateEditor.compilationUnitProvider.fromEditor(templateEditor).askReload()
    }

    def documentAboutToBeChanged(event: DocumentEvent) {}
  }
}