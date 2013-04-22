package org.scalaide.play2.quickassist

import org.junit.Test
import org.junit.Assert

class ControllerMethodTest {

  @Test
  def emptyParamCallSyntax() {
    val meth = ControllerMethod("controllers.Application.index", List())
    Assert.assertEquals("Unexpected routeCallSyntax", "controllers.Application.index()", meth.toRouteCallSyntax)
  }

  @Test
  def stringParamCallSyntax() {
    val meth = ControllerMethod("controllers.Application.index", List(("param1", "String")))
    Assert.assertEquals("Unexpected routeCallSyntax", "controllers.Application.index(param1)", meth.toRouteCallSyntax)

    val meth1 = ControllerMethod("controllers.Application.index", List(("param1", "java.lang.String")))
    Assert.assertEquals("Unexpected routeCallSyntax", "controllers.Application.index(param1)", meth1.toRouteCallSyntax)
  }

  @Test
  def primitiveParamCallSyntax() {
    val meth = ControllerMethod("controllers.Application.index", List(("param1", "scala.Long")))
    Assert.assertEquals("Unexpected routeCallSyntax", "controllers.Application.index(param1: Long)", meth.toRouteCallSyntax)

    val meth1 = ControllerMethod("controllers.Application.index", List(("param1", "Long")))
    Assert.assertEquals("Unexpected routeCallSyntax", "controllers.Application.index(param1: Long)", meth1.toRouteCallSyntax)
  }

  @Test
  def primitiveMultipleParamCallSyntax() {
    val meth = ControllerMethod("controllers.Application.index", List(("param1", "scala.Long"), ("param2", "Int"), ("param3", "String")))
    Assert.assertEquals("Unexpected routeCallSyntax", "controllers.Application.index(param1: Long, param2: Int, param3)", meth.toRouteCallSyntax)
  }
}