package controllers.scala

import play.api._
import play.api.mvc._

object MembersVisibility {
  private def actionMethod1() = Action {}
  private[scala] def actionMethod2() = Action {}
  protected def actionMethod3() = Action {}
  protected[scala] def actionMethod4() = Action {}
  
  private val actionVal1 = Action {}
  private[scala] val actionVal2 = Action {}
  protected val actionVal3 = Action {}
  protected[scala] val actionVal4 = Action {}
  
  def visibleMethod = Action {}
  
  /** The completion engine in the route file should NOT report abstract methods.*/
  def abstractMethod: Action
}