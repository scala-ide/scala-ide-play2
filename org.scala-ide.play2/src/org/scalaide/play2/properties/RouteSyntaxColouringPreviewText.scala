package org.scalaide.play2.properties

import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass

object RouteSyntaxColouringPreviewText {

  val previewText = """#Route file
GET /static_part/:dynamic_part package1.package2.Class1.method(stringParameter, intParameter)"""

  case class ColouringLocation(syntaxClass: ScalaSyntaxClass, offset: Int, length: Int)

//  private val identifierToSyntaxClass: Map[String, ScalaSyntaxClass] = Map(
//    "foo" -> PACKAGE,
//    "bar" -> PACKAGE,
//    "baz" -> PACKAGE,
//    "Annotation" -> ANNOTATION,
//    "Class" -> CLASS,
//    "CaseClass" -> CASE_CLASS,
//    "CaseObject" -> CASE_OBJECT,
//    "Trait" -> TRAIT,
//    "Int" -> CLASS,
//    "method" -> METHOD,
//    "param" -> PARAM,
//    "lazyLocalVal" -> LAZY_LOCAL_VAL,
//    "localVal" -> LOCAL_VAL,
//    "localVar" -> LOCAL_VAR,
//    "lazyTemplateVal" -> LAZY_TEMPLATE_VAL,
//    "templateVal" -> TEMPLATE_VAL,
//    "templateVar" -> TEMPLATE_VAR,
//    "T" -> TYPE_PARAMETER,
//    "Type" -> TYPE,
//    "Object" -> OBJECT)
//
//  val semanticLocations: List[ColouringLocation] =
//    for {
//      token <- ScalaLexer.rawTokenise(previewText, forgiveErrors = true)
//      if token.tokenType.isId
//      syntaxClass <- identifierToSyntaxClass get token.text
//    } yield ColouringLocation(syntaxClass, token.offset, token.length)

}