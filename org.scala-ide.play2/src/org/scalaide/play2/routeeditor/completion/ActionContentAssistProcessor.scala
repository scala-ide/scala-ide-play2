package org.scalaide.play2.routeeditor.completion

import scala.tools.eclipse.ScalaProject
import scala.tools.eclipse.logging.HasLogger

import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.Region
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.jface.text.contentassist.IContextInformation
import org.scalaide.editor.WordFinder
import org.scalaide.play2.routeeditor.HasScalaProject
import org.scalaide.play2.routeeditor.completion.action.ActionCompletionComputer

class ActionContentAssistProcessor(routeEditor: HasScalaProject) extends IContentAssistProcessor with HasLogger {

  override def getCompletionProposalAutoActivationCharacters(): Array[Char] = Array('.')

  override def getContextInformationAutoActivationCharacters(): Array[Char] = null

  override def getErrorMessage = null

  override def getContextInformationValidator = null

  override def computeCompletionProposals(viewer: ITextViewer, offset: Int): Array[ICompletionProposal] = {
    val proposals = {
      for (project <- routeEditor.getScalaProject)
        yield computeCompletionProposals(project, viewer, offset)
    }
    proposals getOrElse null
  }

  private def computeCompletionProposals(project: ScalaProject, viewer: ITextViewer, offset: Int): Array[ICompletionProposal] = {
    val document = viewer.getDocument

    val completions = project.withPresentationCompiler { compiler =>
      compiler.askOption { () =>
        val computer = new ActionCompletionComputer(compiler)
        computer.computeCompletionProposals(document, offset)
      } getOrElse Nil
    }()

    completions.sorted.toArray
  }

  override def computeContextInformation(viewer: ITextViewer, offset: Int): Array[IContextInformation] = {
    null
  }
}