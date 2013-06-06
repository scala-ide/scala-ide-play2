package org.scalaide.play2.routeeditor.hyperlink

import org.junit.Test
import org.junit.Assert._
import scala.tools.eclipse.testsetup.TestProjectSetup
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.Signature
import org.junit.Test
import scala.Array.apply
import scala.Array.canBuildFrom
import org.junit.AfterClass
import scala.tools.eclipse.testsetup.SDTTestUtils

object MethodFinderTest extends TestProjectSetup("aProject", bundleName = "org.scala-ide.play2.tests") {
  @AfterClass
  def projectCleanUp() {
    SDTTestUtils.deleteProjects(project)
  }
}

class MethodFinderTest {
  MethodFinderTest
  
  val finder = new MethodFinder(MethodFinderTest.project.javaProject)
  import finder._
  
  @Test
  def getParametersStringTest() = {
    def t = testForGetParametersString _
    t ("(A)", Array("A"))
    t ("(A,B)", Array("A", "B"))
    t ("(A,B,C)", Array("A", "B", "C"))
    t ("()", Array())
  }
  
  private def testForGetParametersString(expected: String, parameterTypes: Array[String]) = {
    val actual =  getParametersString(parameterTypes)
    assertEquals("getParametersString() is not correct", expected, actual)
  }

  @Test
  def methodSearchTest() = {
    def t = testForMethodSearch _
    t ("JavaClass.method1", Array())
    t ("JavaClass.method2", Array())
    t ("JavaClass.method2", Array("String"))
    t ("JavaClass.method2", Array("int"))
    t ("JavaClass.method2", Array("int", "String"))
    t ("JavaClass.method2", Array("Integer"))
  }
  
  private def testForMethodSearch(methodName: String, parameterTypes: Array[String]) = {
    val actual = dec(searchMethod(methodName, parameterTypes))
    // as IMethod.getParameterTypes returns signature of types, we have to get signature for types!
    val expected =  methodName + getParametersString(parameterTypes map (Signature.createTypeSignature(_, false)))
    assertEquals("Could not find method", expected, actual)
  }

  private def dec(methods: Array[IJavaElement]): String = {
    if (methods.length != 1)
      return ""
    val method = methods(0).asInstanceOf[IMethod]
    val className = method.getParent.getElementName
    val methodName = method.getElementName
    val parameterTypes = method.getParameterTypes // It returns signature of parameter types!
    val paramsString = getParametersString(parameterTypes)
    className + "." + methodName + paramsString
  }
}