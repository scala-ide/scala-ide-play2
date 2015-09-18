package org.scalaide.play2.templateeditor.processing

import org.eclipse.core.runtime.RegistryFactory
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.properties.PlayPreferences

object TemplateProcessingProvider {
  val ExtensionPointId = "org.scalaide.play2.template.processing"

  def templateProcessing(): TemplateProcessing = {
    val extensions = RegistryFactory.getRegistry.getConfigurationElementsFor(ExtensionPointId)
    val playVersion = Option(PlayPlugin.preferenceStore.getString(PlayPreferences.PlayVersion)).flatMap { version =>
      if (version.isEmpty) None else Some(version)
    }
    val version = playVersion.flatMap { pVer =>
      extensions.find {
        _.getChildren("version").map {
          _.getAttribute("supports")
        }.contains(pVer)
      }
    }
    version.get.createExecutableExtension("class").asInstanceOf[TemplateProcessing]
  }
}
