package org.scalaide.play2.lexical

import org.eclipse.jface.text.TypedRegion
import org.eclipse.jface.text.IDocument
/**
 * Interface for tokeniser
 */
trait PlayPartitionTokeniser {
  def tokenise(document: IDocument): List[TypedRegion]
}
