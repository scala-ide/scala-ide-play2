package org.scalaide.play2.routeeditor.hyperlink

import scala.Array.canBuildFrom
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlink
import org.eclipse.jdt.ui.actions.OpenAction
import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.Region
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector
import org.scalaide.play2.routeeditor.lexical.RoutePartitions
import org.scalaide.play2.routeeditor.tools.MethodFinder
import org.scalaide.play2.routeeditor.lexical.RoutePartitions
import org.scalaide.play2.routeeditor.RouteEditor
import scala.Array.apply

class RouteHyperlinkDetector(routeEditor: RouteEditor) extends IHyperlinkDetector {
  override def detectHyperlinks(textViewer: ITextViewer,
    region: IRegion, canShowMultipleHyperlinks: Boolean): Array[IHyperlink] = {
    try {
      if (RoutePartitions.isRouteAction(textViewer.getDocument()
        .getContentType(region.getOffset()))) {
        val wordRegion = findWord(textViewer.getDocument().get(),
          region.getOffset())
        if (!isInMethodNamePart(textViewer.getDocument().get(),
          wordRegion))
          return null
        val offset = wordRegion.getOffset
        val length = wordRegion.getLength
        val methodName = textViewer.getDocument.get(offset, length)
        val parameterTypes = findParameterTypes(textViewer
          .getDocument.get, offset + length);
        val elems = MethodFinder.searchMethod(methodName,
          parameterTypes)
        val numberOfElements = elems.length;
        if (numberOfElements > 0) {
          val openAction = new OpenAction(
            routeEditor.getEditorSite());
          val hyperLinks = elems map (new JavaElementHyperlink(wordRegion,
            openAction, _, numberOfElements > 1))
          return hyperLinks.toArray;
        }
        return null
      } else {
        return null;
      }
    } catch {
      case _ => return null
    }
  }

  private def isIdentifierPart(c: Char) = {
    (c == '.') || Character.isUnicodeIdentifierPart(c);
  }

  def isInMethodNamePart(document: String, region: Region): Boolean = {
    val startIndex = region.getOffset() + region.getLength();
    try {
      if (document.charAt(region.getOffset()) == '(')
        return false;
      if (Character.isWhitespace(document.charAt(startIndex)) || document.substring(startIndex).trim().charAt(0) == '(')
        return true;
    } catch {
      case _ => return true;
    }
    return false;
  }

  def findParameterTypes(document: String, endOfMethodNameIndex: Int): Array[String] = {
    val startIndex = document.indexOf("(", endOfMethodNameIndex)
    if (startIndex == -1 || {val nlIndex =document.indexOf("\n", endOfMethodNameIndex); startIndex > nlIndex && nlIndex != -1}) {
      return Array()
    }
    val endIndex = document.indexOf(")", endOfMethodNameIndex)
    if (startIndex + 1 == endIndex) {
      return Array()
    }
    val parametersString = document.substring(startIndex + 1, endIndex)
    val paramStringArray = parametersString.split(",")
    val paramTypes = paramStringArray map (inferParameterType(_))
    paramTypes
  }

  def inferParameterType(parameterString: String): String = {
    val parameterStringTrimmed = parameterString.trim()
    val startIndex = parameterStringTrimmed.indexOf(":")
    if (startIndex == -1) {
      return "String"
    }
    val typeString = parameterStringTrimmed.substring(startIndex + 1).trim()
    val r = findWord(typeString, 0)
    return typeString.substring(r.getOffset(),
      r.getOffset() + r.getLength())
  }
  
  def findWord(document: String, offset: Int): Region =
    findWord(document.toSeq, offset)

  def findWord(document: Seq[Char], offset: Int): Region = {

    def find(p: Char => Boolean): Region = {
      var start = -2
      var end = -1

      try {
        var pos = offset

        while (pos >= 0 && p(document(pos)))
          pos -= 1

        start = pos

        pos = offset
        val len = document.length
        while (pos < len && p(document(pos)))
          pos += 1

        end = pos
      } catch {
        case ex: BadLocationException => // Deliberately ignored 
      }

      if (start >= -1 && end > -1) {
        if (start == offset && end == offset)
          new Region(offset, 0)
        else if (start == offset)
          new Region(start, end - start)
        else
          new Region(start + 1, end - start - 1)
      } else
        null
    }

    find(isIdentifierPart)
  }
}