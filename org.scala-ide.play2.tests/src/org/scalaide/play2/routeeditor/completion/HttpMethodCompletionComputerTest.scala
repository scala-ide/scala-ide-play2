package org.scalaide.play2.routeeditor.completion

import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.junit.Test
import org.scalaide.play2.routeeditor.lexical.HTTPKeywords

class HttpMethodCompletionComputerTest extends CompletionComputerTest[String, CompletionComputerTest.DisplayStringProposal] {

  override def createComletionComputer: IContentAssistProcessor = new HttpMethodCompletionComputer

  implicit val factory = CompletionComputerTest.DisplayStringProposal

  @Test
  def HttpGET_completion() {
    val route = RouteFile { "G@" }

    route expectedCompletions "GET"
  }

  @Test
  def HttpGET_completion_is_case_insensitive() {
    val route = RouteFile { "g@" }

    route expectedCompletions "GET"
  }

  @Test
  def HTTP_PUT_POST_completion() {
    val route = RouteFile { "P@" }

    route expectedCompletions Seq("POST", "PUT")
  }

  @Test
  def HTTP_HEAD_completion() {
    val route = RouteFile { "HeA@" }

    route expectedCompletions "HEAD"
  }

  @Test
  def HTTP_DELETE_completion() {
    val route = RouteFile { "de@" }

    route expectedCompletions "DELETE"
  }

  @Test
  def show_all_HTTP_methods_when_word_doesnt_match() {
    val route = RouteFile { "DET@" }

    route expectedCompletions HTTPKeywords.Methods
  }

  @Test
  def all_Http_Method_completion_at_beginning_of_empty_line() {
    val route = RouteFile { "@" }

    route expectedCompletions HTTPKeywords.Methods
  }

  @Test
  def all_Http_Method_completion_at_end_of_empty_line() {
    val route = RouteFile {
      // whitespaces before the '*' are relevant for this test!
      "   @"
    }

    route expectedCompletions HTTPKeywords.Methods
  }

  @Test
  def all_Http_Method_completion_in_middle_of_empty_line() {
    val route = RouteFile {
      // whitespaces after the '*' are relevant for this test!
      "  @   "
    }

    route expectedCompletions HTTPKeywords.Methods
  }

  @Test
  def all_Http_Method_completion_when_cursor_is_on_already_valid_Http_method() {
    val route = RouteFile { "G@ET" }

    route expectedCompletions HTTPKeywords.Methods
  }

  @Test
  def GET_Http_Method_completion_returned() {
    val route = RouteFile {
      // whitespaces after the '*' is relevant for this test!
      "G@  "
    }

    route expectedCompletions "GET"
  }
}