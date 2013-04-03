package org.scalaide.play2.templateeditor.properties

object TemplateSyntaxColouringPreviewText {

  val previewText = """@* Template Comment *@
@(param: String, items: List[Item])
<html load="@test.action()">
@{
    class A {
      val b = "some string"
      val n = 12.3
      println("sym" + 'sym)
    }

    new A
}
@items.foreach { item =>
    @{return item}
}
</html>"""

}