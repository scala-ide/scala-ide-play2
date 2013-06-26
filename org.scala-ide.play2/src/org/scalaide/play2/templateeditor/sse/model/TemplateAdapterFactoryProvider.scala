package org.scalaide.play2.templateeditor.sse.model

import org.eclipse.wst.sse.ui.internal.provisional.registry.AdapterFactoryProvider
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel
import org.eclipse.wst.sse.ui.internal.util.Assert
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter
import org.eclipse.wst.html.ui.internal.contentoutline.JFaceNodeAdapterFactoryForHTML
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.IDocumentTypeHandler

class TemplateAdapterFactoryProvider extends AdapterFactoryProvider {
  override def addAdapterFactories(structuredModel: IStructuredModel) = {
    val factoryRegistry = structuredModel.getFactoryRegistry()
    Assert.isNotNull(factoryRegistry, "Program error: client caller must ensure model has factory registry")
    if (factoryRegistry.getFactoryFor(classOf[IJFaceNodeAdapter]) == null) {
      factoryRegistry.addFactory(new JFaceNodeAdapterFactoryForHTML)
    }
  }

  override def isFor(contentTypeDescription: IDocumentTypeHandler) = contentTypeDescription match {
    case _: TemplateModelHandler => true
    case _ => false
  }
  
  override def reinitializeFactories(structuredModel: IStructuredModel) = { }
}