package org.scalaide.play2.templateeditor.processing

import org.eclipse.core.resources.IFile
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.properties.PlayPreferences

object TemplateVersionExtractor {
  def fromIFile(resource: IFile): Option[String] =
    PlayPlugin.instance().asPlayProject(resource.getProject).flatMap { project =>
      val templateVersion = project.cachedPreferenceStore.getString(PlayPreferences.PlayVersion)
      if (templateVersion.isEmpty())
        None
      else
        Some(templateVersion)
    }
}
