package org.scalaide.play2.lexical

import scala.tools.eclipse.lexical.ScalaPartitionRegion
import org.eclipse.jface.text.IDocument
/**
 * Interface for tokeniser
 */
trait PlayPartitionTokeniser {
  def tokenise(document: IDocument): List[ScalaPartitionRegion]
}
