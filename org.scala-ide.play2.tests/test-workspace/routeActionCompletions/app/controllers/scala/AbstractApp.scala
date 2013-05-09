package controllers.scala

import play.api._
import play.api.mvc._

abstract class AbstractApp {
  def actionMethod() = Action {}
  
  val actionVal = Action {}
  
  def nonActionMethod(): Unit = ()
}