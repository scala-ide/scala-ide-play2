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

trait CompletionComputerTest[I, S <: CompletionComputerTest.ExpectedProposal] {

  protected def createComletionComputer: IContentAssistProcessor
  
  protected val factory: CompletionComputerTest.ExpectedProposalFactory[I, S]

  protected val TestMarker: Char = '@' 
  
  protected class RouteFile(rawText: String) {
    private val text = rawText.stripMargin.trim

    private val cleanedText: String = text.filterNot(_ == TestMarker).mkString

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

    def expectedCompletions(oracle: I): Unit = expectedCompletions(Seq(oracle))

    def expectedCompletions(oracle: Seq[I]): Unit = {
      val mappedOracle = oracle.map(factory.apply)
      val completions = computeCompletionProposals
      val textualCompletions = completions.map(factory.apply)
      Assert.assertEquals("Expected completions don't match.", mappedOracle.toList, textualCompletions.toList)
    }

    private val caretOffset: Int = {
      val offset = text.indexOf(TestMarker)
      if (offset == -1) Assert.fail(s"Could not locate caret position marker '${TestMarker}' in test.")
      offset
    }
  }
  protected object RouteFile {
    def apply(text: String): RouteFile = new RouteFile(text.stripMargin)
  }

}

object CompletionComputerTest {
  trait ExpectedProposal
  trait ExpectedProposalFactory[I, T <: ExpectedProposal] {
    def apply(proposal: ICompletionProposal): T
    def apply(input: I): ExpectedProposal
  }
  
  class DisplayStringProposal(private val displayString: String) extends ExpectedProposal {
    override def equals(that: Any): Boolean = that match {
      case completion: DisplayStringProposal =>
        completion.displayString == displayString
      case _ => false
    }
  }

  object DisplayStringProposal extends ExpectedProposalFactory[String, DisplayStringProposal] {
    override def apply(proposal: ICompletionProposal): DisplayStringProposal = {
      new DisplayStringProposal(proposal.getDisplayString)
    }
    
    override def apply(displayString: String): DisplayStringProposal = new DisplayStringProposal(displayString)
  }
}