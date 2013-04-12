package org.scalaide.play2.util

import org.eclipse.core.expressions.PropertyTester
import org.eclipse.core.resources.IProject
import org.scalaide.play2.PlayPlugin
import org.eclipse.jdt.core.IJavaProject

object Play2PropertyTester {
  final val IsPlayProject = "isPlay2Project"
}

/** Eclipse property tester. Can check if a project is a play2 project.
 */
class Play2PropertyTester() extends PropertyTester {

  // from IPropertyTester

  override def test(receiver: Any, property: String, args: Array[Object], expectedValue: Any): Boolean = {
    import Play2PropertyTester._

    property match {
      case IsPlayProject =>
        receiver match {
          case project: IProject =>
            PlayPlugin.instance().asPlayProject(project).isDefined
          case project: IJavaProject =>
            PlayPlugin.instance().asPlayProject(project.getProject()).isDefined
          case _ =>
            false
        }
      case _ =>
        false
    }
  }

}