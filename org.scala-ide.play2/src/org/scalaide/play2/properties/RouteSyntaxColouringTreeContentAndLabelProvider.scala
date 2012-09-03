package org.scalaide.play2.properties

import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass

import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.jface.viewers.Viewer
import org.scalaide.play2.routeeditor.RouteSyntaxClasses

object RouteSyntaxColouringTreeContentAndLabelProvider extends SyntaxColouringTreeContentAndLabelProvider(RouteSyntaxClasses.categories)