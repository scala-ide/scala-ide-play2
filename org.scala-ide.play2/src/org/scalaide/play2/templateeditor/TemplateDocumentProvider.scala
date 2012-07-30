package org.scalaide.play2.templateeditor

import org.eclipse.jface.text.IDocument
import org.eclipse.ui.editors.text.FileDocumentProvider
import org.scalaide.play2.templateeditor.scanners.TemplateDocumentPartitioner

class TemplateDocumentProvider extends FileDocumentProvider {
  protected override def createDocument(element: Object): IDocument = {
    val document = super.createDocument(element);
    if (document != null) {
      val partitioner = new TemplateDocumentPartitioner()
      partitioner.connect(document)
      document.setDocumentPartitioner(partitioner)
    }
    document
  }
}