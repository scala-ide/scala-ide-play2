package org.scalaide.editor.util

import org.eclipse.jface.text.TypedRegion
import org.eclipse.jface.text.IRegion

object RegionHelper {
  
  implicit class RichTypedRegion(val region: TypedRegion) extends AnyVal {
    
    def shift(n: Int): TypedRegion =
        new TypedRegion(region.getOffset() + n, region.getLength(), region.getType())
    
    /** Checks if the given position is contained in this region.
     *  The check is inclusive. If this region has offset 5, and length 3, it will return
     *  true for 5, 6, 7 and 8. 
     */
    def containsPosition(offset: Int): Boolean = {
      if (region.getLength() == 0) {
        region.getOffset() == offset
      } else {
        region.getOffset() <= offset && (region.getOffset() + region.getLength()) >= offset
      }
    }
    
    /** Check if the given region is contained in this region.
     */
    def containsRegion(innerRegion: IRegion): Boolean = {
      containsPosition(innerRegion.getOffset()) && containsPosition(innerRegion.getOffset() + innerRegion.getLength())
    }
  }
  

  implicit class AdvancedTypedRegionList(val a: List[TypedRegion]) extends AnyVal {
    def U(b: List[TypedRegion]) =
      union(a, b)
    def \(b: List[TypedRegion]) =
      subtract(a, b)
    def ^(b: List[TypedRegion]) =
      intersect(a, b)

  }

  /**
   * Intersects between two lists of regions
   */
  def intersect(a: List[TypedRegion], b: List[TypedRegion]): List[TypedRegion] = {
    subtract(a, subtract(a, b))
  }

  /**
   * Subtracts a list of regions from another one 
   */
  def subtract(a: List[TypedRegion], b: List[TypedRegion]): List[TypedRegion] = {
    (a, b) match {
      case (x :: xs, y :: ys) =>
        val xStart = x.getOffset()
        val xEnd = xStart + x.getLength() - 1
        val yStart = y.getOffset()
        val yEnd = yStart + y.getLength() - 1
        if (xEnd < yStart)
          //x: ___
          //y:      +++
          x :: subtract(xs, b)
        else if (yEnd < xStart)
          //x:      ___
          //y: +++
          subtract(a, ys)
        else if (x.containsRegion(y)) { // x contains y
          //x:   -------
          //y:    +++++
          val newElem =
            if (xStart == yStart)
              //x:  -------
              //y:  ++
              Nil
            else
              //x:  -------
              //y:    ++
              List(new TypedRegion(xStart, yStart - xStart, x.getType()))
          val producedElem =
            if (xEnd == yEnd)
              //x:  -------
              //y:       ++
              Nil
            else
              //x:  -------
              //y:      ++
              List(new TypedRegion(yEnd + 1, xEnd - yEnd, x.getType()))
          newElem ::: subtract(producedElem ::: xs, ys)
        } else if (y.containsRegion(x)) { // y contains x
          //x:    -----
          //y:   +++++++
          subtract(xs, b)
        } else if (x.containsPosition(yEnd)) {
          //x:  -------
          //y: ++++
          val producedElem = new TypedRegion(yEnd + 1, xEnd - yEnd, x.getType())
          subtract(producedElem :: xs, ys)
        } else if (x.containsPosition(yStart)) {
          //x:  -------
          //y:      ++++++
          val newElem = new TypedRegion(xStart, yStart - xStart, x.getType())
          newElem :: subtract(xs, b)
        } else {
          throw new RuntimeException("Unhandled case! Impossible!")
        }
      case (xl, Nil) => xl
      case (Nil, _) => Nil
    }
  }

  /**
   * Unions two lists of regions
   * The lists must have no intersection
   */
  def union(a: List[TypedRegion], b: List[TypedRegion]): List[TypedRegion] = {
    merge[TypedRegion](a, b, ((x, y) => x.getOffset() < y.getOffset()))
  }

  private def merge[T](aList: List[T], bList: List[T], lt: (T, T) => Boolean): List[T] = bList match {
    case Nil => aList
    case _ =>
      aList match {
        case Nil => bList
        case x :: xs =>
          if (lt(x, bList.head))
            x :: merge(xs, bList, lt)
          else
            bList.head :: merge(aList, bList.tail, lt)
      }
  }
}