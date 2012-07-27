package org.scalaide.play2.properties

import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass

import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.jface.viewers.Viewer
import org.scalaide.play2.routeeditor.RouteSyntaxClasses
import org.scalaide.play2.routeeditor.RouteSyntaxClasses.Category

object RouteSyntaxColouringTreeContentAndLabelProvider extends LabelProvider with ITreeContentProvider {

  def getElements(inputElement: AnyRef) = RouteSyntaxClasses.categories.toArray

  def getChildren(parentElement: AnyRef) = parentElement match {
    case Category(_, children) => children.toArray
    case _ => Array()
  }

  def getParent(element: AnyRef): Category = RouteSyntaxClasses.categories.find(_.children contains element).orNull

  def hasChildren(element: AnyRef) = getChildren(element).nonEmpty

  def inputChanged(viewer: Viewer, oldInput: AnyRef, newInput: AnyRef) {}

  override def getText(element: AnyRef) = element match {
    case Category(name, _) => name
    case ScalaSyntaxClass(displayName, _, _) => displayName
  }
}