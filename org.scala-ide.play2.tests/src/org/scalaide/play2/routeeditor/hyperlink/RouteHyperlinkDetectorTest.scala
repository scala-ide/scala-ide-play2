package org.scalaide.play2.routeeditor.hyperlink

import scala.tools.eclipse.testsetup.SDTTestUtils
import scala.tools.eclipse.testsetup.TestProjectSetup

import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.IType
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Region
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.junit.AfterClass
import org.junit.Assert._
import org.junit.BeforeClass
import org.junit.Test
import org.scalaide.play2.routeeditor.RouteAction
import org.scalaide.play2.routeeditor.RouteTest

object RouteHyperlinkDetectorTest extends TestProjectSetup("routeHyperlink", srcRoot = "/%s/app/", bundleName = "org.scala-ide.play2.tests") {
  
  @AfterClass
  def projectCleanUp() {
    SDTTestUtils.deleteProjects(project)
  }
  
  @BeforeClass
  def cleanBuild {
    project.underlying.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor)
    project.underlying.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor)
    
  }
}

/** These tests are mostly based on the checking the value of IHyperlink.getTypeLabel.
 *  It should contain the fully qualified method signature, with parameter types if any, for Scala. And a mix of fully qualified name and signature for Java.
 */

class RouteHyperlinkDetectorTest extends RouteTest {

  import RouteHyperlinkDetectorTest._


  @Test
  def scalaControllerNoParameterAction() {
    testWithAnswer("""GET     /   controllers.ScalaA~pplication.intro""", "controllers.ScalaApplication.intro")
  }

  @Test
  def scalaControllerNoParameterAction2() {
    testWithAnswer("""GET     /   controllers.ScalaApplication~.withEmptyParams""", "controllers.ScalaApplication.withEmptyParams()")
  }

  @Test
  def scalaControllerEmptyParametersAction() {
    testWithAnswer("""GET     /   controllers.ScalaA~pplication.intro()""", "controllers.ScalaApplication.intro")
  }

  @Test
  def scalaControllerEmptyParametersAction2() {
    testWithAnswer("""GET     /   controllers.ScalaApplication~.withEmptyParams()""", "controllers.ScalaApplication.withEmptyParams()")
  }

  @Test
  def scalaControllerIntAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplicati~on.pInt(a: Int)""", "controllers.ScalaApplication.pInt(Int)")
  }

  @Test
  def scalaControllerStringAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplicatio~n.pString(a)""", "controllers.ScalaApplication.pString(String)")
  }

  @Test
  def scalaControllerRefAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplicatio~n.pRef(a: model.Element)""", "controllers.ScalaApplication.pRef(model.Element)")
  }

  @Test
  def scalaControllerOverloadedIntAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplicati~on.overloaded(a: Int)""", "controllers.ScalaApplication.overloaded(Int)")
  }

  @Test
  def scalaControllerOverloadedStringAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplicati~on.overloaded(a)""", "controllers.ScalaApplication.overloaded(String)")
  }

  @Test
  def scalaControllerOverloadedRefAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplicati~on.overloaded(a: model.Element)""", "controllers.ScalaApplication.overloaded(model.Element)")
  }

  @Test
  def scalaControllerOverloadedStringRefAction() {
    testWithAnswer("""GET     /:a   controllers.ScalaApplicati~on.overloaded(b, a: model.Element)""", "controllers.ScalaApplication.overloaded(String,model.Element)")
  }

  @Test
  def scalaNoMatch {
    testWithoutAnswer("""GET     /   some.unexisting.p~ackage.Application.index""")
  }

  @Test
  def scalaWrongParameterTypes {
    testWithoutAnswer("""GET     /:s   controllers.Sca~laApplication.pInt(s)""")
  }

  @Test
  def scalaWrongParameterTypesOverloaded {
    testWithoutAnswer("""GET     /:s   controllers.ScalaAppl~ication.overloaded(s, i: Int)""")
  }

  @Test
  def scalaControllerClassNoParameterAction() {
    testWithAnswer("""GET     /   @controllers.ScalaC~lass.intro""", "controllers.ScalaClass.intro")
  }

  @Test
  def scalaControllerClassNoParameterAction2() {
    testWithAnswer("""GET     /   @controllers.ScalaClass~.withEmptyParams""", "controllers.ScalaClass.withEmptyParams()")
  }

  @Test
  def scalaControllerClassEmptyParametersAction() {
    testWithAnswer("""GET     /   @controllers.ScalaC~lass.intro()""", "controllers.ScalaClass.intro")
  }

  @Test
  def scalaControllerClassEmptyParametersAction2() {
    testWithAnswer("""GET     /   @controllers.ScalaClass~.withEmptyParams()""", "controllers.ScalaClass.withEmptyParams()")
  }

  @Test
  def scalaControllerClassIntAction() {
    testWithAnswer("""GET     /:a   @controllers.ScalaCla~ss.pInt(a: Int)""", "controllers.ScalaClass.pInt(Int)")
  }

  @Test
  def scalaControllerClassStringAction() {
    testWithAnswer("""GET     /:a   @controllers.ScalaClas~s.pString(a)""", "controllers.ScalaClass.pString(String)")
  }

  @Test
  def scalaControllerClassRefAction() {
    testWithAnswer("""GET     /:a   @controllers.ScalaClas~s.pRef(a: model.Element)""", "controllers.ScalaClass.pRef(model.Element)")
  }

  @Test
  def scalaControllerClassOverloadedIntAction() {
    testWithAnswer("""GET     /:a   @controllers.ScalaCla~ss.overloaded(a: Int)""", "controllers.ScalaClass.overloaded(Int)")
  }

  @Test
  def scalaControllerClassOverloadedStringAction() {
    testWithAnswer("""GET     /:a   @controllers.ScalaCla~ss.overloaded(a)""", "controllers.ScalaClass.overloaded(String)")
  }

  @Test
  def scalaControllerClassOverloadedRefAction() {
    testWithAnswer("""GET     /:a   @controllers.ScalaCla~ss.overloaded(a: model.Element)""", "controllers.ScalaClass.overloaded(model.Element)")
  }

  @Test
  def scalaControllerClassOverloadedStringRefAction() {
    testWithAnswer("""GET     /:a   @controllers.ScalaCla~ss.overloaded(b, a: model.Element)""", "controllers.ScalaClass.overloaded(String,model.Element)")
  }

  @Test
  def scalaClassNoMatch {
    testWithoutAnswer("""GET     /   @some.unexisting.p~ackage.Class.index""")
  }

  @Test
  def scalaClassWrongParameterTypes {
    testWithoutAnswer("""GET     /:s   controllers.Sca~laClass.pInt(s)""")
  }

  @Test
  def scalaClassWrongParameterTypesOverloaded {
    testWithoutAnswer("""GET     /:s   controllers.ScalaCl~ass.overloaded(s, i: Int)""")
  }

  @Test
  def javaControllerNoParameterAction() {
    testWithAnswer("""GET     /   controllers.JavaAp~plication.intro""", "controllers.JavaApplication.intro()QObject;")
  }

  @Test
  def javaControllerNoParameterAction2() {
    testWithAnswer("""GET     /   controllers.JavaAp~plication.withEmptyParams""", "controllers.JavaApplication.withEmptyParams()QObject;")
  }

  @Test
  def javaControllerEmptyParametersAction() {
    testWithAnswer("""GET     /   controllers.JavaAp~plication.intro()""", "controllers.JavaApplication.intro()QObject;")
  }

  @Test
  def javaControllerEmptyParametersAction2() {
    testWithAnswer("""GET     /   controllers.JavaApplication.~withEmptyParams()""", "controllers.JavaApplication.withEmptyParams()QObject;")
  }

  @Test
  def javaControllerIntAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplicatio~n.pInt(a: Int)""", "controllers.JavaApplication.pInt(I)QObject;")
  }

  @Test
  def javaControllerStringAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplication~.pString(a)""", "controllers.JavaApplication.pString(QString;)QObject;")
  }

  @Test
  def javaControllerRefAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplication~.pRef(a: model.Element)""", "controllers.JavaApplication.pRef(QElement;)QObject;")
  }

  @Test
  def javaControllerOverloadedIntAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplicatio~n.overloaded(a: Int)""", "controllers.JavaApplication.overloaded(I)QObject;")
  }

  @Test
  def javaControllerOverloadedStringAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplicatio~n.overloaded(a)""", "controllers.JavaApplication.overloaded(QString;)QObject;")
  }

  @Test
  def javaControllerOverloadedRefAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplicatio~n.overloaded(a: model.Element)""", "controllers.JavaApplication.overloaded(QElement;)QObject;")
  }

  @Test
  def javaControllerOverloadedStringRefAction() {
    testWithAnswer("""GET     /:a   controllers.JavaApplicatio~n.overloaded(b, a: model.Element)""", "controllers.JavaApplication.overloaded(QString;QElement;)QObject;")
  }

  @Test
  def javaNoMatch {
    testWithoutAnswer("""GET     /   some.unexisting.p~ackage.Application.index""")
  }

  @Test
  def javaWrongParameterTypes {
    testWithoutAnswer("""GET     /:s   some.unexisting~.package.Application.pInt(s)""")
  }

  @Test
  def javaWrongParameterTypesOverloaded {
    testWithoutAnswer("""GET     /:s   some.unexisting.packa~ge.Application.overloaded(s, i: Int)""")
  }

  private def testWithAnswer(
    content: String,
    expectedLabel: String) {

    val file = RouteFile(content, List('~'))

    val actual = RouteHyperlinkComputer.detectHyperlinks(project, file.document, new Region(file.caretOffset('~'), 0), createJavaHyperlink)

    actual match {
      case Some(hyperlink) =>
        assertEquals("Wrong label", expectedLabel, hyperlink.getTypeLabel())
        assertEquals("Wrong region", file.document.getPartition(file.caretOffset('~')), hyperlink.getHyperlinkRegion())
      case _ =>
        fail("Wrong detectHyperlink result. Expected: Some(hyperlink), was: %s".format(actual))
    }
  }

  private def testWithoutAnswer(content: String) {

    val file = RouteFile(content, List('~'))

    val actual = RouteHyperlinkComputer.detectHyperlinks(project, file.document, new Region(file.caretOffset('~'), 0), createJavaHyperlink)

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