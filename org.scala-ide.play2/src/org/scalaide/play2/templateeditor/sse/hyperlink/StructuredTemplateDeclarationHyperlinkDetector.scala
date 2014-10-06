package org.scalaide.play2.templateeditor.sse.hyperlink

import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.scalaide.play2.templateeditor.hyperlink.TemplateDeclarationHyperlinkDetector
import org.scalaide.play2.util.StoredEditorUtils


class StructuredTemplateDeclarationHyperlinkDetector extends AbstractHyperlinkDetector {

  /** We create a new detector every time because we need to set the context (the corresponding editor) only once.
   *  (setContext throws an exception if it's called more than once). We could force a way to initialize this only
   *  once at the expense of additional state in this class, but seems overkill.
   *
   *  Since the detector is a lightweight class (only one field), we take this approach.
   */
  private def templateDeclHyperlinkDetector() = TemplateDeclarationHyperlinkDetector()

  final override def detectHyperlinks(viewer: ITextViewer, currentSelection: IRegion, canShowMultipleHyperlinks: Boolean): Array[IHyperlink] = {
    // make sure the context is correct
    val detector = templateDeclHyperlinkDetector
    StoredEditorUtils.getEditorOfViewer(viewer: ITextViewer).map(detector.setContext)
	  detector.detectHyperlinks(viewer, currentSelection, canShowMultipleHyperlinks)
  }
}