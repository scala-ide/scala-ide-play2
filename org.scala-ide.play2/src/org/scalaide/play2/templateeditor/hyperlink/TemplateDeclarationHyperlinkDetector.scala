package org.scalaide.play2.templateeditor.hyperlink

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlink
import org.eclipse.jdt.ui.actions.OpenAction
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.eclipse.ui.texteditor.ITextEditor
import scala.tools.eclipse.ScalaWordFinder
import scala.tools.eclipse.javaelements.ScalaCompilationUnit
import scala.tools.eclipse.javaelements.ScalaSelectionEngine
import scala.tools.eclipse.javaelements.ScalaSelectionRequestor
import scala.tools.eclipse.logging.HasLogger
import scala.tools.eclipse.InteractiveCompilationUnit
import org.eclipse.jdt.internal.core.JavaProject
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner
import scala.tools.eclipse.hyperlink.text.detector.BaseHyperlinkDetector
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions
import org.eclipse.jface.text.Region
import org.scalaide.play2.templateeditor.compiler.PositionHelper
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import scala.tools.eclipse.hyperlink.text.detector.DeclarationHyperlinkDetector
import scala.tools.eclipse.util.Utils


class TemplateDeclarationHyperlinkDetector extends DeclarationHyperlinkDetector {

  override def runDetectionStrategy(icu: InteractiveCompilationUnit, textEditor: ITextEditor, currentSelection: IRegion): List[IHyperlink] = {
    val input = textEditor.getEditorInput
    val doc = textEditor.getDocumentProvider.getDocument(input)
    if (doc.getPartition(currentSelection.getOffset()).getType() != TemplatePartitions.TEMPLATE_SCALA) {
      return Nil // should not be null, if it was null, it would throw an exception
    }
    if (doc.getChar(currentSelection.getOffset()) == '.') // otherwise it will generate an error
      return Nil
    val wordRegion = ScalaWordFinder.findWord(doc.get, currentSelection.getOffset)
    
    import Utils.any2optionable
    val tu = icu.asInstanceOfOpt[TemplateCompilationUnit]

    tu.flatMap(_.mapTemplateToScalaRegion(wordRegion)) match {
      case Some(mappedRegion) => super.findHyperlinks(textEditor, icu, wordRegion, mappedRegion)
      case None => Nil
    }
  }

}

object TemplateDeclarationHyperlinkDetector {
  def apply(): BaseHyperlinkDetector = new TemplateDeclarationHyperlinkDetector
}