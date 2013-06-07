package org.scalaide.play2.routeeditor

import org.scalaide.play2.routeeditor.lexical.RouteDocumentPartitioner
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.IDocument
import org.junit.Assert._
import org.eclipse.jface.text.Document
import scala.annotation.tailrec
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Region
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.StringBuilder

trait RouteTest {

  protected class RouteFile(rawText: String, markers: List[Char] = List('@')) {
    private val text = rawText.stripMargin.trim

    private val (cleanedText: String, allMarkedPositions: Map[Char, List[Int]]) = extractPositions(rawText)

    val document: IDocument = {
      val doc = new Document(cleanedText)
      val partitioner = new RouteDocumentPartitioner
      partitioner.connect(doc)
      doc.setDocumentPartitioner(partitioner)
      doc
    }

    /** Return the only marked position by a marker char.
     */
    def caretOffset(marker: Char): Int = {
      val positions = allMarkedPositions(marker)
      assertEquals("too many marked positions", 1, positions.size)
      positions(0)
    }

    /** Return the region marked by one or two marker.
     */
    def selectedRegion(marker: Char = '@'): IRegion = {
      allMarkedPositions(marker) match {
        case a :: Nil =>
          new Region(a, 0)
        case a :: b :: Nil =>
          new Region(a, b - a)
        case _ =>
          fail("Should be 1 or 2 marked position, was: " + allMarkedPositions)
          null
      }
    }
    
    /** Return the regions marked by pairs of markers
     */
    def selectedRegions(marker: Char): Seq[IRegion] = {
      @tailrec
      def selectedRegions(positions: List[Int], acc: Vector[IRegion]): Vector[IRegion] = {
        positions match {
          case Nil =>
          acc
        case a :: b :: tail =>
          selectedRegions(tail, acc :+ new Region(a, b - a))
        case _ =>
          fail(s"Could not find matching pairs of $marker markers")
          null
        }
      }
      
      selectedRegions(allMarkedPositions(marker), Vector())
    }
    
    /** Remove the markers from the raw text, and return a Map with the positions of the
     *  markers by type. 
     */
    private def extractPositions(rawText: String): (String, Map[Char, List[Int]]) = {
      def addToMap(map: Map[Char, List[Int]], marker: Char, position: Int):  Map[Char, List[Int]] = {
        val l = map(marker) :+ position
        map + ((marker, l))
      }
      
      @tailrec
      def extractPositions(chars: List[Char], currentIndex: Int, cleaned: StringBuilder, positions: Map[Char, List[Int]]): (String, Map[Char, List[Int]]) = {
        chars match {
          case Nil =>
            (cleaned.mkString, positions)
          case c :: tail if markers.contains(c)=>
            extractPositions(tail, currentIndex, cleaned, addToMap(positions, c, currentIndex))
          case c :: tail =>
            extractPositions(tail, currentIndex + 1, cleaned.append(c), positions)
        }
      }
      
      extractPositions(rawText.toList, 0, new StringBuilder(), markers.map(c => (c, Nil)).toMap)
    }
  }
  
  protected object RouteFile {
    def apply(text: String, markers: List[Char] = List('@')): RouteFile = new RouteFile(text, markers)
  }
}