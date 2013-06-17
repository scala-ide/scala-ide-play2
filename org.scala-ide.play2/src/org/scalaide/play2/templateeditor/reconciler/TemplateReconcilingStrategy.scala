package org.scalaide.play2.templateeditor.reconciler

import org.eclipse.jface.text.DocumentEvent
import org.eclipse.jface.text.IDocumentListener
import org.scalaide.editor.ReconcilingStrategy
import org.scalaide.editor.SourceCodeEditor
import org.scalaide.play2.templateeditor.TemplateEditor
import org.scalaide.play2.templateeditor.TTemplateEditor
import scala.util.Try

private class TemplateReconcilingStrategy(templateEditor: SourceCodeEditor, documentListener: IDocumentListener) extends ReconcilingStrategy(templateEditor, documentListener)

object TemplateReconcilingStrategy {

  def apply(templateEditor: TTemplateEditor): ReconcilingStrategy = new TemplateReconcilingStrategy(templateEditor, new Reloader(templateEditor))

  private class Reloader(templateEditor: TTemplateEditor) extends IDocumentListener {
    def documentChanged(event: DocumentEvent) {
      templateEditor.compilationUnitProvider.fromEditor(templateEditor).askReload()
      // FIXME: Couldn't this be a call to `askReload` like in the worksheet plugin?
      // templateEditor.getInteractiveCompilationUnit.updateTemplateSourceFile()
      // FIXME: This looks fishy: Why do we need to invalidate the text presentation?
      // templateEditor.getViewer.invalidateTextPresentation()
    }

    def documentAboutToBeChanged(event: DocumentEvent) {}
  }
}