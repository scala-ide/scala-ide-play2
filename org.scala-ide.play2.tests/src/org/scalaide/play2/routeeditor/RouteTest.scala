package org.scalaide.play2.routeeditor

import org.scalaide.play2.routeeditor.lexical.RouteDocumentPartitioner
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.IDocument
import org.junit.Assert
import org.eclipse.jface.text.Document

trait RouteTest {

  protected class RouteFile(rawText: String, marker: Char = '@') {
    private val text = rawText.stripMargin.trim

    private val cleanedText: String = text.filterNot(_ == marker).mkString

    val document: IDocument = {
      val doc = new Document(cleanedText)
      val partitioner = new RouteDocumentPartitioner
      partitioner.connect(doc)
      doc.setDocumentPartitioner(partitioner)
      doc
    }

    val caretOffset: Int = {
      val offset = text.indexOf(marker)
      if (offset == -1) Assert.fail(s"Could not locate caret position marker '$marker' in test.")
      offset
    }
  }
  protected object RouteFile {
    def apply(text: String): RouteFile = new RouteFile(text)
  }
}