package org.scalaide.play2.routeeditor.hyperlink

import scala.tools.eclipse.testsetup.TestProjectSetup
import org.junit.Test
import org.eclipse.jface.text.IRegion
import org.junit.Assert._
import org.junit.AfterClass
import scala.tools.eclipse.testsetup.SDTTestUtils
import org.scalaide.play2.routeeditor.TestDocumentWithRoutePartition
import org.eclipse.jface.text.TypedRegion
import org.eclipse.jface.text.Region
import org.scalaide.play2.routeeditor.lexical.RoutePartitions
import scala.tools.eclipse.hyperlink.text.Hyperlink.ScalaHyperlink
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.scalaide.play2.routeeditor.RouteAction
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.IType
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.junit.Before
import org.eclipse.core.runtime.NullProgressMonitor
import org.scalaide.play2.routeeditor.RouteActionTest

object RouteHyperlinkDetectorTest extends TestProjectSetup("routeHyperlink", srcRoot = "/%s/app/", bundleName = "org.scala-ide.play2.tests") {
  @AfterClass
  def projectCleanUp() {
    SDTTestUtils.deleteProjects(project)
  }
}

/** These tests are mostly based on the checking the value of IHyperlink.getTypeLabel.
 *  It should contain the fully qualified method signature, with parameter types if any, for Scala. And a mix of fully qualified name and signature for Java.
 */

class RouteHyperlinkDetectorTest {

  import RouteHyperlinkDetectorTest._

  @Before
  def cleanBuild {
    project.underlying.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor)
    project.underlying.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor)

  }

  @Test
  def scalaControllerNoParameterAction() {
    testWithAnswer("""GET     /   controllers.ScalaApplication.intro""", 30, "controllers.ScalaApplication.intro")
  }

  @Test
  def scalaControllerNoParameterAction2() {
    testWithAnswer("""GET     /   controllers.ScalaApplication.withEmptyParams""", 40, "controllers.ScalaApplication.withEmptyParams()")
  }

  @Test
  def scalaControllerEmptyParametersAction() {
    testWithAnswer("""GET     /   controllers.ScalaApplication.intro()""", 30, "controllers.ScalaApplication.intro")
  }

  @Test
  def scalaControllerEmptyParametersAction2() {
    testWithAnswer("""GET     /   controllers.ScalaApplication.withEmptyParams()""", 40, "controllers.ScalaApplication.withEmptyParams()")
  }

  @Test
  def scalaControllerIntAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplication.pInt(a: Int)""", 40, "controllers.ScalaApplication.pInt(Int)")
  }

  @Test
  def scalaControllerStringAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplication.pString(a)""", 41, "controllers.ScalaApplication.pString(String)")
  }

  @Test
  def scalaControllerRefAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplication.pRef(a: model.Element)""", 41, "controllers.ScalaApplication.pRef(model.Element)")
  }

  @Test
  def scalaControllerOverloadedIntAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplication.overloaded(a: Int)""", 40, "controllers.ScalaApplication.overloaded(Int)")
  }

  @Test
  def scalaControllerOverloadedStringAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplication.overloaded(a)""", 40, "controllers.ScalaApplication.overloaded(String)")
  }

  @Test
  def scalaControllerOverloadedRefAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplication.overloaded(a: model.Element)""", 40, "controllers.ScalaApplication.overloaded(model.Element)")
  }

  @Test
  def scalaControllerOverloadedStringRefAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplication.overloaded(b, a: model.Element)""", 40, "controllers.ScalaApplication.overloaded(String,model.Element)")
  }

  @Test
  def scalaNoMatch {
    testWithoutAnswer("""GET     /   some.unexisting.package.Application.index""", 29)
  }

  @Test
  def scalaWrongParameterTypes {
    testWithoutAnswer("""GET     /:s   controllers.ScalaApplication.pInt(s)""", 29)
  }

  @Test
  def scalaWrongParameterTypesOverloaded {
    testWithoutAnswer("""GET     /:s   controllers.ScalaApplication.overloaded(s, i: Int)""", 35)
  }

  @Test
  def javaControllerNoParameterAction() {
    testWithAnswer("""GET     /   controllers.JavaApplication.intro""", 30, "controllers.JavaApplication.intro()QObject;")
  }

  @Test
  def javaControllerNoParameterAction2() {
    testWithAnswer("""GET     /   controllers.JavaApplication.withEmptyParams""", 40, "controllers.JavaApplication.withEmptyParams()QObject;")
  }

  @Test
  def javaControllerEmptyParametersAction() {
    testWithAnswer("""GET     /   controllers.JavaApplication.intro()""", 30, "controllers.JavaApplication.intro()QObject;")
  }

  @Test
  def javaControllerEmptyParametersAction2() {
    testWithAnswer("""GET     /   controllers.JavaApplication.withEmptyParams()""", 40, "controllers.JavaApplication.withEmptyParams()QObject;")
  }

  @Test
  def javaControllerIntAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplication.pInt(a: Int)""", 40, "controllers.JavaApplication.pInt(I)QObject;")
  }

  @Test
  def javaControllerStringAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplication.pString(a)""", 41, "controllers.JavaApplication.pString(QString;)QObject;")
  }

  @Test
  def javaControllerRefAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplication.pRef(a: model.Element)""", 41, "controllers.JavaApplication.pRef(QElement;)QObject;")
  }

  @Test
  def javaControllerOverloadedIntAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplication.overloaded(a: Int)""", 40, "controllers.JavaApplication.overloaded(I)QObject;")
  }

  @Test
  def javaControllerOverloadedStringAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplication.overloaded(a)""", 40, "controllers.JavaApplication.overloaded(QString;)QObject;")
  }

  @Test
  def javaControllerOverloadedRefAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplication.overloaded(a: model.Element)""", 40, "controllers.JavaApplication.overloaded(QElement;)QObject;")
  }

  @Test
  def javaControllerOverloadedStringRefAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplication.overloaded(b, a: model.Element)""", 40, "controllers.JavaApplication.overloaded(QString;QElement;)QObject;")
  }

  @Test
  def javaNoMatch {
    testWithoutAnswer("""GET     /   some.unexisting.package.Application.index""", 29)
  }

  @Test
  def javaWrongParameterTypes {
    testWithoutAnswer("""GET     /:s   some.unexisting.package.Application.pInt(s)""", 29)
  }

  @Test
  def javaWrongParameterTypesOverloaded {
    testWithoutAnswer("""GET     /:s   some.unexisting.package.Application.overloaded(s, i: Int)""", 35)
  }

  private def testWithAnswer(
    content: String,
    hyperlinkOffset: Int,
    expectedLabel: String) {

    val partitionOffset = RouteActionTest.actionPartitionOffset(content)
    val partitionLength = content.length() - partitionOffset
    val document = new TestDocumentWithRoutePartition(content, new TypedRegion(partitionOffset, partitionLength, RoutePartitions.ROUTE_ACTION))

    val actual = RouteHyperlinkComputer.detectHyperlinks(project, document, new Region(hyperlinkOffset, 0), createJavaHyperlink)

    actual match {
      case Some(hyperlink) =>
        assertEquals("Wrong label", expectedLabel, hyperlink.getTypeLabel())
        assertEquals("Wrong region", new Region(partitionOffset, partitionLength), hyperlink.getHyperlinkRegion())
      case _ =>
        fail("Wrong detectHyperlink result. Expected: Some(hyperlink), was: %s".format(actual))
    }
  }

  private def testWithoutAnswer(
    content: String,
    hyperlinkOffset: Int) {

    val partitionOffset = RouteActionTest.actionPartitionOffset(content)
    val partitionLength = content.length() - partitionOffset
    val document = new TestDocumentWithRoutePartition(content, new TypedRegion(partitionOffset, partitionLength, RoutePartitions.ROUTE_ACTION))

    val actual = RouteHyperlinkComputer.detectHyperlinks(project, document, new Region(hyperlinkOffset, 0), createJavaHyperlink)

    actual match {
      case None =>
      // this is the expected value
      case _ =>
        fail("Wrong detectHyperlink result. Expected: None, was: %s".format(actual))
    }

  }
  
  /** An implementation to create fake Java Hyperlinks.
   *  Creating 'real' ones would require the Eclipse UI.
   */
  private def createJavaHyperlink(routeAction: RouteAction, method: IJavaElement): IHyperlink = {
    new TestJavaHyperlink(routeAction.region, method)
  }

  private class TestJavaHyperlink(region: IRegion, element: IJavaElement) extends IHyperlink {
    def getHyperlinkRegion(): IRegion = region
    def getHyperlinkText(): String = ???
    def getTypeLabel(): String = element match {
      case method: IMethod =>
        "%s.%s%s".format(method.getParent().asInstanceOf[IType].getFullyQualifiedName(), method.getElementName(), method.getSignature())
      case _ =>
        fail("linked element should be a method: %s".format(element))
        "failed"
    }
    def open(): Unit = ???
  }
}