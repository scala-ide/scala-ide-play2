package org.scalaide.play2

import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import org.scalaide.play2.lexical.PlayDocumentPartitionerTests
import org.scalaide.play2.quickassist.ControllerMethodTest
import org.scalaide.play2.quickassist.ResolverTest
import org.scalaide.play2.routeeditor.completion.ActionContentAssistProcessorTest

@RunWith(value=classOf[org.junit.runners.Suite])
@SuiteClasses(value=Array(
    classOf[RouteTestSuite],
    classOf[TemplateTestSuite],
    classOf[ControllerMethodTest],
    classOf[ActionContentAssistProcessorTest],
    classOf[ResolverTest],
    classOf[PlayDocumentPartitionerTests]
))
class TestSuite