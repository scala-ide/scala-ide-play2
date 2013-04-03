package org.scalaide.play2.templateeditor

import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass
import org.scalaide.play2.properties.Category
import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClasses

object TemplateSyntaxClasses {
  val DEFAULT = ScalaSyntaxClass("Default", "template.default")
  val PLAIN = ScalaSyntaxClass("Plain", "template.plain")
  val COMMENT = ScalaSyntaxClass("Template Comment", "template.comment")
  val MAGIC_AT = ScalaSyntaxClass("Template Magic @", "template.at")
  val BRACE = ScalaSyntaxClass("Template Brace", "template.brace")

  val scalaCategory = Category("Scala", List(ScalaSyntaxClasses.KEYWORD,
    ScalaSyntaxClasses.STRING,
    ScalaSyntaxClasses.DEFAULT,
    ScalaSyntaxClasses.OPERATOR,
    ScalaSyntaxClasses.BRACKET,
    ScalaSyntaxClasses.RETURN,
    ScalaSyntaxClasses.SYMBOL,
    ScalaSyntaxClasses.MULTI_LINE_STRING,
    ScalaSyntaxClasses.NUMBER_LITERAL))

  val htmlCategory = Category("HTML", List(ScalaSyntaxClasses.XML_COMMENT, 
    ScalaSyntaxClasses.XML_ATTRIBUTE_VALUE, 
    ScalaSyntaxClasses.XML_ATTRIBUTE_NAME, 
    ScalaSyntaxClasses.XML_ATTRIBUTE_EQUALS, 
    ScalaSyntaxClasses.XML_TAG_DELIMITER, 
    ScalaSyntaxClasses.XML_TAG_NAME, 
    ScalaSyntaxClasses.XML_PI, 
    ScalaSyntaxClasses.XML_CDATA_BORDER))

  val commentsCategory = Category("Comments", List(ScalaSyntaxClasses.SINGLE_LINE_COMMENT,
    ScalaSyntaxClasses.MULTI_LINE_COMMENT,
    ScalaSyntaxClasses.SCALADOC, 
    COMMENT))

  val otherCategory = Category("Other", List(
    DEFAULT, PLAIN, MAGIC_AT, BRACE))

  val categories = List(scalaCategory, htmlCategory, commentsCategory, otherCategory)

  val ALL_SYNTAX_CLASSES = categories.flatMap(_.children)

  val ENABLED_SUFFIX = ".enabled"
  val FOREGROUND_COLOUR_SUFFIX = ".colour"
  val BACKGROUND_COLOUR_SUFFIX = ".backgroundColour"
  val BACKGROUND_COLOUR_ENABLED_SUFFIX = ".backgroundColourEnabled"
  val BOLD_SUFFIX = ".bold"
  val ITALIC_SUFFIX = ".italic"
  val UNDERLINE_SUFFIX = ".underline"

  val ALL_SUFFIXES = List(ENABLED_SUFFIX, FOREGROUND_COLOUR_SUFFIX, BACKGROUND_COLOUR_SUFFIX,
    BACKGROUND_COLOUR_ENABLED_SUFFIX, BOLD_SUFFIX, ITALIC_SUFFIX, UNDERLINE_SUFFIX)

  val ALL_KEYS = (for {
    syntaxClass <- ALL_SYNTAX_CLASSES
    suffix <- ALL_SUFFIXES
  } yield syntaxClass.baseName + suffix).toSet

}