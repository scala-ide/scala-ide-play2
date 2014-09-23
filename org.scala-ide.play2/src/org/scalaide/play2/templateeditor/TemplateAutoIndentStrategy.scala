package org.scalaide.play2.templateeditor

import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy
import org.scalaide.logging.HasLogger
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.DocumentCommand
import org.eclipse.jface.text.TextUtilities
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Platform

/** Simple auto-indent strategy.
 *
 *  - By default, indent to the previous line.
 *  - If the line contains unbalanced (open) parenthesis to the left of the caret, indent further by 1 tab width
 *  - If the rest of the line (to the right of caret) starts with a closed parenthesis, add a new line and indent
 *    the closing parenthesis to the previous line (without moving the caret)
 *
 *  @note This class does not distinguish between `(' and `{', so a line like `(blah}` is considered balanced
 */
class TemplateAutoIndentStrategy(tabSize: Int, useSpacesForTabs: Boolean) extends DefaultIndentLineAutoEditStrategy with HasLogger {
  private final val INDENT = if (useSpacesForTabs) " " * tabSize else "\t"
  private final val openParens = Set('(', '{')
  private final val closedParens = Set(')', '}')

  override def customizeDocumentCommand(doc: IDocument, cmd: DocumentCommand) {
    if (cmd.offset == -1 || doc.getLength() == 0) return // don't spend time on invalid docs

    try {
      if (cmd.length == 0 && cmd.text != null && TextUtilities.endsWith(doc.getLegalLineDelimiters(), cmd.text) != -1) {
        autoIndent(doc, cmd)
      }
    } catch {
      case e: Exception =>
        // don't break typing under any circumstances
        eclipseLog.warn("Error in scaladoc autoedit", e)
    }
  }

  private def autoIndent(doc: IDocument, cmd: DocumentCommand) {
    val LineInfo(indent, prefix, tail) = breakLine(doc, cmd.offset)
    val newLine = TextUtilities.getDefaultLineDelimiter(doc)

    val buf = new StringBuilder(cmd.text)

    buf.append(indent)
    if (unbalancedOpenParens(prefix)) {
      buf.append(INDENT)
    }

    val caretOffset = cmd.offset + buf.length
    if ((tail.length() > 0) && closedParens(tail.charAt(0)))
      buf.append(newLine).append(indent)

    cmd.text = buf.toString
    cmd.caretOffset = caretOffset
    cmd.shiftsCaret = false
  }

  /** Are there unbalanced parenthesis/braces in this string? */
  private def unbalancedOpenParens(line: String): Boolean = {
    var open = 0
    for (ch <- line)
      if (openParens(ch))
        open += 1
      else if (closedParens(ch))
        open -= 1

    open > 0
  }

  /** Return information about the line at given offset */
  private def breakLine(doc: IDocument, offset: Int): LineInfo = {
    val lineInfo = doc.getLineInformationOfOffset(offset)
    val endOfWS = findEndOfWhiteSpace(doc, lineInfo.getOffset(), offset)
    val indent = doc.get(lineInfo.getOffset, endOfWS - lineInfo.getOffset)
    val prefix = doc.get(endOfWS, offset - endOfWS)
    val restAfterCaret = doc.get(offset, lineInfo.getOffset() - offset + lineInfo.getLength())
    LineInfo(indent, prefix, restAfterCaret)
  }
}

/** Line information
 *
 *  @param indent whitespace-only prefix
 *  @param prefix characters between `indent` and the caret
 *  @param tail   characters between the caret and the end of the line
 */
private case class LineInfo(indent: String, prefix: String, tail: String)
