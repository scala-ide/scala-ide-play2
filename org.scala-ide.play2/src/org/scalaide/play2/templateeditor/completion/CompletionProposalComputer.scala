package org.scalaide.play2.templateeditor.completion

import scala.tools.eclipse.ScalaPresentationCompiler
import scala.tools.eclipse.ScalaWordFinder
import scala.tools.eclipse.completion.ScalaCompletions
import scala.tools.eclipse.ui.ScalaCompletionProposal
import scala.tools.eclipse.util.EditorUtils
import scala.tools.nsc.util.SourceFile
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.ui.texteditor.ITextEditor
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.eclipse.jface.text.Region
import scala.tools.eclipse.completion.CompletionProposal

class CompletionProposalComputer(textEditor: ITextEditor) extends ScalaCompletions with IContentAssistProcessor {
  def getCompletionProposalAutoActivationCharacters() = Array('.')

  def getContextInformationAutoActivationCharacters() = Array[Char]()

  def getErrorMessage = "No error"

  def getContextInformationValidator = null

  def computeCompletionProposals(viewer: ITextViewer, offset: Int): Array[ICompletionProposal] = {
    EditorUtils.getEditorCompilationUnit(textEditor) match {
      case Some(tcu: TemplateCompilationUnit) =>
        tcu.askReload()
        tcu.withSourceFile { findCompletions(viewer, offset, tcu) }(List[ICompletionProposal]()).toArray
      case _ => Array()
    }
  }

  private def findCompletions(viewer: ITextViewer, position: Int, tcu: TemplateCompilationUnit)(sourceFile: SourceFile, compiler: ScalaPresentationCompiler): List[ICompletionProposal] = {
    val region = ScalaWordFinder.findCompletionPoint(tcu.getTemplateContents, position)
    
    val completions = {
      for {
        mappedRegion <- tcu.mapTemplateToScalaRegion(region)
        mappedPosition <- tcu.mapTemplateToScalaOffset(position - 1)
        realPosition = mappedPosition + 1
      } yield {
        findCompletions(mappedRegion)(realPosition, tcu)(sourceFile, compiler).sortBy(_.relevance).reverse map { prop =>
          val newProp = prop.copy(startPos = prop.startPos - realPosition + position)
          ScalaCompletionProposal(viewer.getSelectionProvider)(newProp)  
        }
      }
    }
    
    completions getOrElse Nil
  }

  def computeContextInformation(viewer: ITextViewer, offset: Int): Array[IContextInformation] = {
    null
  }
}