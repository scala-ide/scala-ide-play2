package play.api.mvc

trait EssentialAction

object EssentialAction {
  def apply[A](f: => A) = new EssentialAction {}
}
