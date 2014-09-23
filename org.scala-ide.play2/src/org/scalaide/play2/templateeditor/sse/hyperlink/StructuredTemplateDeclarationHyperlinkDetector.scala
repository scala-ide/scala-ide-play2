package org.scalaide.play2.templateeditor.sse.hyperlink

import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.scalaide.play2.templateeditor.hyperlink.TemplateDeclarationHyperlinkDetector
import org.scalaide.play2.util.StoredEditorUtils


class StructuredTemplateDeclarationHyperlinkDetector extends AbstractHyperlinkDetector {
  protected val templateDeclHyperlinkDetector = TemplateDeclarationHyperlinkDetector()
  
  final override def detectHyperlinks(viewer: ITextViewer, currentSelection: IRegion, canShowMultipleHyperlinks: Boolean): Array[IHyperlink] = {
    StoredEditorUtils.getEditorOfViewer(viewer).map{ textEditor =>
    	templateDeclHyperlinkDetector.detectHyperlinks(textEditor, currentSelection, canShowMultipleHyperlinks)
    } getOrElse(Array())
  } 
}