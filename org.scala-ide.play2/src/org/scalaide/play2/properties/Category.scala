package org.scalaide.play2.properties

import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass
/**
 * Category of syntax classes
 */
case class Category(name: String, children: List[ScalaSyntaxClass])