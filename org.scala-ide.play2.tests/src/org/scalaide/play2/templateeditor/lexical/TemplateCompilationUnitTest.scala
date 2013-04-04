package org.scalaide.play2.templateeditor.lexical

import scala.tools.eclipse.testsetup.SDTTestUtils
import scala.tools.eclipse.testsetup.TestProjectSetup

import org.eclipse.jdt.internal.core.util.SimpleDocument
import org.junit.AfterClass
import org.junit.Assert._
import org.junit.Test
import org.scalaide.play2.templateeditor.TemplateCompilationUnit

object TemplateCompilationUnitTest extends TestProjectSetup("aProject", bundleName = "org.scala-ide.play2.tests") {
  @AfterClass
  def projectCleanUp() {
    SDTTestUtils.deleteProjects(project)
  }
}

class TemplateCompilationUnitTest {
  import TemplateCompilationUnitTest._

  /**
   * Test the following sequence:
   *   - content1 -> generated1
   *   - content2 -> generated2
   *   - content1 -> generated1 (used to return generated2, see ticket #30)
   */
  @Test
  def cachedContentIsCorrectlyUpdated() {
    val indexFile = file("app/views/index.scala.html")
    val templateCU = TemplateCompilationUnit(indexFile)

    val content1 = SDTTestUtils.slurpAndClose(indexFile.getContents())

    val document = new TestDocument(content1)
    templateCU.connect(document)

    val generated1 = templateCU.generatedSource.get.content

    val content2 = "@(messages: String)\n<html><body>@messages</body></html>\n"
    document.set(content2)

    val generated2 = templateCU.generatedSource.get.content

    assertFalse("generated1 and generated2 should be different", generated1 == generated2)

    document.set(content1)

    val generated1_2 = templateCU.generatedSource.get.content

    assertEquals("Same content should return the same generated code", generated1, generated1_2)

  }

  @Test
  def no_scala_source_is_generated_when_there_are_template_parse_errors() {
    val tFile = file("app/views/template_parse_error.scala.html")
    val tu = TemplateCompilationUnit(tFile)
    assertTrue(tu.generatedSource.isFailure)
  }
  
  @Test
  def error_on_position_zero_no_crash() {
    val tFile = file("app/views/template_unclosed_comment.scala.html")
    val tu = TemplateCompilationUnit(tFile)
    val errors = tu.reconcile(tu.getTemplateContents.toString)
    assertEquals("Unexpected errors", 1, errors.size)
    assertTrue("Negative offset", errors.head.getSourceStart() >= 0)
  }
  
  @Test
  def scala_source_is_generated_when_there_are_scala_compiler__errors() {
    val tFile = file("app/views/scala_compiler_error.scala.html")
    val tu = TemplateCompilationUnit(tFile)
    assertTrue(tu.generatedSource.isSuccess)
  }

  @Test
  def scala_nature_is_automatically_added_when_creating_template_unit() {
    assertTrue(project.hasScalaNature)

    // remove the Scala nature
    import scala.tools.eclipse.actions.ToggleScalaNatureAction
    val toggleScalaNature = new ToggleScalaNatureAction()
    toggleScalaNature.performAction(project.underlying)
    
    assertFalse("The test project should not have the Scala nature at this point.", project.hasScalaNature)

    val indexFile = file("app/views/index.scala.html")
    val templateCU = TemplateCompilationUnit(indexFile)

    assertTrue("Creating a `TemplateCompilationUnit` should force the underlying project to automatically add the Scala nature.", project.hasScalaNature)
  }
  
}

/**
 * Minimal Document supporting getting and setting content
 */
class TestDocument(var content: String) extends SimpleDocument("") {
  
  override def get(): String = content
  
  override def set(c: String) {
    content= c
  }
  
}