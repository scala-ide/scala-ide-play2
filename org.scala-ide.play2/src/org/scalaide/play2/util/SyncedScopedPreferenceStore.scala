package org.scalaide.play2.util

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.ProjectScope
import org.eclipse.ui.preferences.ScopedPreferenceStore

class SyncedScopedPreferenceStore(project: IProject, pluginId: String) {

  private val preferenceStore = new ScopedPreferenceStore(new ProjectScope(project), pluginId)

  def getString(name: String): String = {
    preferenceStore.synchronized {
      preferenceStore.getString(name)
    }
  }
}