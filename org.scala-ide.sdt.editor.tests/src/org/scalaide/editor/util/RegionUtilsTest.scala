package org.scalaide.editor.util

import org.junit.Test
import org.junit.Assert._
import org.eclipse.jface.text.TypedRegion

class RegionUtilsTest {
  
  @Test
  def substractAContainsB {
    
    val a = List(new TypedRegion(0, 16, "A"))
    val b = List(new TypedRegion(1, 15, "B"))
    
    val expected= List(new TypedRegion(0, 1, "A")) 
    
    val actual= RegionHelper.subtract(a, b)
    
    assertEquals("Wrong result", expected, actual)
    
  }
  
  @Test
  def substractEmtpies {
    val a = List(new TypedRegion(0, 0, "A"))
    val b = List(new TypedRegion(0, 0, "B"))
    
    val expected= Nil
    
    val actual= RegionHelper.subtract(a, b)
    
    assertEquals("Wrong result", expected, actual)
  }

  @Test
  def substract_issue_123 {
    val a = List(new TypedRegion(808, 93, "A"), new TypedRegion(908, 52, "A"))
    val b = List(new TypedRegion(807, 20, "B"), new TypedRegion(881, 13, "B"), new TypedRegion(899, 3, "B"))

    val expected = List(new TypedRegion(827, 54, "A"), new TypedRegion(894, 5, "A"), new TypedRegion(908, 52, "A"))

    val actual = RegionHelper.subtract(a, b)

    assertEquals("Wrong result", expected, actual)

  }

  /** |       ++++
   *  | ----
   */
  @Test
  def overlapBefore {
    testNoOverlap(13, 7, 5, 5)
  }

  /** |     ++++
   *  | ----
   */
  @Test
  def overlapBeforeTouching {
    testNoOverlap(13, 7, 5, 8)
  }

  /** |   ++++
   *  | ----
   */
  @Test
  def overlapOverBeginning {
    testOverlap(13, 7, 11, 5)
  }

  /** | ++++++
   *  |  ----
   */
  @Test
  def overlapInsideSmaller {
    testOverlap(13, 7, 14, 3)
  }

  /** | ++++
   *  | ----
   */
  @Test
  def overlapSame {
    testOverlap(13, 7, 14, 3)
  }

  /** |  ++++
   *  | ------
   */
  @Test
  def overlapOverLarger {
    testOverlap(13, 7, 11, 11)
  }

  /** | ++++
   *  |   ----
   */
  @Test
  def overlapOverEnd {
    testOverlap(13, 7, 17, 5)
  }

  /** | ++++
   *  |     ----
   */
  @Test
  def overlapAfterTouching {
    testNoOverlap(13, 7, 20, 5)
  }

  /** | ++++
   *  |       ----
   */
  @Test
  def overlapAfter {
    testNoOverlap(13, 7, 22, 5)
  }

  private def testNoOverlap(offsetA: Int, lengthA: Int, offsetB: Int, lengthB: Int) {
    import RegionHelper._
    assertFalse(s"($offsetA, $lengthA) should not overlap with ($offsetB, $lengthB)", new TypedRegion(offsetA, lengthA, "A").overlapsWith(new TypedRegion(offsetB, lengthB, "B")))
  }

  private def testOverlap(offsetA: Int, lengthA: Int, offsetB: Int, lengthB: Int) {
    import RegionHelper._
    assertTrue(s"($offsetA, $lengthA) should overlap with ($offsetB, $lengthB)", new TypedRegion(offsetA, lengthA, "A").overlapsWith(new TypedRegion(offsetB, lengthB, "B")))
  }

}