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
import org.scalaide.editor.util.EditorHelper
import org.eclipse.wst.sse.ui.contentassist.{ICompletionProposalComputer, CompletionProposalInvocationContext}
import org.eclipse.core.runtime.IProgressMonitor
import scala.tools.eclipse.InteractiveCompilationUnit

class CompletionProposalComputer extends ScalaCompletions with IContentAssistProcessor with ICompletionProposalComputer {

  var textEditor: Option[ITextEditor] = None
  
  def this(textEditor: ITextEditor) = {
    this()
    this.textEditor = Some(textEditor)
  }
  
  /* ICompletionProposalComputer methods */
  
  def sessionStarted() : Unit = { }
  
  def computeCompletionProposals(context: CompletionProposalInvocationContext, monitor: IProgressMonitor): java.util.List[ICompletionProposal] = {
    import scala.collection.JavaConversions._
    this.computeCompletionProposals(context.getViewer(), context.getInvocationOffset()).toList
  }
  
  def computeContextInformation(context: CompletionProposalInvocationContext, monitor: IProgressMonitor): java.util.List[IContextInformation] = {
    import scala.collection.JavaConversions._
    this.computeContextInformation(context.getViewer(), context.getInvocationOffset()).toList
  }
  
  def sessionEnded(): Unit = { }
  
  /* IContentAssistProcessor methods */
  
  override def getCompletionProposalAutoActivationCharacters() = Array('.')

  override def getContextInformationAutoActivationCharacters() = Array[Char]()

  override def getErrorMessage = "No error"

  override def getContextInformationValidator = null

  override def computeCompletionProposals(viewer: ITextViewer, offset: Int): Array[ICompletionProposal] = {
    // TODO - I don't like relying on withCurrentEditor.. I'd prefer to find a better way to get the *actual* editor
    // in a way where we can be 100% positive it's the correct editor.
    val compileUnit: Option[InteractiveCompilationUnit] = textEditor match {
      case Some(editor) => EditorUtils.getEditorCompilationUnit(editor)
      case None => EditorHelper.withCurrentEditor(editor => EditorUtils.getEditorCompilationUnit(editor))
    }
    
    compileUnit match {
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

  override def computeContextInformation(viewer: ITextViewer, offset: Int): Array[IContextInformation] = {
    null
  }
}