package org.scalaide.play2

import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.Assert._
import org.junit.runners.Suite.SuiteClasses
import org.scalaide.play2.templateeditor.lexical.TemplatePartitionTokeniserTest
import org.scalaide.play2.templateeditor.lexical.TemplateCompilationUnitTest
import org.scalaide.play2.indenter.TemplateAutoIndentTest
@RunWith(value = classOf[org.junit.runners.Suite])
@SuiteClasses(value = Array(
  classOf[TemplateAutoIndentTest],
  classOf[TemplatePartitionTokeniserTest],
  classOf[TemplateCompilationUnitTest]))
class TemplateTestSuite {

}
