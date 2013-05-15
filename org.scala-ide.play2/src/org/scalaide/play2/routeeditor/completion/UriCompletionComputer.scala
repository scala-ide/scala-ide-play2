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
import org.scalaide.play2.routeeditor.RouteUri
import org.scalaide.play2.routeeditor.RouteUriWithRegion

class UriCompletionComputer extends IContentAssistProcessor {

  override def getCompletionProposalAutoActivationCharacters(): Array[Char] =
    Array('/')

  override def getContextInformationAutoActivationCharacters(): Array[Char] = null

  override def getErrorMessage = null

  override def getContextInformationValidator = null

  override def computeCompletionProposals(viewer: ITextViewer, offset: Int): Array[ICompletionProposal] = {
    val document = viewer.getDocument()

    val word = WordFinder.findWord(document, offset)
    // only consider the prefix (word to the left of caret)
    val rawUri = document.get(word.getOffset, (offset - word.getOffset()))
    val uri = RouteUri(rawUri)

    val existingUris = RouteUriWithRegion.existingUrisInDocument(document).filter(_ != uri)

    // If the `rawUri` is empty then add '/' to the returned set of proposals 
    val defaultUriProposal = if (rawUri.isEmpty) Set(uri) else Set.empty 
    
    val staticUrisProposals = {
      if (RouteUri.isValid(rawUri)) existingUris.flatMap(_.subUrisStartingWith(rawUri))
      // If the `rawUri` isn't valid, then proposals will contain all possible valid permutations of existing URIs
      else existingUris.flatMap(_.subUrisStartingWith(""))
    }

    val dynamicUrisProposals = {
      // Add dynamics parts to the proposals only if completion happens on an empty URI or after a slash 
      if (rawUri.isEmpty || rawUri.last == '/') RouteUri(rawUri).dynamicUris
      else staticUrisProposals
    }
 
    val allUrisProposals = defaultUriProposal ++ staticUrisProposals ++ dynamicUrisProposals

    val sortedProposals = allUrisProposals.toList.sorted(RouteUri.AlphabeticOrder)
    
    sortedProposals.map(new UriCompletionProposal(word, _)).toArray
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
    override def getImage: Image = PlayPlugin.instance.getImageRegistry().get(Images.URL_ICON)
    override def getContextInformation: IContextInformation = null
  }
}

