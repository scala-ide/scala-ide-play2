package org.scalaide.play2.quickassist

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.swt.graphics.Image
import org.eclipse.jface.text.IDocument
import org.scalaide.core.quickassist.BasicCompletionProposal

case class AddRouteEntryProposal(display: String, image: Image = null)(f: IDocument => Unit) extends BasicCompletionProposal(100, display, image) {
  override def apply(doc: IDocument) = f(doc)
}