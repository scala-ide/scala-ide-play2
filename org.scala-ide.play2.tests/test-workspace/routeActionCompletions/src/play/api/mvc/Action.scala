package play.api.mvc

trait Action extends EssentialAction

object Action {
  def apply[A](f: => A) = new Action {}
}