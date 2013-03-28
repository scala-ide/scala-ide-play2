package org.scalaide.play2

import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses

@RunWith(value=classOf[org.junit.runners.Suite])
@SuiteClasses(value=Array(
    classOf[RouteTestSuite],
    classOf[TemplateTestSuite]
))
class TestSuite