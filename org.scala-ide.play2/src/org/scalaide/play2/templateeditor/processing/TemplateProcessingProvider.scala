package org.scalaide.play2.templateeditor.processing

import org.eclipse.core.runtime.RegistryFactory
import org.scalaide.logging.HasLogger
import org.scalaide.play2.properties.PlayPreferences

object TemplateProcessingProvider extends HasLogger {
  val ExtensionPointId = "org.scalaide.play2.template.processing"

  def templateProcessing(templateVersion: Option[String] = None): TemplateProcessing = {
    val extensions = RegistryFactory.getRegistry.getConfigurationElementsFor(ExtensionPointId)
    val playVersion = templateVersion.orElse(TemplateVersionExhibitor.get.flatMap { version =>
      if (version.isEmpty) None else Some(version)
    }).orElse {
      eclipseLog.warn(s"Cannot find template version. Dropped to default ${PlayPreferences.DefaultPlayVersion}.")
      Some(PlayPreferences.DefaultPlayVersion)
    }
    val version = playVersion.flatMap { pVer =>
      extensions.find {
        _.getChildren("version").map {
          _.getAttribute("supports")
        }.contains(pVer)
      }
    }
    if (version.nonEmpty)
      version.get.createExecutableExtension("class").asInstanceOf[TemplateProcessing]
    else
      throw new IllegalStateException(s"Cannot find template processing for Play version: ${playVersion.getOrElse("version not found")}.")
  }
}
