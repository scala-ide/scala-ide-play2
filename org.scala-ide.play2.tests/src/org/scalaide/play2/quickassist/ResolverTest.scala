package org.scalaide.play2.quickassist

import org.junit.Test
import org.junit.AfterClass
import org.scalaide.core.testsetup.SDTTestUtils
import org.scalaide.core.testsetup.TestProjectSetup
import org.junit.Assert

object ResolverTest extends TestProjectSetup("resolver", srcRoot = "/%s/app/", bundleName = "org.scala-ide.play2.tests") {
  @AfterClass
  def projectCleanUp() {
    SDTTestUtils.deleteProjects(project)
  }
}

class ResolverTest {
  import ResolverTest._
  import Assert._

  @Test
  def testScalaResolverPos() {
    val resolver = new ScalaControllerMethodResolver

    val scu = scalaCompilationUnit("controllers/Application.scala")
    val positions = SDTTestUtils.positionsOf(scu.getContents, "/*!*/")

    for (pos <- positions) {
      val cm = resolver.getControllerMethod(scu, pos)
      assertTrue(s"Could not resolve controller method at position $pos", cm.isDefined)
      assertTrue(s"${cm.get} is not in $scalaResolvedMethods", scalaResolvedMethods.contains(cm.get))
    }
  }

  @Test
  def testScalaResolverNeg() {
    val resolver = new ScalaControllerMethodResolver

    val scu = scalaCompilationUnit("controllers/NonController.scala")
    val positions = SDTTestUtils.positionsOf(scu.getContents, "/*!*/")

    for (pos <- positions) {
      val cm = resolver.getControllerMethod(scu, pos)
      assertTrue(s"Wrong controller method (${cm}) at position $pos", cm.isEmpty)
    }
  }

  @Test
  def testJavaResolverPos() {
    val resolver = new JavaControllerMethodResolver
    val cu = compilationUnit("controllers/JavaApplication.java")
    val positions = SDTTestUtils.positionsOf(cu.getBuffer().getCharacters(), "/*!*/")

    for (pos <- positions) {
      val cm = resolver.getControllerMethod(cu, pos)
      assertTrue(s"Could not resolve controller method at position $pos", cm.isDefined)
      assertTrue(s"${cm.get} is not in $javaResolvedMethods", javaResolvedMethods.contains(cm.get))
    }
  }

  @Test
  def testJavaResolverNeg() {
    val resolver = new ScalaControllerMethodResolver

    val scu = compilationUnit("controllers/JavaNonController.java")
    val positions = SDTTestUtils.positionsOf(scu.getBuffer().getCharacters(), "/*!*/")

    for (pos <- positions) {
      val cm = resolver.getControllerMethod(scu, pos)
      assertTrue(s"Wrong controller method (${cm}) at position $pos", cm.isEmpty)
    }
  }

  private val scalaResolvedMethods = Set(
    ControllerMethod("controllers.Application.index", List()),
    ControllerMethod("controllers.Application.post", List(("id", "Long"))),
    ControllerMethod("controllers.Application.postText", List(("text", "String"), ("id", "Int"))),
    ControllerMethod("controllers.Application.internalPostText1", List(("text", "String"), ("id", "Char"))),
    ControllerMethod("controllers.Application.internalPostText2", List(("text", "String"), ("id", "Short"))))

  private val javaResolvedMethods = Set(
    ControllerMethod("controllers.JavaApplication.index", List()),
    ControllerMethod("controllers.JavaApplication.index2", List(("lng", "long"))),
    ControllerMethod("controllers.JavaApplication.index3", List(("str", "java.lang.String"), ("id", "int"))),
    ControllerMethod("controllers.JavaApplication.index4", List(("f", "java.io.File"), ("id", "int"))))
}
