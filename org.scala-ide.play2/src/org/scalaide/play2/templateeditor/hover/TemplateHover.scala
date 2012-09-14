package org.scalaide.play2.templateeditor.hover

import org.eclipse.jdt.core.ICodeAssist
import org.eclipse.jface.text.{ ITextViewer, IRegion, ITextHover }
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import scala.tools.nsc.symtab.Flags
import scala.tools.eclipse.util.EclipseUtils._
import scala.tools.eclipse.ScalaWordFinder
import org.eclipse.jface.text.Region
import scala.tools.eclipse.ScalaHover

class TemplateHover(tcu: TemplateCompilationUnit) extends ScalaHover(tcu) {

  override def getHoverInfo(viewer: ITextViewer, region: IRegion): String = {
    // maps the region to scala generated source
    val mappedRegion = tcu.mapTemplateToScalaRegion(region.asInstanceOf[Region])
    super.getHoverInfo(viewer, mappedRegion)
  }

}
