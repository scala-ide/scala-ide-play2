package org.scalaide.play2


import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.Assert._
import org.junit.runners.Suite.SuiteClasses
import org.scalaide.play2.routeeditor.hyperlink.RouteHyperlinkDetectorTest
import org.scalaide.play2.routeeditor.hyperlink.MethodFinderTest

@RunWith(value=classOf[org.junit.runners.Suite])
@SuiteClasses(value=Array(
    classOf[RouteHyperlinkDetectorTest],
//    Test disabled from the test suite as they require UI
//    classOf[RouteActionScannerTest],
//    classOf[RouteURIScannerTest],
//    classOf[RouteScannerTest],
    classOf[MethodFinderTest]))
class RouteTestSuite {
	
}
