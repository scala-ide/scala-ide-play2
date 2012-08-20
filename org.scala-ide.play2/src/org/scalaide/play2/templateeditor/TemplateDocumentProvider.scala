package org.scalaide.play2.templateeditor

import org.eclipse.core.filebuffers.IAnnotationModelFactory
import org.eclipse.core.runtime.IPath
import org.eclipse.jface.text.IDocument
import org.eclipse.ui.IFileEditorInput
import org.eclipse.ui.editors.text.FileDocumentProvider
import org.scalaide.play2.templateeditor.scanners.TemplateDocumentPartitioner

class TemplateDocumentProvider extends FileDocumentProvider/* with IAnnotationModelFactory */{
  protected override def createDocument(element: Object): IDocument = {
    val document = super.createDocument(element);
    //    connect(element)
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

//  override def createAnnotationModel(element: Object) = {
//    if (element.isInstanceOf[IFileEditorInput]) {
//      val input = element.asInstanceOf[IFileEditorInput];
//      val path = input.getFile().getFullPath()
//      createAnnotationModel(path)
//    } else
//      super.createAnnotationModel(element);
//  }

/*  override def createAnnotationModel(path: IPath) = {
    TemplateAnnotationModelFactory.createAnnotationModel(path)
  }*/

  //  override def getAnnotationModel(element: Object) = {
  //  val file= ResourcesPlugin.getWorkspace().getRoot().findMember(path);
  //		if (file instanceof IFile)
  //			return new CompilationUnitAnnotationModel(file);
  //		return new AnnotationModel();
  //  }

} 