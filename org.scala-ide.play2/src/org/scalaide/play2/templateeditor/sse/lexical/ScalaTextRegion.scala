package org.scalaide.play2.templateeditor.sse.lexical

import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass
import org.eclipse.wst.sse.core.internal.parser.ContextRegion

class ScalaTextRegion(val syntaxClass: ScalaSyntaxClass, newStart: Int, newTextLength: Int, newLength: Int)
  extends ContextRegion(syntaxClass.displayName, newStart, newTextLength, newLength)