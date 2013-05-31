package play.api.mvc

class EssentialAction

object EssentialAction {
  def apply[A](f: => A) = new EssentialAction
}
