package org.scalaide.play2.properties

import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.ui.IWorkbench
import org.eclipse.ui.IWorkbenchPreferencePage
import org.eclipse.ui.dialogs.PropertyPage

object PlayPreferences {

  /**
   * Preference containing the list of import to automatically add to the generate template code.
   *  The data is stored as the string which will be added to the generated source (to not recreate
   *  it everytime). The empty String represent an empty import list.
   *  [[org.scalaide.play2.properties.PlayPreferences.serializeImports]] and [[org.scalaide.play2.properties.PlayPreferences.deserializeImports]]
   *  need to be used when converting the preference value to/from Array[String].
   */
  final val TemplateImports = "templateImports"

  final val DefaultTemplateImports = List(
    "models._",
    "controllers._",
    "play.api.i18n._",
    "play.api.mvc._",
    "play.api.data._",
    "views.%format%._",
    "play.api.templates.PlayMagic._",
    "play.mvc.Http.Context.Implicit._")

  final val defaultImports = serializeImports(DefaultTemplateImports.toArray)

  final val PlayVersion = "playVersion"

  final val PlaySupportedVersion = List("2.6", "2.5")

  final val DefaultPlayVersion = PlaySupportedVersion.head

  // Regex used for the operations on the templateImports preference.
  private val importsRegex = "import ([^\n]+)\n".r

  /**
   * @see [[org.scalaide.play2.properties.PlayPreferences.TemplateImports]]
   */
  def serializeImports(entries: Array[String]): String = {
    if (entries.length == 0) {
      ""
    } else {
      entries.mkString("import ", "\nimport ", "\n")
    }
  }

  /**
   * @see [[org.scalaide.play2.properties.PlayPreferences.TemplateImports]]
   */
  def deserializeImports(s: String): Array[String] = {
    if (s.length == 0) {
      new Array(0)
    } else {
      importsRegex.findAllIn(s).matchData.map(m => m.group(1)).toArray
    }
  }

}

/** An empty preference page, used as an intermediary node under the Play page.  */
class PlayPreferences extends PropertyPage with IWorkbenchPreferencePage {
  override def createContents(parent: Composite): Control = {
    new Composite(parent, SWT.NULL)
  }

  override def init(workbench: IWorkbench): Unit = {}
}
