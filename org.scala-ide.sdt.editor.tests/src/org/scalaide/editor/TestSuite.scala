package org.scalaide.editor

import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import org.scalaide.editor.util.RegionUtilsTest

@RunWith(value = classOf[org.junit.runners.Suite])
@SuiteClasses(value = Array(
  classOf[WordFinderTest],
  classOf[RegionUtilsTest]))
class TestSuite