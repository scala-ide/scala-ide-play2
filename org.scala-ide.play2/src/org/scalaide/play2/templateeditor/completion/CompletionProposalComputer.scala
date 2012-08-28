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
    EditorUtils.getEditorScalaInput(textEditor) match {
      case Some(tcu: TemplateCompilationUnit) =>
        // TODO: Not sure if this is the best way. Maybe compilation units should always be connected to something..
        tcu.connect(viewer.getDocument)
//        textEditor.doSave(null) // TODO it's very messy! But it is necessary to make it work
        //        val mappedOffset = tcu.mapTemplateToScalaOffset(offset)
        tcu.askReload()
        tcu.withSourceFile { findCompletions(viewer, offset, tcu) }(List[ICompletionProposal]()).toArray
      case _ => Array()
    }
  }

  private def findCompletions(viewer: ITextViewer, position: Int, tcu: TemplateCompilationUnit)(sourceFile: SourceFile, compiler: ScalaPresentationCompiler): List[ICompletionProposal] = {
    val region = ScalaWordFinder.findCompletionPoint(tcu.getTemplateContents, position)
    val mappedRegion = tcu.mapTemplateToScalaRegion(region.asInstanceOf[Region])
    val mappedPosition = tcu.mapTemplateToScalaOffset(position - 1) + 1

    val res = findCompletions(mappedRegion)(mappedPosition, tcu)(sourceFile, compiler).sortBy(_.relevance).reverse

    res.map(prop => {
      val newProp = prop.copy(startPos = prop.startPos - mappedPosition + position)

      ScalaCompletionProposal(viewer.getSelectionProvider)(newProp)
    })

  }

  def computeContextInformation(viewer: ITextViewer, offset: Int): Array[IContextInformation] = {
    null
  }
}