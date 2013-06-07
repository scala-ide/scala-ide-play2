package org.scalaide.play2.routeeditor

import org.junit.Test
import org.junit.Assert._

class RouteUriTest {
  
  @Test
  def parsingSimple {
    val route = RouteUri("/ab/cd/ef")
    assertEquals("Wrong uri parsing", List("ab", "cd", "ef"), route.parts)
    assertEquals("Wrong validity [should be true/valid]", true, route.isValid)
  }
  
  @Test
  def parsingInvalid {
    val route = RouteUri("ab/cd/ef")
    assertEquals("Wrong uri parsing", List("ab", "cd", "ef"), route.parts)
    assertEquals("Wrong validity [should be false/invalid]", false, route.isValid)
  }
  
  @Test
  def prefixLengthNoPrefix {
    testPrefixLength("/ab", List(), 1)
  }
  
  @Test
  def prefixLengthNoMatch {
    testPrefixLength("/ab", List("ba"), -1)
  }
  
  @Test
  def prefixLengthWholeMatch {
    testPrefixLength("/ab/cd", List("ab", "cd"), 6)
  }
  
  @Test
  def prefixLengthPrefixMatch {
    testPrefixLength("/ab/cd", List("ab"), 4)
  }
  
  @Test
  def prefixLengthInvalidUri {
    testPrefixLength("ab/cd", List("ab"), 3)
  }
  
  @Test
  def prefixLengthEmptyUri {
    testPrefixLength("/", List(), 1)
  }
  
  @Test
  def prefixLengthEmptyInvalidUri {
    testPrefixLength("", List(), 0)
  }
  
  private def testPrefixLength(rawUri: String, prefixParts: List[String], expectedLength: Int) {
    val route = RouteUri(rawUri)
    assertEquals("Wrong prefix length", expectedLength, route.prefixLength(prefixParts))
  }
}