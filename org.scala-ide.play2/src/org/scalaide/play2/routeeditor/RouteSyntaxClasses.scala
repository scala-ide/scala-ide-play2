package org.scalaide.play2.routeeditor

import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass
import org.scalaide.play2.properties.Category

object RouteSyntaxClasses {
  val DEFAULT = ScalaSyntaxClass("Default", "route.default")
  val COMMENT = ScalaSyntaxClass("Comment", "route.comment")
  val URI = ScalaSyntaxClass("URI", "route.uri")
  val URI_DYNAMIC = ScalaSyntaxClass("URI Dynamic", "route.uriDynamic")
  val ACTION = ScalaSyntaxClass("Action", "route.action")
  val ACTION_PACKAGE = ScalaSyntaxClass("Action package", "route.actionPackage")
  val ACTION_CLASS = ScalaSyntaxClass("Action class", "route.actionClass")
  val ACTION_METHOD = ScalaSyntaxClass("Action method name", "route.actionMethod")
  val HTTP_KEYWORD = ScalaSyntaxClass("HTTP keyword", "route.httpKeyword")

  val routeURICategory = Category("URI", List(
    URI, URI_DYNAMIC))

  val routeActionCategory = Category("Action", List(
    ACTION, ACTION_PACKAGE, ACTION_CLASS, ACTION_METHOD))

  val routeOtherCategory = Category("Other", List(
    DEFAULT, COMMENT, HTTP_KEYWORD))

  val categories = List(routeURICategory, routeActionCategory, routeOtherCategory)

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