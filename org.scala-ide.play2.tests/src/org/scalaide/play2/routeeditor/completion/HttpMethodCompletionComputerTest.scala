package org.scalaide.play2.routeeditor.completion

import org.junit.Test
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.mockito.Mockito.mock
import org.mockito.Mockito.when
import org.eclipse.ui.texteditor.ITextEditor
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.Document
import org.junit.Assert
import org.scalaide.play2.routeeditor.lexical.HTTPKeywords
import org.eclipse.jface.text.contentassist.ICompletionProposal

class HttpMethodCompletionComputerTest {

  private class RouteFile(text: String) {
    private def cleanedText: String = text.filterNot(_ == '*').mkString

    private val document = new Document(cleanedText)

    private lazy val computeCompletionProposals: Array[ICompletionProposal] = {
      val contentAssist = new HttpMethodCompletionComputer
      val viewer = mock(classOf[ITextViewer])
      when(viewer.getDocument()).thenReturn(document)
      contentAssist.computeCompletionProposals(viewer, caretOffset)
    }

    def expectedMethodCompletions(method: String*): Unit = {
      val completions = computeCompletionProposals
      val textualCompletions = completions.map(_.getDisplayString())
      Assert.assertEquals("Expected HTTP method completions don't match.", method.toList, textualCompletions.toList)

      if(textualCompletions.size == 1) checkCompletionIsCorrectlyAppliedToDocument(completions(0))
    }

    private def checkCompletionIsCorrectlyAppliedToDocument(proposal: ICompletionProposal): Unit = {
      Assert.assertFalse(document.get().contains(proposal.getDisplayString()))
      proposal.apply(document)
      Assert.assertTrue(document.get().contains(proposal.getDisplayString()))
    }

    private def caretOffset: Int = {
      val offset = text.indexOf('*')
      if (offset == -1) Assert.fail("Could not locate caret position marker '*' in test.")
      offset
    }
  }
  private object RouteFile {
    def apply(text: String): RouteFile = new RouteFile(text.stripMargin)
  }

  @Test
  def HttpGET_completion() {
    val route = RouteFile {
      """
      |G*
      """
    }

    route expectedMethodCompletions "GET"
  }

  @Test
  def HttpGET_completion_is_case_insensitive() {
    val route = RouteFile {
      """
      |g*
      """
    }

    route expectedMethodCompletions "GET"
  }

  @Test
  def HTTP_PUT_POST_completion() {
    val route = RouteFile {
      """
      |P*
      """
    }

    route expectedMethodCompletions ("POST", "PUT")
  }

  @Test
  def HTTP_HEAD_completion() {
    val route = RouteFile {
      """
      |HeA*
      """
    }

    route expectedMethodCompletions "HEAD"
  }

  @Test
  def HTTP_DELETE_completion() {
    val route = RouteFile {
      """
      |de*
      """
    }

    route expectedMethodCompletions "DELETE"
  }

  @Test
  def show_all_HTTP_methods_word_doesnt_match() {
    val route = RouteFile {
      """
      |DET*
      """
    }

    route expectedMethodCompletions (HTTPKeywords.Methods: _*)
  }
}