package org.scalaide.play2.quickassist

import org.eclipse.jface.text.IDocument

import org.eclipse.swt.graphics.Image
import org.scalaide.core.internal.statistics.Features.Feature
import org.scalaide.core.internal.statistics.Groups.Editing
import org.scalaide.core.quickassist.BasicCompletionProposal

object AddRouteEntry extends Feature("AddRouteEntry")("Add entry to route file", Editing)

case class AddRouteEntryProposal(display: String, image: Image = null)(f: IDocument => Unit) extends BasicCompletionProposal(AddRouteEntry, 100, display, image) {
  override def applyProposal(doc: IDocument) = f(doc)
}
