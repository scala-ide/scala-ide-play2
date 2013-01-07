package org.scalaide.play2

import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.Assert._
import org.junit.runners.Suite.SuiteClasses
import org.scalaide.play2.templateeditor.lexical.TemplatePartitionTokeniserTest
import org.scalaide.play2.templateeditor.lexical.TemplateCompilationUnitTest

class DummyTest extends scala.tools.eclipse.testsetup.TestProjectSetup("aProject", bundleName = "org.scala-ide.play2.tests") {
  @Test
  def dummyTest() = {
  }
}

@RunWith(value = classOf[org.junit.runners.Suite])
@SuiteClasses(value = Array(classOf[DummyTest]))
// NOTE: The following test are commented due to the error:
//[org.eclipse.debug.core] error can't determine modifiers of missing type org.eclipse.pde.internal.ui.wizards.imports.PluginImportHelper
  // classOf[TemplatePartitionTokeniserTest],
  // classOf[TemplateCompilationUnitTest]))
class TemplateTestSuite {

}
