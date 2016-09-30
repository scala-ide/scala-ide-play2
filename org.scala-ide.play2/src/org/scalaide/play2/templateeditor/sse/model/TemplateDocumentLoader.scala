package org.scalaide.play2.templateeditor.sse.model

import org.eclipse.wst.html.core.internal.encoding.HTMLDocumentLoader
import org.eclipse.wst.sse.core.internal.ltk.parser.RegionParser
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.text.BasicStructuredDocument
import org.eclipse.wst.xml.core.internal.parser.XMLStructuredDocumentReParser
import org.scalaide.play2.templateeditor.processing.TemplateVersionExhibitor
import org.scalaide.play2.templateeditor.sse.lexical.TemplateRegionParser
import org.scalaide.play2.templateeditor.sse.lexical.TemplateStructuredTextPartitioner

class TemplateDocumentLoader extends HTMLDocumentLoader {

  override def newEncodedDocument() = {
    val structuredDocument = TemplateStructuredDocument(getParser(), TemplateVersionExhibitor.get)

    // A reparser that always reparses the whole document
    val reparser = new XMLStructuredDocumentReParser {
      override protected def findDirtyEnd(end: Int): IStructuredDocumentRegion = {
        val result = fStructuredDocument.getLastStructuredDocumentRegion()
        if (result != null) fStructuredDocument.setCachedDocumentRegion(result)
        dirtyEnd = result
        dirtyEnd
      }

      override protected def findDirtyStart(start: Int): Unit = {
        val result = fStructuredDocument.getFirstStructuredDocumentRegion()
        if (result != null) fStructuredDocument.setCachedDocumentRegion(result)
        dirtyStart = result
      }
    }

    structuredDocument.delegate.setReParser(reparser)

    // TODO - set the default embedded type content type handler.. whatever that means.
    // I don't believe we'll ever need to do that, but if it comes up in the future, this is the place to do it.
    structuredDocument
  }

  override def newInstance() = new TemplateDocumentLoader

  override def getParser(): RegionParser = new TemplateRegionParser

  override val getDefaultDocumentPartitioner = new TemplateStructuredTextPartitioner
}
