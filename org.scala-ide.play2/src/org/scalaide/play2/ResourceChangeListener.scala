package org.scalaide.play2

import org.eclipse.core.resources.IResourceChangeListener
import org.eclipse.core.resources.IResourceChangeEvent
import org.eclipse.core.resources.IProject
import scala.tools.eclipse.ScalaPlugin
import javax.swing.JOptionPane
import org.eclipse.core.resources.IResource
/*
object ResourceChangeListener extends IResourceChangeListener {
  override def resourceChanged( event: IResourceChangeEvent) = {
    (event.getResource, event.getType) match {
//      case (project: IProject, IResourceChangeEvent.PRE_CLOSE) =>
//        disposeProject(project)
//      case (project: IProject, IResourceChangeEvent.PRE_REFRESH) =>
//        refreshProject(project)
      case (project: IProject, id: Int) =>
        shower(project.getName(), id)
      case (resource: IResource, id: Int) =>
        shower(resource.getName(), id)
      case _ =>
    }
  }
  
  def getPlayProject(project: IProject) = PlayPlugin.plugin.asPlayProject(project)
  
  def disposeProject(project: IProject) = {
    val playProject = getPlayProject(project)
    playProject match {
      case Some(pp) => pp.dispose()
      case None =>
    }
  }
  
  def refreshProject(project: IProject) = {
    val playProject = getPlayProject(project)
    JOptionPane.showMessageDialog(null, project.getName() + "refresh")
  }
  
  def shower(prefix: String, id: Int) = {
    JOptionPane.showMessageDialog(null, prefix + " :" + id)
  }
}
*/