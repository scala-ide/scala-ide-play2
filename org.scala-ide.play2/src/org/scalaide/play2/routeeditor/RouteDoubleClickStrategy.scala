package org.scalaide.play2.routeeditor

import org.eclipse.jface.text._

class RouteDoubleClickStrategy extends ITextDoubleClickStrategy {
  protected var fText: ITextViewer = _
  def doubleClicked(part: ITextViewer): Unit = {
    val pos = part.getSelectedRange().x;

    if (pos < 0)
      return ;

    fText = part;

    if (!selectComment(pos)) {
      selectWord(pos); 
    }
  }

  protected def selectComment(caretPos: Int): Boolean = {
    val doc = fText.getDocument();
    var startPos = 0
    var endPos = 0

    try {
      var pos = caretPos;
      var c = ' ';

      def procede(factor: Int)(condition: Int => Boolean): Unit = {
        c = doc.getChar(pos)
        if (!(c == Character.LINE_SEPARATOR || c == '\"')) {
          pos += factor
          if (condition(pos)) {
            procede(factor)(condition)
          }
        }
      }

      procede(-1)(_ >= 0)

      if (c != '\"')
        return false;

      startPos = pos;

      pos = caretPos;
      val length = doc.getLength();
      c = ' ';

      procede(1)(_ < length)

      if (c != '\"')
        return false;

      endPos = pos;

      val offset = startPos + 1;
      val len = endPos - offset;
      fText.setSelectedRange(offset, len);
      return true;
    } catch {
      case _ =>
    }

    return false;
  }

  protected def selectWord(caretPos: Int): Boolean = {

    val doc = fText.getDocument();
    var startPos = 0
    var endPos = 0

    try {

      var pos = caretPos
      var c = ' '

      def procede(factor: Int)(condition: Int => Boolean): Unit = {
        c = doc.getChar(pos)
        if (Character.isJavaIdentifierPart(c)) {
          pos += factor
          if (condition(pos)) {
            procede(factor)(condition)
          }
        }
      }

      procede(-1)(_ >= 0)

      startPos = pos;

      pos = caretPos;
      val length = doc.getLength();

      procede(1)(_ < length)

      endPos = pos;
      selectRange(startPos, endPos);
      return true;

    } catch {
      case _ =>
    }

    return false;
  }

  private def selectRange(startPos: Int, stopPos: Int) = {
    val offset = startPos + 1;
    val length = stopPos - offset;
    fText.setSelectedRange(offset, length);
  }
}