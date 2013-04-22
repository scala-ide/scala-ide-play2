package org.scalaide.play2.quickassist

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.swt.graphics.Image
import org.eclipse.jface.text.IDocument

case class BaseAssistProposal(display: String, image: Image = null)(f: IDocument => Unit) extends IJavaCompletionProposal {
  override def getRelevance = 100
  override def getDisplayString() = display
  override def getAdditionalProposalInfo() = null
  override def getImage() = image
  override def apply(doc: IDocument) = f(doc)
  override def getContextInformation() = null
  override def getSelection(doc: IDocument): org.eclipse.swt.graphics.Point = null
}