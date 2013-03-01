package org.scalaide.play2.properties

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
import org.scalaide.play2.PlayPlugin

class PreferenceInitializer extends AbstractPreferenceInitializer {

  override def initializeDefaultPreferences() {
    PlayPlugin.prefStore.setDefault(PlayPreferences.TemplateImports, "import play.api.templates._\nimport play.api.templates.PlayMagic._\n")
  }

}