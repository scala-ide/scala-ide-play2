package org.scalaide.play2.routeeditor.completion

import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point

import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.routeeditor.lexical.HTTPKeywords
import org.scalaide.play2.util.Images

class HttpMethodCompletionComputer extends IContentAssistProcessor {

  private val wordFinder = new HttpMethodCompletionComputer.WordFinder

  override def getCompletionProposalAutoActivationCharacters(): Array[Char] = null

  override def getContextInformationAutoActivationCharacters(): Array[Char] = null

  override def getErrorMessage = null

  override def getContextInformationValidator = null

  override def computeCompletionProposals(viewer: ITextViewer, offset: Int): Array[ICompletionProposal] = {
    Option(wordFinder.findWord(viewer.getDocument(), offset)) match {
      case None => null
      case Some(region) =>
        val word = viewer.getDocument().get(region.getOffset(), region.getLength())
        val filteredCompletions = HTTPKeywords.Methods.filter(_.take(region.getLength) equalsIgnoreCase word)

        val completions = if (filteredCompletions.isEmpty) HTTPKeywords.Methods else filteredCompletions
        completions.map(new HttpMethodCompletionProposal(region, _))
    }
  }

  override def computeContextInformation(viewer: ITextViewer, offset: Int): Array[IContextInformation] = {
    null
  }

  private class HttpMethodCompletionProposal(region: IRegion, displayString: String) extends ICompletionProposal {
    override def apply(document: IDocument): Unit = {
      document.replace(region.getOffset, region.getLength, displayString)
    }

    override def getSelection(document: IDocument): Point = null
    override def getAdditionalProposalInfo: String = null
    override def getDisplayString: String = displayString
    override def getImage: Image = PlayPlugin.instance.getImageRegistry().get(Images.HTTP_METHODS_ICON)
    override def getContextInformation: IContextInformation = null
  }
}

object HttpMethodCompletionComputer {
  private class WordFinder extends DefaultTextDoubleClickStrategy {
    private def isOffsetAtEndOfLine(offset: Int, lineRegion: IRegion): Boolean =
      offset == lineRegion.getOffset() + lineRegion.getLength()

    override def findWord(document: IDocument, offset: Int): IRegion = {
      val line = try { document.getLineInformationOfOffset(offset) } catch { case _: BadLocationException => null }

      if (isOffsetAtEndOfLine(offset, line)) super.findWord(document, offset - 1) // removing 1 or no completion is shown!
      else super.findWord(document, offset)
    }
  }
}