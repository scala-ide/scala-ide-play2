package org.scalaide.play2.templates

import scala.tools.eclipse.contribution.weaving.jdt.ui.document.IMasterProjectionDocumentProvider
import org.eclipse.jface.text.IDocument
import org.scalaide.play2.templateeditor.sse.model.TemplateStructuredDocument

class MasterProjectionDocumentProvider extends IMasterProjectionDocumentProvider {
  override def extractActualMaster(master: IDocument) = master match {
    case master @ TemplateStructuredDocument(_, _) => master.delegate
    case _ => master
  }
}