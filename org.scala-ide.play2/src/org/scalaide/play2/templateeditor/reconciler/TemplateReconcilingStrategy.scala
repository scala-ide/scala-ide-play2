package org.scalaide.play2.templateeditor.reconciler

import scala.tools.eclipse.logging.HasLogger

import org.eclipse.jface.text.DocumentEvent
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IDocumentListener
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.reconciler.DirtyRegion
import org.eclipse.jface.text.reconciler.IReconcilingStrategy
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.scalaide.play2.templateeditor.TemplateEditor

class TemplateReconcilingStrategy(textEditor: TemplateEditor) extends IReconcilingStrategy with HasLogger {
  private var document: IDocument = _

  private lazy val templateUnit = TemplateCompilationUnit.fromEditor(textEditor)

  override def setDocument(doc: IDocument) {
    document = doc

    doc.addDocumentListener(reloader)
  }

  override def reconcile(dirtyRegion: DirtyRegion, subRegion: IRegion) {
    logger.debug("Incremental reconciliation not implemented.")
  }

  override def reconcile(partition: IRegion) {
    val errors = templateUnit.reconcile(document.get)
    textEditor.updateErrorAnnotations(errors)
  }

  /**
   * Ask the underlying unit to reload on each document change event.
   *
   *  This is certainly wasteful, but otherwise the AST trees are not up to date
   *  in the interval between the last keystroke and reconciliation (which has a delay of
   *  500ms usually). The user can be quick and ask for completions in this interval, and get
   *  wrong results.
   */
  private object reloader extends IDocumentListener {
    def documentChanged(event: DocumentEvent) {
      templateUnit.updateTemplateSourceFile()
      textEditor.getViewer.invalidateTextPresentation()
    }

    def documentAboutToBeChanged(event: DocumentEvent) {}
  }
}