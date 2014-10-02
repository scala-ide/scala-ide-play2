package org.scalaide.play2.routeeditor.properties

import org.scalaide.play2.routeeditor.RouteSyntaxClasses
import org.scalaide.ui.syntax.preferences.BaseSyntaxColoringPreferencePage
import org.scalaide.play2.PlayPlugin

class RouteSyntaxColoringPreferencePage extends BaseSyntaxColoringPreferencePage(
    RouteSyntaxClasses.categories, 
    RouteSyntaxClasses.routeOtherCategory, 
    PlayPlugin.instance().getPreferenceStore,
    RouteSyntaxColoringPreferencePage.previewText, 
    RoutePreviewerFactoryConfiguration)

object RouteSyntaxColoringPreferencePage {
  val previewText =
    """#Route file
      |GET /static_part/:dynamic_part package1.package2.Class1.method(stringParameter, intParameter)
      |""".stripMargin
}