package org.scalaide.play2.templateeditor

import org.eclipse.core.filebuffers.IAnnotationModelFactory
import org.eclipse.core.runtime.IPath
import org.eclipse.jface.text.IDocument
import org.eclipse.ui.IFileEditorInput
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

  override def getAnnotationModel(element: Object) = {
    val result = super.getAnnotationModel(element)
    result
  }

} 