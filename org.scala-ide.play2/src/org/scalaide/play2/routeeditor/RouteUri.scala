package org.scalaide.play2.routeeditor

import org.eclipse.jface.text.IDocument
import scala.tools.eclipse.util.Utils.any2optionable
import org.scalaide.play2.routeeditor.lexical.RouteDocumentPartitioner
import org.eclipse.jface.text.IRegion
import scala.annotation.tailrec

/** Class representing a URI in a route configuration file.
 */
case class RouteUri protected (parts: List[String]) {
  def startsWith(prefix: String): Boolean = {
    val uriPrefix = RouteUri(prefix)
    toString().startsWith(uriPrefix.toString)
  }

  def startsWith(prefix: List[String]): Boolean = {
    parts.startsWith(prefix)
  }

  def subUrisStartingWith(prefix: String): List[RouteUri] = {
    if (startsWith(prefix)) {
      val splitPoint = Math.max(0, RouteUri(prefix).parts.length - 1)
      val (common, additional) = parts.splitAt(splitPoint)
      (for (i <- 1 to additional.size)
        yield RouteUri(common ::: additional.slice(0, i)))(collection.breakOut)
    } else Nil
  }
  def append(part: String): RouteUri = RouteUri(parts :+ part)

  def dynamicUris: List[RouteUri] = List(":", "*", "$") map (append(_))

  /** Returns the parts of the URI which are in contact with the give range, the touched parts, and the parts before them, the prefix.
   *  The content of the returned tuple is: (prefix parts, touched parts). 
   */
  def partsTouchedBy(offset: Int, length: Int): (List[String], List[String]) = {
    @tailrec
    def computePrefixExclusive(parts: List[String], offset: Int, acc: List[String]): List[String] = {
      parts match {
        // offset is after the first part, so the first part is in the prefix
        case head :: tail if (offset > head.length()) =>
          computePrefixExclusive(tail, offset - head.length - 1, acc :+ head)
        case _ =>
          acc
      }
    }

    @tailrec
    def computePrefixInclusive(parts: List[String], offset: Int, acc: List[String]): List[String] = {
      if (offset < 1) {
        acc
      } else {
        parts match {
          // offset is after the first part, so the first part is in the prefix
          case head :: tail =>
            computePrefixInclusive(tail, offset - head.length - 1, acc :+ head)
          case Nil =>
            acc
        }
      }
    }
    
    // if it is just a caret position, search one character before, to manage the case
    // when the caret is before a slash. It should select the part before it
    val adjustedOffset = if (length == 0) offset - 1 else offset
    val prefix = computePrefixExclusive(parts, adjustedOffset, Nil)
    
    val endOffsetInRemainder = offset + length - prefix.map(_.length + 1).sum
    val touchedParts = computePrefixInclusive(parts.drop(prefix.size), endOffsetInRemainder, Nil)

    (prefix, touchedParts)
  }

  override def toString(): String = parts.mkString("/", "/", "")
}

object RouteUri {
  def apply(uri: String): RouteUri = RouteUri(split(uri))

  def isValid(rawUri: String): Boolean = rawUri.startsWith("/")

  private[routeeditor] def split(uri: String): List[String] = {
    val parts = uri.split("/").toList
    parts.filterNot(_.trim.isEmpty)
  }

  implicit object AlphabeticOrder extends Ordering[RouteUri] {
    override def compare(x: RouteUri, y: RouteUri): Int =
      x.toString.compare(y.toString)
  }
}

/** Class representing a URI in a route configuration file, with information about its location in the original file.
 */
class RouteUriWithRegion private (parts: List[String], val region: IRegion) extends RouteUri(parts)

object RouteUriWithRegion {
  def apply(uri: String, region: IRegion) = new RouteUriWithRegion(RouteUri.split(uri), region)

  /** Returns all the existing URIs for the passed `document`. */
  def existingUrisInDocument(document: IDocument): Set[RouteUri] = {
    allUrisInDocument(document).toSet
  }
  
  /** Returns all the regions with URI in the passed `document`. */
  def allUrisInDocument(document: IDocument): List[RouteUriWithRegion] = {
    (for {
      partitioner <- document.getDocumentPartitioner().asInstanceOfOpt[RouteDocumentPartitioner].toList
      partition <- partitioner.uriPartitions
      length = Math.max(0, partition.getLength)
      if length > 0
      rawUri = document.get(partition.getOffset, length)
    } yield RouteUriWithRegion(rawUri, partition))
  }

}
