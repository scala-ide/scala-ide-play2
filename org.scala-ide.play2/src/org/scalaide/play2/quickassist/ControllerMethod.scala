package org.scalaide.play2.quickassist

/** A controller method definition, with helper methods to generate the route-file
 *  syntax.
 */
case class ControllerMethod(fullName: String, params: List[(String, String)]) {
  import ControllerMethod._

  /** Return the route-call syntax, simplifying types if possible.
   *
   *  A parameter of type String is omitted, and primitive types are stripped the
   *  `scala` package prefix.
   *
   *  @see http://www.playframework.com/documentation/2.1.1/ScalaRouting
   */
  def toRouteCallSyntax: String = {
    val parts = for ((name, tpe) <- params) yield simplifyType(tpe) match {
      case None => name
      case Some(simpleTpe)  => s"$name: $simpleTpe"
    }

    s"$fullName(${parts.mkString(", ")})"
  }
}

object ControllerMethod {
  /** Map a type to a pretty name to use in the route file.
   *  Strings are mapped to the empty string
   */
  private val simplifyType = Map(
    "java.lang.String" -> None,
    "scala.String" -> None,
    "String" -> None,
    "scala.Boolean" -> Some("Boolean"),
    "scala.Byte" -> Some("Byte"),
    "scala.Short" -> Some("Short"),
    "scala.Char" -> Some("Char"),
    "scala.Int" -> Some("Int"),
    "scala.Long" -> Some("Long"),
    "scala.Float" -> Some("Float"),
    "scala.Double" -> Some("Double"),

    // Java types
    "boolean" -> Some("Boolean"),
    "byte" -> Some("Byte"),
    "short" -> Some("Short"),
    "char" -> Some("Char"),
    "int" -> Some("Int"),
    "long" -> Some("Long"),
    "float" -> Some("Float"),
    "double" -> Some("Double")) withDefault (Some.apply)
}