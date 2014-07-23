package org.scalaide.play2.templateeditor

import org.eclipse.jface.text.IDocument
import org.eclipse.ui.editors.text.FileDocumentProvider
import org.scalaide.play2.templateeditor.lexical.TemplateDocumentPartitioner

class TemplateDocumentProvider extends FileDocumentProvider/* with IAnnotationModelFactory */{
  protected override def createDocument(element: Object): IDocument = {
    val document = super.createDocument(element);
    if (document != null) {
      val partitioner = new TemplateDocumentPartitioner(true)
      partitioner.connect(document)
      document.setDocumentPartitioner(partitioner)
    }
    document
  }

} 