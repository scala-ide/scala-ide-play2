package org.scalaide.play2.util

import scala.tools.eclipse.lexical.ScalaPartitionRegion

object ScalaPartitionRegionUtils {
  case class AdvancedScalaPartitionRegionList(a: List[ScalaPartitionRegion]) {
    def U(b: List[ScalaPartitionRegion]) =
      union(a, b)
    def \(b: List[ScalaPartitionRegion]) =
      subtract(a, b)
    def ^(b: List[ScalaPartitionRegion]) =
      intersect(a, b)

  }

  implicit def advanceScalaPartitionRegionList(spr: List[ScalaPartitionRegion]) = {
    AdvancedScalaPartitionRegionList(spr)
  }

  def intersect(a: List[ScalaPartitionRegion], b: List[ScalaPartitionRegion]): List[ScalaPartitionRegion] = {
    subtract(a, subtract(a, b))
  }

  def subtract(a: List[ScalaPartitionRegion], b: List[ScalaPartitionRegion]): List[ScalaPartitionRegion] = {
    (a, b) match {
      case (x :: xs, y :: ys) =>
        if (x.end < y.start)
          //x: ___
          //y:      +++
          x :: subtract(xs, b)
        else if (y.end < x.start)
          //x:      ___
          //y: +++
          subtract(a, ys)
        else if (x.containsRange(y.start, y.end - y.start)) { // x contains y
          //x:   -------
          //y:    +++++
          val newElem =
            if (x.start == y.start)
              //x:  -------
              //y:  ++
              Nil
            else
              //x:  -------
              //y:    ++
              List(x.copy(end = y.start - 1))
          val producedElem =
            if (x.end == y.end)
              //x:  -------
              //y:       ++
              Nil
            else
              //x:  -------
              //y:      ++
              List(x.copy(start = y.end + 1))
          newElem ::: subtract(producedElem ::: xs, ys)
        } else if (y.containsRange(x.start, x.end - x.start)) { // y contains x
          //x:    -----
          //y:   +++++++
          subtract(xs, b)
        } else if (x.containsPosition(y.end)) {
          //x:  -------
          //y: ++++
          val producedElem = x.copy(start = y.end + 1)
          subtract(producedElem :: xs, ys)
        } else if (x.containsPosition(y.start)) {
          //x:  -------
          //y:      ++++++
          val newElem = x.copy(end = y.start - 1)
          newElem :: subtract(xs, b)
        } else {
          throw new RuntimeException("Unhandled case! Impossible!")
        }
      case (xl, Nil) => xl
      case (Nil, _) => Nil
    }
  }

  def union(a: List[ScalaPartitionRegion], b: List[ScalaPartitionRegion]): List[ScalaPartitionRegion] = {
    merge[ScalaPartitionRegion](a, b, ((x, y) => x.start < y.start))
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