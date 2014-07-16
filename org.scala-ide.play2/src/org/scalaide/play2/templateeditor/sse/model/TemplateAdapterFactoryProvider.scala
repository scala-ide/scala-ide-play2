package org.scalaide.play2.templateeditor.sse.model

import org.eclipse.wst.html.ui.internal.contentoutline.JFaceNodeAdapterFactoryForHTML
import org.eclipse.wst.jsdt.web.core.javascript.JsTranslationAdapterFactory
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.IDocumentTypeHandler
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter
import org.eclipse.wst.sse.ui.internal.provisional.registry.AdapterFactoryProvider
import org.eclipse.wst.sse.ui.internal.util.Assert

class TemplateAdapterFactoryProvider extends AdapterFactoryProvider {
  
  override def addAdapterFactories(structuredModel: IStructuredModel) = {
    val factoryRegistry = structuredModel.getFactoryRegistry()
    Assert.isNotNull(factoryRegistry, "Program error: client caller must ensure model has factory registry")
    if (factoryRegistry.getFactoryFor(classOf[IJFaceNodeAdapter]) == null) {
      factoryRegistry.addFactory(new JFaceNodeAdapterFactoryForHTML)
    }
    if (factoryRegistry.getFactoryFor(classOf[org.eclipse.wst.jsdt.web.core.javascript.IJsTranslation]) == null) {
      factoryRegistry.addFactory(new JsTranslationAdapterFactory)
    }
  }

  override def isFor(contentTypeDescription: IDocumentTypeHandler) = contentTypeDescription match {
    case _: TemplateModelHandler => true
    case _ => false
  }
  
  override def reinitializeFactories(structuredModel: IStructuredModel) = { }
}