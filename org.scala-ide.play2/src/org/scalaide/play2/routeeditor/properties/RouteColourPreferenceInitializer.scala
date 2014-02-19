package org.scalaide.play2.routeeditor.properties
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.resource.StringConverter
import org.eclipse.swt.graphics.RGB
import org.scalaide.ui.syntax.ScalaSyntaxClass
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.routeeditor.RouteSyntaxClasses._

class RouteColourPreferenceInitializer extends AbstractPreferenceInitializer {

  override def initializeDefaultPreferences() {
    doInitializeDefaultPreferences()
  }

  private def doInitializeDefaultPreferences() {
    val prefStore = PlayPlugin.preferenceStore
    setDefaultsForSyntaxClasses(prefStore)
    prefStore.setDefault(PlayPlugin.RouteFormatterMarginId, 3) // for formatter
  }

  private def setDefaultsForSyntaxClass(
    syntaxClass: ScalaSyntaxClass,
    foregroundRGB: RGB,
    enabled: Boolean = true,
    backgroundRGBOpt: Option[RGB] = None,
    bold: Boolean = false,
    italic: Boolean = false,
    strikethrough: Boolean = false,
    underline: Boolean = false)(implicit scalaPrefStore: IPreferenceStore) =
    {
      lazy val WHITE = new RGB(255, 255, 255)
      scalaPrefStore.setDefault(syntaxClass.enabledKey, enabled)
      scalaPrefStore.setDefault(syntaxClass.foregroundColourKey, StringConverter.asString(foregroundRGB))
      val defaultBackgroundColour = StringConverter.asString(backgroundRGBOpt getOrElse WHITE)
      scalaPrefStore.setDefault(syntaxClass.backgroundColourKey, defaultBackgroundColour)
      scalaPrefStore.setDefault(syntaxClass.backgroundColourEnabledKey, backgroundRGBOpt.isDefined)
      scalaPrefStore.setDefault(syntaxClass.boldKey, bold)
      scalaPrefStore.setDefault(syntaxClass.italicKey, italic)
      scalaPrefStore.setDefault(syntaxClass.underlineKey, underline)
    }

  private def setDefaultsForSyntaxClasses(implicit scalaPrefStore: IPreferenceStore) {
    setDefaultsForSyntaxClass(COMMENT, 			new RGB(128, 128, 0))
    setDefaultsForSyntaxClass(URI, 				new RGB(0, 0, 128))
    setDefaultsForSyntaxClass(URI_DYNAMIC, 		new RGB(128, 128, 255))
    setDefaultsForSyntaxClass(ACTION, 			new RGB(128, 0, 0))
    setDefaultsForSyntaxClass(ACTION_PACKAGE, 	new RGB(196, 196, 196))
    setDefaultsForSyntaxClass(ACTION_CLASS, 	new RGB(63, 46, 255))
    setDefaultsForSyntaxClass(ACTION_METHOD, 	new RGB(0, 0, 0), italic = true)
    setDefaultsForSyntaxClass(DEFAULT, 			new RGB(0, 0, 0))
    setDefaultsForSyntaxClass(HTTP_KEYWORD, 	new RGB(139, 10, 80), bold = true)
  }

}
