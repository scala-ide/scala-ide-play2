package org.scalaide.play2.templateeditor

import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider

object TemplateAnnotationModelFactory extends CompilationUnitDocumentProvider {
  override def createAnnotationModel(path: IPath) = {
    val result = super.createAnnotationModel(path)
    result
  }
}