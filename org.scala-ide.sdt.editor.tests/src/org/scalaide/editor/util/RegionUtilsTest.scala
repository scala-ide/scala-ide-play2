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

}