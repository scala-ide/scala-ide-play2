package play.api.mvc

trait Action

object Action {
  def apply[A](f: => A) = new Action {}
}