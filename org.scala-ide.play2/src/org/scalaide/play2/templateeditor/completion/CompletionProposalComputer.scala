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
import play.templates.ScalaTemplateParser
import org.scalaide.play2.templateeditor.AbstractTemplateEditor
import org.scalaide.play2.templateeditor.TemplateEditor
import org.scalaide.play2.templateeditor.TemplateCompilationUnitProvider

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
    val editorFilter: PartialFunction[ITextEditor, AbstractTemplateEditor] = { case e: AbstractTemplateEditor => e }
    val editor = (textEditor collect editorFilter) orElse (EditorHelper.findEditorOfDocument(viewer.getDocument()) collect editorFilter)

    val compileUnit: Option[TemplateCompilationUnit] = editor map { templateEditor =>
      templateEditor.compilationUnitProvider match {
        case tcup: TemplateCompilationUnitProvider => tcup.copy(usesInclusiveDot = true).fromEditor(templateEditor)
        case _ => null // null will be caught by the catch-all case in the compileUnit match
      }
    }
    
    compileUnit match {
      case Some(tcu) => {
        // FIXME: askReload doesn't (always) trigger a recompile
        tcu.askReload()
        val completions = tcu.withSourceFile { findCompletions(viewer, offset, tcu) } getOrElse Nil
        completions.toArray
      }
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
        // `realPosition` is only valid if completing on a non-zero length name
        val actualPosition = if (region.getLength() == 0) mappedRegion.getOffset() else realPosition 
        findCompletions(mappedRegion)(realPosition, tcu)(sourceFile, compiler).sortBy(prop => -(prop.relevance)) map { prop =>
          val newProp = prop.copy(startPos = prop.startPos - actualPosition + position)
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