package org.scalaide.play2.templateeditor.sse

import org.eclipse.wst.html.ui.internal.edit.ui.ActionContributorHTML

class TemplateActionContributor extends ActionContributorHTML {
  // Add refactoring actions ("RetargetTextEditorAction") here if needed
  
  override protected def getExtensionIDs(): Array[String] =
    Array(ContentTypeIdForScala.ContentTypeID_Scala, "org.eclipse.wst.sse.ui.StructuredTextEditor")
}