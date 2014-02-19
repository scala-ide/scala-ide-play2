package org.scalaide.play2.properties

import org.scalaide.ui.syntax.ScalaSyntaxClass

import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.jface.viewers.Viewer
/**
 * Provides the tree structure for syntax coloring according to given
 * list of categories and associated syntax classes to each one.
 * Each category would be parent in the tree and associated syntax classes
 * are children.
 * 
 * TODO: This class is a copy of the sdt.core object SyntaxColoringContentAndLabelProvider, except
 * that it abstracts over `categories`. Needs to be moved over.
 */
abstract class SyntaxColouringTreeContentAndLabelProvider(categories: List[Category]) extends LabelProvider with ITreeContentProvider {

  def getElements(inputElement: AnyRef): Array[Object] = categories.toArray

  def getChildren(parentElement: AnyRef) = parentElement match {
    case Category(_, children) => children.toArray
    case _ => Array()
  }

  def getParent(element: AnyRef): Category = categories.find(_.children contains element).orNull

  def hasChildren(element: AnyRef) = getChildren(element).nonEmpty

  def inputChanged(viewer: Viewer, oldInput: AnyRef, newInput: AnyRef) {}

  override def getText(element: AnyRef) = element match {
    case Category(name, _) => name
    case ScalaSyntaxClass(displayName, _, _) => displayName
  }
}