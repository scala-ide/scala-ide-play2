package org.scalaide.play2.util

import org.eclipse.jface.text.TextViewer
import org.scalaide.play2.templateeditor.AbstractTemplateEditor
import org.eclipse.jface.text.ITextViewer
import org.eclipse.core.resources.IFile
import org.eclipse.ui.IFileEditorInput
import org.eclipse.jface.text.source.ISourceViewer

object StoredEditorUtils {

  def getFileOfViewer(viewer: ITextViewer): Option[IFile] = {
    getEditorOfViewer(viewer).map(_.getEditorInput).collect{
      case fileEditorInput: IFileEditorInput =>
        fileEditorInput.getFile
    }
  }

  def getEditorOfViewer(viewer: ITextViewer): Option[AbstractTemplateEditor] =
    viewer match {
      case textViewer: TextViewer =>
        Option(textViewer.getData("scalaide.play.editor").asInstanceOf[AbstractTemplateEditor])
      case _ =>
        None
    }
  
  def storeEditorInViewer(sourceViewer: ISourceViewer, editor: AbstractTemplateEditor) {
    sourceViewer match {
      case tv: TextViewer =>
        tv.setData("scalaide.play.editor", editor)
      case _ =>
    }
  }

}