package org.scalaide.play2.routeeditor

import org.eclipse.jface.text.IDocument
import org.eclipse.ui.editors.text.FileDocumentProvider
import org.scalaide.play2.routeeditor.lexical.RouteDocumentPartitioner

class RouteDocumentProvider extends FileDocumentProvider {
  override protected def createDocument(element: Object): IDocument = {
    val document = super.createDocument(element);
    if (document != null) {
      val partitioner = new RouteDocumentPartitioner()
      partitioner.connect(document)
      document.setDocumentPartitioner(partitioner)
    }
    document
  }
}