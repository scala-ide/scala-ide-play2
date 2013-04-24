package org.scalaide.play2.routeeditor.hyperlink

import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlink
import org.eclipse.jdt.ui.actions.OpenAction
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector
import org.scalaide.play2.routeeditor.RouteAction
import org.scalaide.play2.routeeditor.RouteEditor

class RouteHyperlinkDetector(routeEditor: RouteEditor) extends IHyperlinkDetector {
  override def detectHyperlinks(textViewer: ITextViewer, region: IRegion, canShowMultipleHyperlinks: Boolean): Array[IHyperlink] = {
    routeEditor.getScalaProject.flatMap {
      scalaProject =>
        RouteHyperlinkComputer.detectHyperlinks(scalaProject, textViewer.getDocument(), region, createJavaHyperlink)
    }.map(Array(_)).getOrElse(null)
  }

  protected def createJavaHyperlink(routeAction: RouteAction, method: IJavaElement): IHyperlink = {
    val openAction = new OpenAction(
      routeEditor.getEditorSite())
    new JavaElementHyperlink(routeAction.region,
      openAction, method, false)
  }

}