package org.scalaide.play2.lexical

import scala.tools.eclipse.lexical.ScalaPartitionRegion

trait PlayPartitionTokeniser {
  def tokenise(text: String): List[ScalaPartitionRegion]
}
