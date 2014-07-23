package org.scalaide.play2.lexical

import org.eclipse.jface.text.TypedRegion
import org.junit.Assert.assertEquals
import org.junit.Test
import org.scalaide.play2.routeeditor.RouteTest
import org.scalaide.play2.routeeditor.lexical.RoutePartitions

class PlayDocumentPartitionerTests extends RouteTest {
  
  @Test
  def httpToken {
    testToken("G%ET / controller", new TypedRegion(0, 3, RoutePartitions.ROUTE_HTTP))
  }
  
  @Test
  def uriToken {
    testToken("GET /path/p%ath controller", new TypedRegion(4, 10, RoutePartitions.ROUTE_URI))
  }
  
  @Test
  def actionToken {
    testToken("GET / contro%ller", new TypedRegion(6, 10, RoutePartitions.ROUTE_ACTION))
  }
  
  @Test
  def beginningOfFileDefaultToken {
    testToken(" % GET / controller", new TypedRegion(0, 2, RoutePartitions.ROUTE_DEFAULT))
  }
  
  @Test
  def endOfFileDefaultToken {
    testToken("GET / controller %", new TypedRegion(17, 0, RoutePartitions.ROUTE_DEFAULT))
  }
  
  @Test
  def surroundedDefaultToken {
    testToken("GET %  / controller", new TypedRegion(3, 3, RoutePartitions.ROUTE_DEFAULT))
  }
  
  private def testToken(rawContent: String, expectedToken: TypedRegion) = {
    val file = RouteFile(rawContent, List('%'))
    val caretOffset = file.caretOffset('%')
    val partition = file.document.getPartition(caretOffset)
    assertEquals("Unexpected token", expectedToken, partition)
  }
}