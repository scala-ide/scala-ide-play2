package org.scalaide.play2.templateeditor

import scala.tools.eclipse.InteractiveCompilationUnit

import org.eclipse.core.runtime.IAdapterFactory
import org.eclipse.ui.part.FileEditorInput

/** Adapt `FileEditorInput` to `InteractiveCompilationUnit`, if possible. It allows
 *  the hyperlinking engine on the sdt.core project to work with script files.
 */
class AdapterFactory extends IAdapterFactory {

  def getAdapterList = Array(classOf[InteractiveCompilationUnit])

  def getAdapter(adaptable: Any, target: java.lang.Class[_]): Object = adaptable match {
    case fileEditorInput: FileEditorInput =>
      TemplateCompilationUnit.fromEditorInput(fileEditorInput).getOrElse(null)
    case _                                => null
  }
}