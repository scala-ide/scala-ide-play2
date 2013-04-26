package org.scalaide.play2.routeeditor.lexical

import org.junit.Test
import org.junit.Before
import org.junit.Assert
import scala.tools.eclipse.lexical.ScalaPartitionRegion
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.Document

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
    Assert.assertEquals(List(ScalaPartitionRegion(ROUTE_HTTP, 0, 0)), tokens)
  }

  @Test
  def text_with_valid_HTTP_method_returnes_HTTP_partition() {
    val text = "GET"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(ScalaPartitionRegion(ROUTE_HTTP, 0, 3)), tokens)
  }

  @Test
  def text_with_incomplete_HTTP_method_returnes_HTTP_partition() {
    val text = "GE"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(ScalaPartitionRegion(ROUTE_HTTP, 0, 2)), tokens)
  }

  @Test
  def text_with_invalid_incomplete_HTTP_method_returnes_HTTP_partition() {
    val text = "o"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(ScalaPartitionRegion(ROUTE_HTTP, 0, 1)), tokens)
  }

  @Test
  def text_with_starting_hash_returns_comment_partition() {
    val text = "#"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(ScalaPartitionRegion(ROUTE_COMMENT, 0, 1)), tokens)
  }

  @Test
  def empty_lines_tokenized_as_HTPP_partitions() {
    val text = s" \n"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(ScalaPartitionRegion(ROUTE_HTTP, 0, 1), ScalaPartitionRegion(ROUTE_HTTP, 2, 2)), tokens)
  }

  @Test
  def GET_followed_by_newline_are_both_partitioned_as_HTTP_partitions() {
    val text = s"GET \n"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(ScalaPartitionRegion(ROUTE_HTTP, 0, 3), ScalaPartitionRegion(ROUTE_HTTP, 5, 5)), tokens)
  }

  /** This test shows that when no HTTP method is found, the first word in the line is always partitioned as a ROUTE_HTTP.
    * We could implement a smarter behavior and be robust against URI, but we would still have an issue when the user 
    * writes a controller method call first.
    * This test is simply showing that partitioning a URI as a HTTP method is expected behavior when a valid HTTP
    * method is missing.
    */
  @Test
  def when_HTTP_method_is_missing_URI_is_partitioned_as_HTTP_method() {
    val text = " \\public"
    val tokens = tokenizer.tokenise(text)
    Assert.assertEquals(List(ScalaPartitionRegion(ROUTE_HTTP, 1, 8)), tokens)
  }
}