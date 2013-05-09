package org.scalaide.play2.routeeditor.completion

import org.eclipse.jface.text.contentassist.ICompletionProposal

import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.junit.Test

class UriCompletionComputerTest extends CompletionComputerTest {

  case class Proposal(uri: String) extends ExpectedProposal

  implicit object Converter extends AsExpectedProposal[Proposal] {
    def apply(proposal: ICompletionProposal) = Proposal(proposal.getDisplayString)
  }

  override def createCompletionComputer: IContentAssistProcessor = new UriCompletionComputer

  private def dynamicUrisFor(uri: String): Seq[Proposal] = {
    val dynamicUris = UriCompletionComputer.RouteUri(uri).dynamicUris
    val sorted = dynamicUris.sorted(UriCompletionComputer.RouteUri.AlphabeticOrder)
    sorted.map(uri => Proposal(uri.toString))
  }

  @Test
  def simple_completion_with_prefix_matching() {
    val route = RouteFile {
      """
        |GET /public
        |GET /p@
      """
    }

    route expectedCompletions Proposal("/public")
  }

  @Test
  def completion_on_incorrect_uri_returns_proposals_with_matching_prefix() {
    val route = RouteFile {
      """
        |GET /public
        |GET p@
      """
    }

    route expectedCompletions Proposal("/public")
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

    route expectedCompletions Proposal("/public")
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

    route expectedCompletions (Proposal("/aa"), Proposal("/ab"))
  }

  @Test
  def show_dynamic_parts_after_trailing_slash_when_no_completion_match_is_found() {
    val route = RouteFile {
      "GET /public/@"
    }

    route expectedCompletions (dynamicUrisFor("/public/"): _*)
  }

  @Test
  def show_dynamic_parts_when_no_prefix_match_after_trailing_slash_for_empty_uri() {
    val route = RouteFile {
      "GET /@"
    }

    route expectedCompletions (dynamicUrisFor("/"): _*)
  }

  @Test
  def show_dynamic_parts_when_no_prefix_match_for_empty_uri_with_missing_leading_slash() {
    val route = RouteFile {
      "GET @"
    }

    route expectedCompletions ((Proposal("/") +: dynamicUrisFor("/")): _*)
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

    route expectedCompletions (Proposal("/foo"), Proposal("/foo/bar"),
      Proposal("/foo/bar/buz"), Proposal("/foo/public"))
  }

  @Test
  def completion_should_suggest_all_permutations_that_matches_input() {
    val route = RouteFile {
      """
        |GET /foo/bar
        |GET /foo@
      """
    }

    route expectedCompletions (Proposal("/foo"), Proposal("/foo/bar"))
  }

  @Test
  def completion_should_suggest_all_permutations_and_dynamic_parts_when_input_has_trailing_slash() {
    val route = RouteFile {
      """
        |GET /foo/bar
        |GET /foo/@
      """
    }

    route expectedCompletions ((Proposal("/foo") +: dynamicUrisFor("/foo") :+ Proposal("/foo/bar")): _*)
  }

  @Test
  def completion_should_suggest_all_permutation_that_matches_input_if_URI_syntax_is_invalid() {
    val route = RouteFile {
      """
        |GET /foo/bar
        |GET foo@
      """
    }

    route expectedCompletions (Proposal("/foo"), Proposal("/foo/bar"))
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

    route expectedCompletions (Proposal("/public"), Proposal("/published"))
  }

  @Test
  def completion_should_suggest_nothing_if_nothing_matches() {
    val route = RouteFile {
      """
        |GET /foo
        |GET /buz/:id@
      """
    }

    route expectedCompletions ()
  }
}