package org.scalaide.play2

import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import org.scalaide.play2.routeeditor.hyperlink.RouteHyperlinkDetectorTest
import org.scalaide.play2.routeeditor.hyperlink.MethodFinderTest
import org.scalaide.play2.routeeditor.completion.HttpMethodCompletionComputerTest

@RunWith(value=classOf[org.junit.runners.Suite])
@SuiteClasses(value=Array(
    classOf[RouteHyperlinkDetectorTest],
//    Test disabled from the test suite as they require UI
//    classOf[RouteActionScannerTest],
//    classOf[RouteURIScannerTest],
//    classOf[RouteScannerTest],
    classOf[MethodFinderTest],
    classOf[HttpMethodCompletionComputerTest]))
class RouteTestSuite {
	
}
