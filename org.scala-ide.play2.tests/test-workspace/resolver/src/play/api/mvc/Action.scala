package play.api.mvc

class Action extends EssentialAction

object Action {
  def apply[A](f: => A) = new Action
}