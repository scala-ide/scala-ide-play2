package controllers.scala

import play.api._
import play.api.mvc._

class AppClass {
  def actionMethod() = Action {}
  
  val actionVal = Action {}
  
  def nonActionMethod(): Unit = ()
}