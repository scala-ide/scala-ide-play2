package org.scalaide.play2.indenter

import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.DocumentCommand
import org.junit.Assert._
import org.scalaide.play2.templateeditor.TemplateAutoIndentStrategy
import org.junit.ComparisonFailure
import org.junit.Test
import org.eclipse.jface.text.TextUtilities

object TemplateAutoIndentTest {
  class TestCommand(cOffset: Int, cLength: Int, cText: String, cCaretOffset: Int, cShiftsCaret: Boolean, cDoIt: Boolean) extends DocumentCommand {
    caretOffset = cCaretOffset
    doit = cDoIt
    length = cLength
    offset = cOffset
    text = cText
    shiftsCaret = cShiftsCaret
  }

}

class TemplateAutoIndentTest {
  import TemplateAutoIndentTest._

  /** Tests if the input string is equal to the expected output, and the
   *  output caret position is correct.
   *
   *  The '^' character denotes the caret position, both for input and
   *  expected output.
   */
  def test(input: String, expectedOutput: String) {
    require(input.count(_ == '^') == 1, "the cursor in the input isn't set correctly")
    require(expectedOutput.count(_ == '^') == 1, "the cursor in the expected output isn't set correctly")

    def createDocument(input: String): IDocument = {
      val rawInput = input.filterNot(_ == '^')
      new Document(rawInput)
    }

    val doc = createDocument(input)
    
    def createTestCommand(input: String): TestCommand = {
      val pos = input.indexOf('^')
      new TestCommand(pos, 0, TextUtilities.getDefaultLineDelimiter(doc), -1, false, true)
    }

    val cmd = createTestCommand(input)
    val strategy = new TemplateAutoIndentStrategy(2, useSpacesForTabs = true)

    strategy.customizeDocumentCommand(doc, cmd)

    val m = cmd.getClass.getSuperclass.getDeclaredMethod("execute", classOf[IDocument])
    m.setAccessible(true)
    m.invoke(cmd, doc)

    val expected = expectedOutput.replaceAll("\\^", "")
    val actual = doc.get()

    if (expected != actual) {
      throw new ComparisonFailure("", expected, actual)
    }

    assertEquals("Caret position is wrong", cmd.caretOffset, expectedOutput.indexOf('^'))
  }

  @Test
  def indentPrevLine() {
    test(
      """
  foo^""",
      """
  foo
  ^""")
  }

  @Test
  def indentAfterBrace() {
    test(
      """
  foo {^""",
      """
  foo {
    ^""")
  }

  @Test
  def indentAfterParen() {
    test(
      """
  foo (^""",
      """
  foo (
    ^""")
  }

  @Test
  def indentAfterBraceWithTail() {
    test(
      """
  foo { x => ^""",
      """
  foo { x => 
    ^""")
  }

  @Test
  def indentBetweenBraces() {
    test(
      """
  foo {^}""",
      """
  foo {
    ^
  }""")
  }

  @Test
  def indentBetweenBracesWithTail() {
    test(
      """
  foo { x => ^}""",
      """
  foo { x => 
    ^
  }""")
  }

  @Test
  def indentBalancedParens() {
    test(
      """
  foo { x => x + 1 }^
""",
      """
  foo { x => x + 1 }
  ^
""")
  }
}