package org.scalaide.play2

trait PlayClassNames {
  protected def controllerClassFullName: String
  protected def actionClassFullName: String
}

object ScalaPlayClassNames {
  final val ControllerClassFullName = "play.api.mvc.Controller"
  final val ActionClassFullName = "play.api.mvc.EssentialAction"
}

trait ScalaPlayClassNames extends PlayClassNames {
  override protected def controllerClassFullName: String = ScalaPlayClassNames.ControllerClassFullName
  override protected def actionClassFullName: String = ScalaPlayClassNames.ActionClassFullName
}

object JavaPlayClassNames {
  final val ControllerClassFullName = "play.mvc.Controller"
  final val ActionClassFullName = "play.mvc.Result"
}

trait JavaPlayClassNames extends PlayClassNames {
  override protected def controllerClassFullName: String = JavaPlayClassNames.ControllerClassFullName
  override protected def actionClassFullName: String = JavaPlayClassNames.ActionClassFullName

  private final def unresolvedActionClassName: String = "QResult;"
  private final def resolvedBinaryFullActionClassName: String = "Lplay/mvc/Result;"

  final protected def allActionClassNameCandidates: Set[String] = Set(unresolvedActionClassName, resolvedBinaryFullActionClassName)
}