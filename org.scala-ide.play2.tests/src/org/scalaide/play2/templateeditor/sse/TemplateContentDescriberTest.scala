package org.scalaide.play2.templateeditor.sse

import org.junit.Test
import org.junit.Assert._
import org.eclipse.core.runtime.content.IContentDescriber
import java.io.ByteArrayInputStream

class TemplateContentDescriberTest {
  
  import IContentDescriber._
  
  @Test
  def emptyFile() {
    test("Empty file", "", INDETERMINATE)
  }
  
  @Test
  def notTemplate() {
    test("Not template", "<xml></xml>", INVALID)
  }
  
  @Test
  def notTemplateWithEmptyLines() {
    test("Not template with emtyp lines", """
    
   
<xml>
""", INVALID)
  }
  
  @Test
  def notTemplateWithLeadingSpaces() {
    test("Not template with leading spaces", "   <xml></xml>", INVALID)
  }
  
  @Test
  def startWithComment() {
    test("Start with comment", "@********@", VALID)
  }
  
  @Test
  def startWithParameters() {
    test("Start with parameters", "@(abc: Int)(test: String)", VALID)
  }
  
  @Test
  def startWithCommentWithEmptyLines() {
    test("Start with comment with emtyp lines", """
        
    
@********@
""", VALID)
  }
  
  @Test
  def startWithParametersWithEmptyLines() {
    test("Start with parameters with emtyp lines", """
   
        
@(abc: Int)(test: String)
""", VALID)
  }
  
  @Test
  def startWithCommentWithLeadingSpaces() {
    test("Start with comment with leading spaces", "   @********@", VALID)
  }
  
  @Test
  def startWithParametersWithLeadingSpaces() {
    test("Start with parameters with leading spaces", "  @(abc: Int)(test: String)", VALID)
  }
  
  private def test(description: String, content: String, expectedValue: Int) {
    val describer = new TemplateContentDescriber()
    
    assertEquals("Wrong value for " + description, expectedValue, describer.describe(new ByteArrayInputStream(content.getBytes("UTF-8")), null))
  }

}