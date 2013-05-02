package org.scalaide.play2.routeeditor.completion

import scala.tools.eclipse.util.Utils.any2optionable

import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point
import org.scalaide.editor.WordFinder
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.routeeditor.lexical.RouteDocumentPartitioner
import org.scalaide.play2.util.Images

class UriCompletionComputer extends IContentAssistProcessor {
  import UriCompletionComputer.RouteUri

  private val wordFinder = new WordFinder

  override def getCompletionProposalAutoActivationCharacters(): Array[Char] = null

  override def getContextInformationAutoActivationCharacters(): Array[Char] = null

  override def getErrorMessage = null

  override def getContextInformationValidator = null

  override def computeCompletionProposals(viewer: ITextViewer, offset: Int): Array[ICompletionProposal] = {
    val document = viewer.getDocument()

    val word = wordFinder.findWord(document, offset)
    val rawUri = document.get(word.getOffset, word.getLength)
    val uri = RouteUri(rawUri)

    val existingUris = existingUrisInDocument(document) - uri

    // If the `rawUri` is empty then add '/' to the returned set of proposals 
    val defaultUriProposal = if (rawUri.isEmpty) Set(uri) else Set.empty 
    
    val staticUrisProposals = {
      if (RouteUri.isValid(rawUri)) existingUris.flatMap(_.permutationForPrefix(rawUri))
      // If the `rawUri` isn't valid, then proposals will contain all possible valid permutations of existing URIs
      else existingUris.flatMap(_.permutationForPrefix(""))
    }

    val dynamicUrisProposals = {
      // Add dynamics parts to the proposals only if completion happens on an empty URI or after a slash 
      if (rawUri.isEmpty || rawUri.last == '/') RouteUri(rawUri).dynamicUris
      else staticUrisProposals
    }
 
    val allUrisProposals = defaultUriProposal ++ staticUrisProposals ++ dynamicUrisProposals

    // If `offset` is in the middle of a URI part, it is possible that the set of `allUrisProposals` is empty. If that happens, returns the set of `existingUris`
    val finalProposals = if (allUrisProposals.isEmpty) existingUris else allUrisProposals
    val sortedProposals = finalProposals.toList.sorted(RouteUri.AlphabeticOrder)
    
    sortedProposals.map(new UriCompletionProposal(word, _)).toArray
  }

  /** Return all the existing URIs for the passed `document`. */
  private def existingUrisInDocument(document: IDocument): Set[RouteUri] = {
    (for {
      partitioner <- document.getDocumentPartitioner().asInstanceOfOpt[RouteDocumentPartitioner].toList
      partition <- partitioner.uriPartitions
      length = Math.max(0, partition.getLength)
      if length > 0
      rawUri = document.get(partition.getOffset, length)
    } yield RouteUri(rawUri))(collection.breakOut)
  }

  override def computeContextInformation(viewer: ITextViewer, offset: Int): Array[IContextInformation] = {
    null
  }

  private class UriCompletionProposal(region: IRegion, uri: RouteUri) extends ICompletionProposal {
    override def apply(document: IDocument): Unit = {
      document.replace(region.getOffset, region.getLength, getDisplayString)
    }

    override def getSelection(document: IDocument): Point =
      new Point(region.getOffset() + getDisplayString.length, 0) // always put caret *after* the inserted completion
    override def getAdditionalProposalInfo: String = null
    override def getDisplayString: String = uri.toString
    override def getImage: Image = PlayPlugin.instance.getImageRegistry().get(Images.HTTP_METHODS_ICON)
    override def getContextInformation: IContextInformation = null
  }
}

object UriCompletionComputer {
  case class RouteUri private (parts: List[String]) {
    def startsWith(prefix: String): Boolean = {
      val uriPrefix = RouteUri(prefix)
      toString().startsWith(uriPrefix.toString)
    }

    def permutationForPrefix(prefix: String): List[RouteUri] = {
      if (startsWith(prefix)) {
        val splitPoint = Math.max(0, RouteUri(prefix).parts.length - 1)
        val (common, additional) = parts.splitAt(splitPoint)
        (for (i <- 1 to additional.size) 
          yield RouteUri(common ::: additional.slice(0, i))
        )(collection.breakOut)
      }
      else Nil
    }
    def append(part: String): RouteUri = RouteUri(parts :+ part)

    def dynamicUris: List[RouteUri] = List(":", "*", "$") map (append(_))

    override def toString(): String = parts.mkString("/", "/", "")
  }

  object RouteUri {
    def apply(uri: String): RouteUri = RouteUri(split(uri))

    def isValid(rawUri: String): Boolean = rawUri.startsWith("/")

    private def split(uri: String): List[String] = {
      val parts = uri.split("/").toList
      parts.filterNot(_.trim.isEmpty)
    }

    implicit object AlphabeticOrder extends Ordering[RouteUri] {
      override def compare(x: RouteUri, y: RouteUri): Int =
        x.toString.compare(y.toString)
    }
  }
}
