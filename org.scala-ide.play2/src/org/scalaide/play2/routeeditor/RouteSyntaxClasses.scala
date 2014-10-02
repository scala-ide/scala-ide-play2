package org.scalaide.play2.routeeditor

import org.scalaide.ui.syntax.ScalaSyntaxClass
import org.scalaide.ui.syntax.ScalaSyntaxClass.Category

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
}