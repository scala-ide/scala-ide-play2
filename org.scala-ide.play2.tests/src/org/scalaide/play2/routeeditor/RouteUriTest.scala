package org.scalaide.play2.routeeditor

import org.junit.Test
import org.junit.Assert._

class RouteUriTest {
  
  @Test
  def parsingSimple {
    val route = RouteUri("/ab/cd/ef")
    assertEquals("Wrong uri parsing", List("ab", "cd", "ef"), route.parts)
  }
  
}