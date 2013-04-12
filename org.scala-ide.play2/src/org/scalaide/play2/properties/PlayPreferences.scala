package org.scalaide.play2.properties

import scala.tools.eclipse.logging.HasLogger
import scala.tools.eclipse.properties.EclipseSettings
import scala.tools.eclipse.properties.ScalaPluginPreferencePage

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Group
import org.eclipse.ui.IWorkbench
import org.eclipse.ui.IWorkbenchPreferencePage
import org.eclipse.ui.dialogs.PropertyPage
import org.scalaide.play2.PlayPlugin

object PlayPreferences {

  /** Preference containing the list of import to automatically add to the generate template code.
   *  The data is stored as the string which will be added to the generated source (to not recreate
   *  it everytime). The empty String represent an empty import list.
   *  [[org.scalaide.play2.properties.PlayPreferences.serializeImports]] and [[org.scalaide.play2.properties.PlayPreferences.deserializeImports]]
   *  need to be used when converting the preference value to/from Array[String].
   */
  final val TemplateImports = "templateImports"

  // Regex used for the operations on the templateImports preference.
  private val importsRegex = "import ([^\n]+)\n".r

  /** @see [[org.scalaide.play2.properties.PlayPreferences.TemplateImports]]
   */
  def serializeImports(entries: Array[String]): String = {
    if (entries.length == 0) {
      ""
    } else {
      entries.mkString("import ", "\nimport ", "\n")
    }
  }

  /** @see [[org.scalaide.play2.properties.PlayPreferences.TemplateImports]]
   */
  def deserializeImports(s: String): Array[String] = {
    if (s.length == 0) {
      new Array(0)
    } else {
      importsRegex.findAllIn(s).matchData.map(m => m.group(1)).toArray
    }
  }

}

class PlayPreferences extends PropertyPage with IWorkbenchPreferencePage with EclipseSettings
  with ScalaPluginPreferencePage with HasLogger {

  /** Pulls the preference store associated with this plugin */
  override def doGetPreferenceStore(): IPreferenceStore = {
    PlayPlugin.preferenceStore
  }

  override def init(wb: IWorkbench) {}

  /** Returns the id of what preference page we use */
  import EclipseSetting.toEclipseBox
  override val eclipseBoxes: List[EclipseSetting.EclipseBox] = Nil

  def createContents(parent: Composite): Control = {
    val composite = {
      //No Outer Composite
      val tmp = new Composite(parent, SWT.NONE)
      val layout = new GridLayout(1, false)
      tmp.setLayout(layout)
      val data = new GridData(GridData.FILL)
      data.grabExcessHorizontalSpace = true
      data.horizontalAlignment = GridData.FILL
      tmp.setLayoutData(data)
      tmp
    }

    eclipseBoxes.foreach(eBox => {
      val group = new Group(composite, SWT.SHADOW_ETCHED_IN)
      group.setText(eBox.name)
      val layout = new GridLayout(3, false)
      group.setLayout(layout)
      val data = new GridData(GridData.FILL)
      data.grabExcessHorizontalSpace = true
      data.horizontalAlignment = GridData.FILL
      group.setLayoutData(data)
      eBox.eSettings.foreach(_.addTo(group))
    })
    composite
  }

  override def performOk = try {
    eclipseBoxes.foreach(_.eSettings.foreach(_.apply()))
    save()
    true
  } catch {
    case ex: Throwable => eclipseLog.error(ex); false
  }

  def updateApply = updateApplyButton

  /** Updates the apply button with the appropriate enablement. */
  protected override def updateApplyButton(): Unit = {
    if (getApplyButton != null) {
      if (isValid) {
        getApplyButton.setEnabled(isChanged)
      } else {
        getApplyButton.setEnabled(false)
      }
    }
  }

  def save(): Unit = {
    //Don't let user click "apply" again until a change
    updateApplyButton
  }
}
