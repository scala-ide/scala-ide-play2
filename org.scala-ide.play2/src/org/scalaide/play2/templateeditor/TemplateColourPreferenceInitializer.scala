

package org.scalaide.play2.templateeditor

import scala.tools.eclipse.semantichighlighting.SemanticHighlightingAnnotations
import scala.tools.eclipse.util.SWTUtils.fnToPropertyChangeListener
import scala.tools.eclipse.ScalaPlugin
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
import org.eclipse.jdt.internal.ui.JavaPlugin
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.resource.StringConverter
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.swt.graphics.RGB
import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses._

class TemplateColourPreferenceInitializer extends AbstractPreferenceInitializer {

  override def initializeDefaultPreferences() {
    doInitializeDefaultPreferences()
  }

  private def doInitializeDefaultPreferences() {
    setDefaultsForSyntaxClasses(PlayPlugin.prefStore)
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
    setDefaultsForSyntaxClass(COMMENT, 			new RGB(0, 128, 0))
    setDefaultsForSyntaxClass(SCALA,			new RGB(0, 0, 255))
    setDefaultsForSyntaxClass(PLAIN, 			new RGB(196, 0, 0))
    setDefaultsForSyntaxClass(DEFAULT, 			new RGB(0, 0, 0))
  }

}
