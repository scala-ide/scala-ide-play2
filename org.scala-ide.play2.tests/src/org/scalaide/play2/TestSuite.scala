package org.scalaide.play2

import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import org.scalaide.play2.quickassist.ControllerMethodTest
import org.scalaide.play2.quickassist.ResolverTest

@RunWith(value=classOf[org.junit.runners.Suite])
@SuiteClasses(value=Array(
    classOf[RouteTestSuite],
    classOf[TemplateTestSuite],
    classOf[ControllerMethodTest],
    classOf[ResolverTest]
))
class TestSuite