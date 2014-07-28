package org.scalaide.play2.routeeditor

import org.scalaide.core.api.ScalaProject

/** @note This marker trait should be moved in the scala-ide sdt.core bundle. */
trait HasScalaProject {
  def getScalaProject: Option[ScalaProject]
}