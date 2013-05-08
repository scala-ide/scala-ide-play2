package org.scalaide.play2.routeeditor

import scala.tools.eclipse.ScalaProject

trait HasScalaProject {
  def getScalaProject: Option[ScalaProject]
}