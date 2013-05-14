package org.scalaide.play2.routeeditor.lexical

import org.junit.Test
import org.junit.Before
import org.junit.Assert
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.TypedRegion

class RoutePartitionTokeniserTest {

  import RoutePartitions._

  private var tokenizer: RoutePartitionTokeniser = _

  @Before
  def setUp() {
    tokenizer = new RoutePartitionTokeniser
  }

  implicit def string2document(text: String): IDocument = new Document(text)

  @Test
  def empty_route_file_should_return_HTTP_partition() {
    val text = ""
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 0, ROUTE_HTTP)), tokens)
  }

  @Test
  def text_with_valid_HTTP_method_returnes_HTTP_partition() {
    val text = "GET"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 3, ROUTE_HTTP)), tokens)
  }

  @Test
  def text_with_incomplete_HTTP_method_returnes_HTTP_partition() {
    val text = "GE"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 2, ROUTE_HTTP)), tokens)
  }

  @Test
  def text_with_invalid_incomplete_HTTP_method_returnes_HTTP_partition() {
    val text = "o"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 1, ROUTE_HTTP)), tokens)
  }

  @Test
  def text_with_starting_hash_returns_comment_partition() {
    val text = "#"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 1, ROUTE_COMMENT)), tokens)
  }

  @Test
  def empty_lines_tokenized_as_HTPP_partitions() {
    val text = s" \n"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 1, ROUTE_HTTP), new TypedRegion(1, 1, ROUTE_DEFAULT), new TypedRegion(2, 0, ROUTE_HTTP)), tokens)
  }

  @Test
  def GET_followed_by_newline_are_both_partitioned_as_HTTP_partitions() {
    val text = s"GET \n"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 3, ROUTE_HTTP), new TypedRegion(3, 1, ROUTE_DEFAULT), new TypedRegion(4, 0, ROUTE_URI), new TypedRegion(4, 1, ROUTE_DEFAULT), new TypedRegion(5, 0, ROUTE_HTTP)), tokens)
  }

  /** This test shows that when no HTTP method is found, the first word in the line is always partitioned as a ROUTE_HTTP.
   *  We could implement a smarter behavior and be robust against URI, but we would still have an issue when the user
   *  writes a controller method call first.
   *  This test is simply showing that partitioning a URI as a HTTP method is expected behavior when a valid HTTP
   *  method is missing.
   */
  @Test
  def when_HTTP_method_is_missing_URI_is_partitioned_as_HTTP_method() {
    val text = " /public"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 1, ROUTE_DEFAULT), new TypedRegion(1, 7, ROUTE_HTTP)), tokens)
  }

  @Test
  def URI_partition_is_always_expected_after_HTTP_partition() {
    val text = "GET "
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 3, ROUTE_HTTP), new TypedRegion(3, 1, ROUTE_DEFAULT), new TypedRegion(4, 0, ROUTE_URI)), tokens)
  }

  @Test
  def simple_URI_after_HTTP_is_correctly_partitioned() {
    val text = "GET /public"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 3, ROUTE_HTTP), new TypedRegion(3, 1, ROUTE_DEFAULT), new TypedRegion(4, 7, ROUTE_URI)), tokens)
  }

  @Test
  def URI_with_dynamic_part_after_HTTP_is_correctly_partitioned() {
    val text = "GET /clients/:id controllers.Clients.show(id: Long)"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 3, ROUTE_HTTP), new TypedRegion(3, 1, ROUTE_DEFAULT), new TypedRegion(4, 12, ROUTE_URI), new TypedRegion(16, 1, ROUTE_DEFAULT), new TypedRegion(17, 34, ROUTE_ACTION)), tokens)
  }

  @Test
  def action_after_HTTP_and_URI_is_expected() {
    val text = "GET /public  "
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 3, ROUTE_HTTP), new TypedRegion(3, 1, ROUTE_DEFAULT), new TypedRegion(4, 7, ROUTE_URI), new TypedRegion(11, 1, ROUTE_DEFAULT), new TypedRegion(12, 1, ROUTE_ACTION)), tokens)
  }

  @Test
  def action_after_HTTP_and_URI_is_correctly_partitioned() {
    val text = "GET /public controllers.Home.welcome()"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 3, ROUTE_HTTP), new TypedRegion(3, 1, ROUTE_DEFAULT), new TypedRegion(4, 7, ROUTE_URI), new TypedRegion(11, 1, ROUTE_DEFAULT), new TypedRegion(12, 26, ROUTE_ACTION)), tokens)
  }

  @Test
  def whitespace_after_action_are_not_included_in_partition() {
    val text = "GET /public controllers.Home.welcome()  " // the trailing whitespaces are relevant for this test! 
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 3, ROUTE_HTTP), new TypedRegion(3, 1, ROUTE_DEFAULT), new TypedRegion(4, 7, ROUTE_URI), new TypedRegion(11, 1, ROUTE_DEFAULT), new TypedRegion(12, 26, ROUTE_ACTION), new TypedRegion(38, 2, ROUTE_DEFAULT)), tokens)
  }
  
  @Test
  def whitespace_in_action_is_correctly_managed() {
    val text = "GET /public controllers.Home.welcome(name, value)" 
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 3, ROUTE_HTTP), new TypedRegion(3, 1, ROUTE_DEFAULT), new TypedRegion(4, 7, ROUTE_URI), new TypedRegion(11, 1, ROUTE_DEFAULT), new TypedRegion(12, 37, ROUTE_ACTION)), tokens)
  }
  
  @Test
  def goodComment() {
    val text = "# some blabla" 
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 13, ROUTE_COMMENT)), tokens)
  }
  
  @Test
  def badCommentLeadingSpace() {
    val text = " # some blabla"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(new TypedRegion(0, 1, ROUTE_DEFAULT), new TypedRegion(1, 1, ROUTE_HTTP), new TypedRegion(2, 1, ROUTE_DEFAULT), new TypedRegion(3,4, ROUTE_URI), new TypedRegion(7, 1, ROUTE_DEFAULT), new TypedRegion(8, 6, ROUTE_ACTION)), tokens)
  }
}