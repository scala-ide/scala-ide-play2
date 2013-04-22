package play.api.mvc

class Action

object Action {
  def apply[A](f: => A) = new Action
}