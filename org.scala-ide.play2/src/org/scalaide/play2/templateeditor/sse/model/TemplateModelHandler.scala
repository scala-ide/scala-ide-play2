package org.scalaide.play2.templateeditor.sse.model

import org.eclipse.jst.jsp.core.internal.encoding.JSPDocumentHeadContentDetector
import org.eclipse.wst.sse.core.internal.document.IDocumentCharsetDetector
import org.eclipse.wst.sse.core.internal.document.IDocumentLoader
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.AbstractModelHandler
import org.eclipse.wst.sse.core.internal.provisional.IModelLoader

object TemplateModelHandler {
  val AssociatedContentTypeID = "org.scalaide.play2.templateSource"
  val ModelHandlerID = "org.scalaide.play2.templateModelHandler"
}

class TemplateModelHandler extends AbstractModelHandler() {
  setId(TemplateModelHandler.ModelHandlerID)
  setAssociatedContentTypeId(TemplateModelHandler.AssociatedContentTypeID)

  override def getEncodingDetector(): IDocumentCharsetDetector = new JSPDocumentHeadContentDetector
  override def getDocumentLoader(): IDocumentLoader = new TemplateDocumentLoader
  override def getModelLoader(): IModelLoader = new TemplateModelLoader
}
