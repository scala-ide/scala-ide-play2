package org.scalaide.play2.templateeditor.properties

import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass
import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClasses
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.resource.StringConverter
import org.eclipse.swt.graphics.RGB
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses.COMMENT
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses.MAGIC_AT
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses.DEFAULT
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses.PLAIN
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses.BRACE

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
    val commentColor = new RGB(63, 127, 95)
    val scalaDefaultColor = new RGB(128, 128, 128)
    setDefaultsForSyntaxClass(COMMENT, commentColor)
    setDefaultsForSyntaxClass(PLAIN, new RGB(196, 0, 0))
    setDefaultsForSyntaxClass(DEFAULT, new RGB(0, 0, 0))
    setDefaultsForSyntaxClass(MAGIC_AT, new RGB(180, 40, 160), bold = true)
    setDefaultsForSyntaxClass(BRACE, new RGB(180, 100, 160), bold = true)

    // Scala syntactic
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.SINGLE_LINE_COMMENT, commentColor)
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.MULTI_LINE_COMMENT, commentColor)
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.SCALADOC, new RGB(63, 95, 191))
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.KEYWORD, new RGB(127, 0, 85), bold = true)
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.STRING, new RGB(42, 0, 255))
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.DEFAULT, scalaDefaultColor)
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.OPERATOR, scalaDefaultColor)
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.BRACKET, scalaDefaultColor)
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.RETURN, new RGB(127, 0, 85), bold = true)

    // XML, see org.eclipse.wst.xml.ui.internal.preferences.XMLUIPreferenceInitializer
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.XML_COMMENT, new RGB(63, 85, 191))
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.XML_ATTRIBUTE_VALUE, new RGB(42, 0, 255), italic = true)
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.XML_ATTRIBUTE_NAME, new RGB(127, 0, 127))
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.XML_ATTRIBUTE_EQUALS, scalaDefaultColor)
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.XML_TAG_DELIMITER, new RGB(0, 128, 128))
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.XML_TAG_NAME, new RGB(63, 127, 127))
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.XML_PI, new RGB(0, 128, 128))
    setDefaultsForSyntaxClass(ScalaSyntaxClasses.XML_CDATA_BORDER, new RGB(0, 128, 128))
  }

}
