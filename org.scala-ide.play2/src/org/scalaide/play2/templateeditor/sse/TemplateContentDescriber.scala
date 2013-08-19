package org.scalaide.play2.templateeditor.sse

import java.io.BufferedReader
import java.io.InputStreamReader

import scala.annotation.tailrec

import org.eclipse.core.runtime.content.IContentDescriber
import org.eclipse.core.runtime.content.IContentDescription

/**
 * Detect a Play template file if it starts with `@(` or `@*`. 
 */
class TemplateContentDescriber extends IContentDescriber {
  
  override def describe(contents: java.io.InputStream, description: IContentDescription) = {

    import IContentDescriber._

    @tailrec
    def checkFirstNonEmptyLine(reader: BufferedReader): Int = {
      val line = reader.readLine()
      if (line == null)
        INDETERMINATE
      else {
        val trimmedLine = line.trim
        trimmedLine.length() match {
          case 0 =>
            checkFirstNonEmptyLine(reader)
          case 1 =>
            INVALID
          case _ =>
            val c0 = trimmedLine.charAt(0)
            val c1 = trimmedLine.charAt(1)
            if (c0 == '@' && (c1 == '*' || c1 == '('))
              IContentDescriber.VALID
            else
              IContentDescriber.INVALID
        }
      }
    }

    checkFirstNonEmptyLine(new BufferedReader(new InputStreamReader(contents, "UTF-8")))
  }

  override def getSupportedOptions() = Array()
}