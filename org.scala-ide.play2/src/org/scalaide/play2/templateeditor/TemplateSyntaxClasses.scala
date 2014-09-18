package org.scalaide.play2.templateeditor

import org.scalaide.ui.syntax.ScalaSyntaxClass
import org.scalaide.ui.syntax.ScalaSyntaxClass.Category
import org.scalaide.ui.syntax.ScalaSyntaxClasses

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
}