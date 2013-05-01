package org.scalaide.play2.routeeditor.completion

import org.eclipse.jface.text.Document
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.mockito.Mockito.mock
import org.mockito.Mockito.when
import org.junit.Assert
import org.eclipse.jface.text.ITextViewer
import org.scalaide.play2.routeeditor.lexical.RouteDocumentPartitioner
import org.eclipse.jface.text.IDocument

trait CompletionComputerTest {
  
  protected def createComletionComputer: IContentAssistProcessor
  
  protected class RouteFile(rawText: String) {
    private val text = rawText.stripMargin.trim

    private val cleanedText: String = text.filterNot(_ == '@').mkString

    private val document: IDocument = {
      val doc = new Document(cleanedText)
      val partitioner = new RouteDocumentPartitioner
      partitioner.connect(doc)
      doc.setDocumentPartitioner(partitioner)
      doc
    }
    

    private lazy val computeCompletionProposals: Array[ICompletionProposal] = {
      val contentAssist = createComletionComputer
      val viewer = mock(classOf[ITextViewer])
      when(viewer.getDocument()).thenReturn(document)
      contentAssist.computeCompletionProposals(viewer, caretOffset)
    }

    def expectedCompletions(oracle: String): Unit = expectedCompletions(Seq(oracle))

    def expectedCompletions(oracle: Seq[String]): Unit = {
      val completions = computeCompletionProposals
      val textualCompletions = completions.map(_.getDisplayString())
      Assert.assertEquals("Expected completions don't match.", oracle.toList, textualCompletions.toList)
    }

    private val caretOffset: Int = {
      val offset = text.indexOf('@')
      if (offset == -1) Assert.fail("Could not locate caret position marker '@' in test.")
      offset
    }
  }
  protected object RouteFile {
    def apply(text: String): RouteFile = new RouteFile(text.stripMargin)
  }
}