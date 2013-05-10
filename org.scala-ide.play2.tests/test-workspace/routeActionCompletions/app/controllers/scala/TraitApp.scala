package controllers.scala

import play.api._
import play.api.mvc._

trait TraitApp {
  def actionMethod() = Action {}
  
  def nonActionMethod(): Unit = ()
}