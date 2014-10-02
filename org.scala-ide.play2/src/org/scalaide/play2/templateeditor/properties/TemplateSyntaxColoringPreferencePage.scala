package org.scalaide.play2.templateeditor.properties

import org.scalaide.ui.syntax.preferences.BaseSyntaxColoringPreferencePage
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses
import org.scalaide.play2.PlayPlugin


class TemplateSyntaxColoringPreferencePage extends BaseSyntaxColoringPreferencePage(
    TemplateSyntaxClasses.categories, 
    TemplateSyntaxClasses.scalaCategory, 
    PlayPlugin.instance().getPreferenceStore,
    TemplateSyntaxColoringPreferencePage.previewText, 
    TemplatePreviewerFactoryConfiguration)



object TemplateSyntaxColoringPreferencePage {
  
  val previewText =
    """@* Template Comment *@
      |@(param: String, items: List[Item])
      |<html load="@test.action()">
      |@{
      |    class A {
      |      val b = "some string"
      |      val n = 12.3
      |      println("sym" + 'sym)
      |    }
      |
      |    new A
      |}
      |@items.foreach { item =>
      |    @{return item}
      |}
      |</html>
      |""".stripMargin

}