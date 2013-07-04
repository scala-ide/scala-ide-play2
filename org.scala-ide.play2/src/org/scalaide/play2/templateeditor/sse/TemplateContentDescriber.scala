package org.scalaide.play2.templateeditor.sse

import org.eclipse.core.runtime.content.IContentDescriber
import org.eclipse.core.runtime.content.IContentDescription

class TemplateContentDescriber extends IContentDescriber {
  override def describe(contents: java.io.InputStream, description: IContentDescription) = IContentDescriber.VALID
  override def getSupportedOptions() = Array()
}