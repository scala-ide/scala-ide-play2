package org.scalaide.play2.properties

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
import org.scalaide.play2.PlayPlugin

class PreferenceInitializer extends AbstractPreferenceInitializer {
  import PlayPreferences._

  override def initializeDefaultPreferences(): Unit = {
    PlayPlugin.preferenceStore.setDefault(PlayVersion, DefaultPlayVersion)
    PlayPlugin.preferenceStore.setDefault(TemplateImports, "")
  }

}
