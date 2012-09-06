package org.scalaide.play2.templateeditor.properties

object TemplateSyntaxColouringPreviewText {

  val previewText = """@* Template Comment *@
@(param: String, items: List[Item])
<html load="@test.action()">
@{
    class A {
      val b = "some string"
    }
    
    new A
}
@items.foreach { item =>
    @item
}
</html>"""

}