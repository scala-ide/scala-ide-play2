package org.scalaide.play2.routeeditor.completion

import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point
import org.scalaide.ui.editor.WordFinder
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.routeeditor.lexical.HTTPKeywords
import org.scalaide.play2.util.Images

class HttpMethodCompletionComputer extends IContentAssistProcessor {

  override def getCompletionProposalAutoActivationCharacters(): Array[Char] = null

  override def getContextInformationAutoActivationCharacters(): Array[Char] = null

  override def getErrorMessage = null

  override def getContextInformationValidator = null

  override def computeCompletionProposals(viewer: ITextViewer, offset: Int): Array[ICompletionProposal] = {
    val region = WordFinder.findWord(viewer.getDocument(), offset)
    val input = viewer.getDocument().get(region.getOffset(), region.getLength()).toUpperCase

    val filteredCompletions = HTTPKeywords.Methods.filter(_.take(region.getLength) == input)
    val completions = {
      // If the `input` matches one of the valid HTTP methods, return full list of alternatives as the 
      // user likely wants to change the current valid input with another one.   
      if (HTTPKeywords.Methods.contains(input) || filteredCompletions.isEmpty) HTTPKeywords.Methods
      else filteredCompletions
    }

    completions.map(new HttpMethodCompletionProposal(region, _))
  }

  override def computeContextInformation(viewer: ITextViewer, offset: Int): Array[IContextInformation] = {
    null
  }

  private class HttpMethodCompletionProposal(region: IRegion, displayString: String) extends ICompletionProposal {
    override def apply(document: IDocument): Unit = {
      document.replace(region.getOffset, region.getLength, displayString)
    }

    override def getSelection(document: IDocument): Point = 
      new Point(region.getOffset() + displayString.length, 0) // always put caret *after* the inserted completion
    override def getAdditionalProposalInfo: String = null
    override def getDisplayString: String = displayString
    override def getImage: Image = PlayPlugin.instance.getImageRegistry().get(Images.HTTP_METHODS_ICON)
    override def getContextInformation: IContextInformation = null
  }
}