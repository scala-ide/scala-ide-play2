package org.scalaide.play2.templateeditor.sse.model

import org.eclipse.wst.html.core.internal.encoding.HTMLDocumentCharsetDetector
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

  // Perhaps at some point it would be beneficial to have our own documentcharsetdetector.
  // At the moment, I don't when there would be a difference between html and template files.
  override def getEncodingDetector(): IDocumentCharsetDetector = new HTMLDocumentCharsetDetector
  override def getDocumentLoader(): IDocumentLoader = new TemplateDocumentLoader
  override def getModelLoader(): IModelLoader = new TemplateModelLoader
}
