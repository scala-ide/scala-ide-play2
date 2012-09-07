package org.scalaide.play2.routeeditor.tools

import scala.collection.mutable

import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.search.SearchMatch
import org.eclipse.jdt.core.search.SearchRequestor

class MethodSearchRequestor extends SearchRequestor {
  val matchedElements = new mutable.ListBuffer[IJavaElement]
  override def acceptSearchMatch(searchMatch: SearchMatch) = {
    val element = searchMatch.getElement
    matchedElements += element.asInstanceOf[IJavaElement]
  }
}