package org.scalaide.play2.templateeditor.hover

import org.eclipse.jdt.core.ICodeAssist
import org.eclipse.jface.text.{ ITextViewer, IRegion, ITextHover }
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import scala.tools.nsc.symtab.Flags
import org.eclipse.jface.text.Region
import org.scalaide.ui.internal.editor.ScalaHover

class TemplateHover(tcu: TemplateCompilationUnit) extends ScalaHover(tcu) {

  override def getHoverInfo(viewer: ITextViewer, region: IRegion): String = {
    // maps the region to scala generated source
    tcu.mapTemplateToScalaRegion(region) match {
      case Some(mappedRegion) => super.getHoverInfo(viewer, mappedRegion)
      case None => null
    }
  }

}
