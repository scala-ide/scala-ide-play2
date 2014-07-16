package org.scalaide.play2

import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import org.scalaide.play2.routeeditor.hyperlink.RouteHyperlinkDetectorTest
import org.scalaide.play2.routeeditor.hyperlink.MethodFinderTest
import org.scalaide.play2.routeeditor.RouteActionTest
import org.scalaide.play2.routeeditor.completion.HttpMethodCompletionComputerTest
import org.scalaide.play2.routeeditor.lexical.RoutePartitionTokeniserTest
import org.scalaide.play2.routeeditor.completion.UriCompletionComputerTest
import org.scalaide.play2.routeeditor.handlers.LocalRenameTest
import org.scalaide.play2.routeeditor.RouteUriTest

@RunWith(value=classOf[org.junit.runners.Suite])
@SuiteClasses(value=Array(
    classOf[RouteHyperlinkDetectorTest],
//    Test disabled from the test suite as they require UI
//    classOf[RouteActionScannerTest],
//    classOf[RouteURIScannerTest],
//    classOf[RouteScannerTest],
    classOf[MethodFinderTest],
    classOf[RouteActionTest],
    classOf[HttpMethodCompletionComputerTest],
    classOf[UriCompletionComputerTest],
    classOf[RoutePartitionTokeniserTest],
    classOf[LocalRenameTest],
    classOf[RouteUriTest]))
class RouteTestSuite {
	
}
