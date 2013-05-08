package controllers.simple

import play.api._
import play.api.mvc._

object SimpleScalaPlayApp extends Controller {
  def foo = Action {}

  def bar() = Action {}

  val buz = Action {}
  
  lazy val lazyBuz = Action {}
  
  var boo = Action {}
  
  def nonActionMethod: Unit = ()
  
  def withStringArg(s: String) = Action {}
  
  def withIntArg(i: Int) = Action {}
  
  def overloadedAction(s: String) = Action {}
  def overloadedAction(id: Long) = Action {}
  def overloadedAction() = Action {}
}
