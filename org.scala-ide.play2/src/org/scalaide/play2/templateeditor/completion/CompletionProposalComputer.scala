package org.scalaide.play2.templateeditor.completion

import scala.collection.JavaConversions.seqAsJavaList
import scala.tools.nsc.util.SourceFile
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.ui.texteditor.ITextEditor
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer
import org.scalaide.core.compiler.IScalaPresentationCompiler
import org.scalaide.core.completion.ScalaCompletions
import org.eclipse.wst.sse.ui.contentassist.{ICompletionProposalComputer, CompletionProposalInvocationContext}
import org.eclipse.core.runtime.IProgressMonitor
import org.scalaide.play2.templateeditor.AbstractTemplateEditor
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.scalaide.play2.templateeditor.TemplateCompilationUnitProvider
import org.scalaide.ui.completion.ScalaCompletionProposal
import org.scalaide.util.ScalaWordFinder
import org.scalaide.play2.util.StoredEditorUtils

class CompletionProposalComputer extends ScalaCompletions with IContentAssistProcessor with ICompletionProposalComputer {

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
    val editorFilter: PartialFunction[ITextEditor, AbstractTemplateEditor] = { case e: AbstractTemplateEditor => e }

    val editor = StoredEditorUtils.getEditorOfViewer(viewer)

    val compileUnit: Option[TemplateCompilationUnit] = editor map { templateEditor =>
      templateEditor.compilationUnitProvider match {
        case tcup: TemplateCompilationUnitProvider => new TemplateCompilationUnitProvider(usesInclusiveDot = true).fromEditor(templateEditor)
        case _ => null // null will be caught by the catch-all case in the compileUnit match
      }
    }

    compileUnit match {
      case Some(tcu) => {
        tcu.initialReconcile()
        tcu.scheduleReconcile(tcu.getContents)
        val completions = findCompletions(viewer, offset, tcu)
        completions.toArray
      }
      case _ => Array()
    }
  }

  private def findCompletions(viewer: ITextViewer, position: Int, tcu: TemplateCompilationUnit): List[ICompletionProposal] = {
    val region = ScalaWordFinder.findCompletionPoint(tcu.getTemplateContents, position)

    val completions = {
      for {
        mappedRegion <- tcu.mapTemplateToScalaRegion(region)
        mappedPosition = tcu.lastSourceMap.scalaPos(position - 1)
        realPosition = mappedPosition + 1
      } yield {
        // `realPosition` is only valid if completing on a non-zero length name
        val actualPosition = if (region.getLength() == 0) mappedRegion.getOffset() else realPosition
        findCompletions(mappedRegion, realPosition, tcu).sortBy(prop => -(prop.relevance)) map { prop =>
          val newProp = prop.copy(startPos = prop.startPos - actualPosition + position)
          ScalaCompletionProposal(newProp)
        }
      }
    }

    completions getOrElse Nil
  }

  override def computeContextInformation(viewer: ITextViewer, offset: Int): Array[IContextInformation] = {
    null
  }
}