package org.scalaide.play2.templateeditor.sse.hyperlink

import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.scalaide.editor.util.EditorHelper
import org.scalaide.play2.templateeditor.hyperlink.TemplateDeclarationHyperlinkDetector


class StructuredTemplateDeclarationHyperlinkDetector extends AbstractHyperlinkDetector {
  val templateDeclHyperlinkDetector = TemplateDeclarationHyperlinkDetector()
  
  final override def detectHyperlinks(viewer: ITextViewer, currentSelection: IRegion, canShowMultipleHyperlinks: Boolean): Array[IHyperlink] = {
    val textEditor = EditorHelper.findEditorOfDocument(viewer.getDocument()) getOrElse null
    templateDeclHyperlinkDetector.detectHyperlinks(textEditor, currentSelection, canShowMultipleHyperlinks)
  } 
}