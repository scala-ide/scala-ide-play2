package org.scalaide.play2.templateeditor.sse.hover

import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextViewer
import org.eclipse.wst.sse.ui.internal.taginfo.AbstractHoverProcessor
import org.scalaide.play2.templateeditor.TemplateCompilationUnitProvider
import org.scalaide.play2.templateeditor.hover.TemplateHover
import org.scalaide.play2.util.StoredEditorUtils
import org.eclipse.jface.text.ITextHoverExtension2
import org.scalaide.ui.internal.editor.hover.ScalaHover

class TemplateScalaTextHoverProcessor extends ScalaHover {


  override def getHoverInfo2(textViewer: ITextViewer, hoverRegion: IRegion): Object = {
    val result =
      for (file <- StoredEditorUtils.getFileOfViewer(textViewer))
      yield new TemplateHover(new TemplateCompilationUnitProvider(false).fromFileAndDocument(file, textViewer.getDocument())).getHoverInfo2(textViewer, hoverRegion)

    result getOrElse null
  }
}