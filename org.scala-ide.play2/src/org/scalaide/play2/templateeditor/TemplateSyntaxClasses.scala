package org.scalaide.play2.templateeditor

import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass

object TemplateSyntaxClasses {
  val DEFAULT = ScalaSyntaxClass("Default", "template.default")
  val COMMENT = ScalaSyntaxClass("Comment", "template.comment")
  val SCALA = ScalaSyntaxClass("Scala", "template.scala")

  case class Category(name: String, children: List[ScalaSyntaxClass])

  val templateOtherCategory = Category("Other", List(
    DEFAULT, COMMENT, SCALA))

  val categories = List(templateOtherCategory)

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