package org.scalaide.play2.routeeditor.completion

import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.junit.Assert
import org.mockito.Mockito.mock
import org.mockito.Mockito.when
import org.scalaide.play2.routeeditor.lexical.RouteDocumentPartitioner
import org.eclipse.jface.text.IDocument
import org.scalaide.play2.routeeditor.RouteTest

trait CompletionComputerTest extends RouteTest {
  
  /** The expected completion proposal.*/
  trait ExpectedProposal

  /** Allows to convert the `ICompletionProposal` returned by the completion computer under test into 
   *  an `ExpectedProposal`, so that the returned completion can be compared against the test expectation.
   *  
   * Subclasses will usually implement this as an '''implicit object''' so that it gets automatically 
   * passed to `RouteFile.expectedCompletions`.
   */
  trait AsExpectedProposal[T <: ExpectedProposal] extends (ICompletionProposal => ExpectedProposal)
  
  protected def createCompletionComputer: IContentAssistProcessor
  
  protected val TestMarker: Char = '@'
    
  protected class RouteCompletionFile(rawText: String) extends RouteFile(rawText, List(TestMarker)) {
    private lazy val computeCompletionProposals: Array[ICompletionProposal] = {
      val contentAssist = createCompletionComputer
      val viewer = mock(classOf[ITextViewer])
      when(viewer.getDocument()).thenReturn(document)
      contentAssist.computeCompletionProposals(viewer, caretOffset)
    }
    
    def caretOffset: Int = caretOffset(TestMarker)

    def expectedCompletions[T <: ExpectedProposal](oracle: T*)(implicit converter: AsExpectedProposal[T]): Unit = {
      val completions = computeCompletionProposals.map(converter)
      Assert.assertEquals("Expected completions don't match.", oracle.toList, completions.toList)
    }

  }
  protected object RouteCompletionFile {
    def apply(text: String): RouteCompletionFile = new RouteCompletionFile(text.stripMargin)
  }
}