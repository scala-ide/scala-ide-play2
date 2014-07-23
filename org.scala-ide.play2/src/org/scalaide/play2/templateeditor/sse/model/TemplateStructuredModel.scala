package org.scalaide.play2.templateeditor.sse.model

import org.eclipse.wst.html.core.internal.document.DOMStyleModelImpl
import org.eclipse.wst.html.core.internal.encoding.HTMLModelLoader
import org.eclipse.wst.sse.core.internal.document.IDocumentLoader
import org.eclipse.wst.sse.core.internal.provisional.IModelLoader
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel

// This class (and the inner classes) will need to be overhauled when adding HTMLValidator support
class TemplateStructuredModel extends DOMStyleModelImpl {
  import org.eclipse.wst.xml.core.internal.document.DOMModelImpl
  import org.eclipse.wst.xml.core.internal.document.XMLModelParser
  import org.eclipse.wst.xml.core.internal.document.XMLModelUpdater

  class NestedDOMModelParser(model: DOMModelImpl) extends XMLModelParser(model)
  class NestedDOMModelUpdater(model: DOMModelImpl) extends XMLModelUpdater(model)
  override def createModelParser() = new NestedDOMModelParser(this)
  override def createModelUpdater() = new NestedDOMModelUpdater(this)
}

class TemplateModelLoader extends HTMLModelLoader {
  override def getDocumentLoader(): IDocumentLoader = new TemplateDocumentLoader
  override def newModel(): IStructuredModel = new TemplateStructuredModel
  override def newInstance(): IModelLoader = new TemplateModelLoader
}