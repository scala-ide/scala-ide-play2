package org.scalaide.play2

trait PlayClassNames {
  def controllerClassFullName: String
  def actionClassFullName: String
}

trait ScalaPlayClassNames extends PlayClassNames {
  override def controllerClassFullName: String = "play.api.mvc.Controller"
  override def actionClassFullName: String = "play.api.mvc.Action"
}

trait JavaPlayClassNames extends PlayClassNames {
  override def controllerClassFullName: String = "play.mvc.Controller"
  override def actionClassFullName: String = "play.mvc.Result"

  private final def unresolvedActionClassName: String = "QResult;"
  private final def resolvedBinaryFullActionClassName: String = "Lplay/mvc/Result;"

  final def allActionClassNameCandidates: Set[String] = Set(unresolvedActionClassName, resolvedBinaryFullActionClassName)
}