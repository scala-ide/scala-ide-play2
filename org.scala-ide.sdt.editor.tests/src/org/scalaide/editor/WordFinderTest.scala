package org.scalaide.editor

import org.junit.Test
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.Document
import org.junit.Assert

class WordFinderTest {

  private final val CaretMarker = '|'

  private val finder = new WordFinder

  private def document(text: String) = new {
    def shouldFind(expectedWord: String): Unit = {
      val caret: Int = {
        val offset = text.indexOf(CaretMarker)
        if (offset == -1) Assert.fail("Could not locate caret position marker '*' in test.")
        offset
      }
      val cleanedText = text.filterNot(_ == CaretMarker).mkString
      val doc = new Document(cleanedText)
      val region = finder.findWord(doc, caret)
      val actualWord = doc.get(region.getOffset(), region.getLength())
      Assert.assertEquals(expectedWord, actualWord)
    }
  }

  @Test
  def findWord_whenCaretIsInTheMiddle() {
    document {
      "necess|ary"
    } shouldFind ("necessary")
  }

  @Test
  def findWord_whenCaretIsAtTheEnd() {
    document {
      "necessary|"
    } shouldFind ("necessary")
  }

  @Test
  def findWord_whenCaretIsAtTheBeginning() {
    document {
      "|necessary"
    } shouldFind ("necessary")
  }

  @Test
  def noWord_whenCaretIsSurroundedByWhitespaces() {
    document {
      "it is | necessary"
    } shouldFind ("")
  }

  @Test
  def noWord_whenCaretIsAtTheEndAfterWhitespace() {
    document {
      "necessary |"
    } shouldFind ("")
  }
}