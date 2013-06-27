package org.scalaide.play2.templateeditor.sse.hover

import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextViewer
import org.eclipse.wst.sse.ui.internal.taginfo.AbstractHoverProcessor
import org.scalaide.editor.util.EditorHelper
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.scalaide.play2.templateeditor.hover.TemplateHover

class TemplateScalaTextHoverProcessor extends AbstractHoverProcessor {
  
  def getHoverInfo(textViewer: ITextViewer, hoverRegion: IRegion): String = {
    val result = 
      for (file <- EditorHelper.findFileOfDocument(textViewer.getDocument()))
      yield new TemplateHover(TemplateCompilationUnit.fromFileAndDocument(file, textViewer.getDocument())).getHoverInfo(textViewer, hoverRegion)
    result getOrElse null 
  }

  def getHoverRegion(textViewer: ITextViewer, offset: Int): IRegion = {
    val result =
      for (file <- EditorHelper.findFileOfDocument(textViewer.getDocument()))
        yield new TemplateHover(TemplateCompilationUnit.fromFileAndDocument(file, textViewer.getDocument())).getHoverRegion(textViewer, offset)
    result getOrElse null
  }
}