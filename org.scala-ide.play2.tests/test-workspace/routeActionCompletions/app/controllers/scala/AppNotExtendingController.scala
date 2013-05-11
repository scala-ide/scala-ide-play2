package controllers.scala

import play.api._
import play.api.mvc._

object AppNotExtendingController {
  def actionMethod() = Action {}

  def nonActionMethod(): Unit = ()
}