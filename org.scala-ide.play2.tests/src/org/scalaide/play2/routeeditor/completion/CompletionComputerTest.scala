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

/** Common superclass for all completion computer tests in route files. */
trait CompletionComputerTest {

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
      val contentAssist = createCompletionComputer
      val viewer = mock(classOf[ITextViewer])
      when(viewer.getDocument()).thenReturn(document)
      contentAssist.computeCompletionProposals(viewer, caretOffset)
    }

    def expectedCompletions[T <: ExpectedProposal](oracle: T*)(implicit converter: AsExpectedProposal[T]): Unit = {
      val completions = computeCompletionProposals.map(converter)
      Assert.assertEquals("Expected completions don't match.", oracle.toList, completions.toList)
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