package org.scalaide.play2

import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import org.scalaide.play2.templateeditor.lexical.TemplatePartitionTokeniserTest
import org.scalaide.play2.templateeditor.lexical.TemplateCompilationUnitTest
import org.scalaide.play2.templateeditor.sse.lexical.TemplateRegionParserTest
import org.scalaide.play2.indenter.TemplateAutoIndentTest
import org.scalaide.play2.templateeditor.sse.TemplateContentDescriberTest
@RunWith(value = classOf[org.junit.runners.Suite])
@SuiteClasses(value = Array(
  classOf[TemplateAutoIndentTest],
  classOf[TemplatePartitionTokeniserTest],
  classOf[TemplateCompilationUnitTest],
  classOf[TemplateRegionParserTest],
  classOf[TemplateContentDescriberTest]))
class TemplateTestSuite {

}
