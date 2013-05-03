package org.scalaide.play2.routeeditor.completion

import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.junit.Test

class UriCompletionComputerTest extends CompletionComputerTest {

  override def createComletionComputer: IContentAssistProcessor = new UriCompletionComputer

  private def dynamicUrisFor(uri: String): Seq[String] = {
    UriCompletionComputer.RouteUri(uri).dynamicUris.sorted(UriCompletionComputer.RouteUri.AlphabeticOrder).map(_.toString)
  }

  @Test
  def simple_completion_with_prefix_matching() {
    val route = RouteFile {
      """
        |GET /public
        |GET /p@
      """
    }

    route expectedCompletions "/public"
  }

  @Test
  def completion_on_incorrect_uri_returns_proposals_with_matching_prefix() {
    val route = RouteFile {
      """
        |GET /public
        |GET p@
      """
    }

    route expectedCompletions "/public"
  }

  @Test
  def completions_do_not_contain_duplicates() {
    val route = RouteFile {
      """
        |GET /public
        |GET /public
        |GET /p@
      """
    }

    route expectedCompletions "/public"
  }

  @Test
  def completion_are_sorted_alphabetically() {
    val route = RouteFile {
      """
        |GET /ab
        |GET /aa
        |GET /a@
      """
    }

    route expectedCompletions Seq("/aa", "/ab")
  }

  @Test
  def show_dynamic_parts_after_trailing_slash_when_no_completion_match_is_found() {
    val route = RouteFile {
      "GET /public/@"
    }

    route expectedCompletions dynamicUrisFor("/public/")
  }

  @Test
  def show_dynamic_parts_when_no_prefix_match_after_trailing_slash_for_empty_uri() {
    val route = RouteFile {
      "GET /@"
    }

    route expectedCompletions dynamicUrisFor("/")
  }

  @Test
  def show_dynamic_parts_when_no_prefix_match_for_empty_uri_with_missing_leading_slash() {
    val route = RouteFile {
      "GET @"
    }

    route expectedCompletions ("/" +: dynamicUrisFor("/"))
  }

  @Test
  def completion_should_show_all_interesting_permutations() {
    val route = RouteFile {
      """
        |GET /foo/public
        |GET /foo/bar/buz
        |GET /f@
      """
    }

    route expectedCompletions Seq("/foo", "/foo/bar", "/foo/bar/buz", "/foo/public")
  }

  @Test
  def completion_should_suggest_all_permutations_that_matches_input() {
    val route = RouteFile {
      """
        |GET /foo/bar
        |GET /foo@
      """
    }

    route expectedCompletions Seq("/foo", "/foo/bar")
  }

  @Test
  def completion_should_suggest_all_permutations_and_dynamic_parts_when_input_has_trailing_slash() {
    val route = RouteFile {
      """
        |GET /foo/bar
        |GET /foo/@
      """
    }

    route expectedCompletions ("/foo" +: dynamicUrisFor("/foo") :+ "/foo/bar")
  }

  @Test
  def completion_should_suggest_all_permutation_that_matches_input_if_URI_syntax_is_invalid() {
    val route = RouteFile {
      """
        |GET /foo/bar
        |GET foo@
      """
    }

    route expectedCompletions Seq("/foo", "/foo/bar")
  }

  @Test
  def completion_should_suggest_only_prefix_matching_completions() {
    val route = RouteFile {
      """
        |GET /foo
        |GET /published
        |GET /pub@lic
        |GET /buz
      """
    }

    route expectedCompletions Seq("/public", "/published")
  }

  @Test
  def completion_should_suggest_nothing_if_nothing_matches() {
    val route = RouteFile {
      """
        |GET /foo
        |GET /buz/:id@
      """
    }

    route expectedCompletions Seq()
  }
}