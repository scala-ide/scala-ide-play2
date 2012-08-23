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
import org.scalaide.play2.templateeditor.scanners.TemplatePartitions
import org.eclipse.jface.text.Region
import org.scalaide.play2.templateeditor.compiler.PositionHelper
import org.scalaide.play2.templateeditor.TemplateCompilationUnit

// FIXME lots of intersection with DeclarationHyperlinkDetector
class TemplateDeclarationHyperlinkDetector extends BaseHyperlinkDetector with HasLogger {

  private val resolver: TemplateScalaDeclarationHyperlinkComputer = new TemplateScalaDeclarationHyperlinkComputer

  override def runDetectionStrategy(scu: InteractiveCompilationUnit, textEditor: ITextEditor, currentSelection: IRegion): List[IHyperlink] = {
    val input = textEditor.getEditorInput
    val doc = textEditor.getDocumentProvider.getDocument(input)
    if (!  doc.getContentType(currentSelection.getOffset()).equals(TemplatePartitions.TEMPLATE_SCALA)) {
      return null
    }
    val wordRegion = ScalaWordFinder.findWord(doc.get, currentSelection.getOffset).asInstanceOf[Region]
    val mappedRegion = scu.asInstanceOf[TemplateCompilationUnit].mapTemplateToScalaRegion(wordRegion)

    resolver.findHyperlinks(scu, mappedRegion, wordRegion) match {
      case None => List()
      case Some(List()) =>
        // FIXME to support java links
//        scu match {
//          case scalaCU: TemplateCompilationUnit =>
//            // the following assumes too heavily a Java compilation unit, being based on the dreaded
//            // ScalaSelectionEngine. However, this is a last-resort hyperlinking that uses search for
//            // top-level types, and unless there are bugs, normal hyperlinking (through compiler symbols)
//            // would find it. So we go here only for `ScalaCompilationUnit`s.
//            javaDeclarationHyperlinkComputer(textEditor, wordRegion, scalaCU)
//          case _ =>
            Nil
//        }
      case Some(hyperlinks) =>
        hyperlinks
    }
  }

//  private def javaDeclarationHyperlinkComputer(textEditor: ITextEditor, wordRegion: IRegion, scu: TemplateCompilationUnit): List[IHyperlink] = {
//    try {
//      val environment = scu.newSearchableEnvironment()
//      val requestor = new ScalaSelectionRequestor(environment.nameLookup, null) // null is not correct
//      val engine = new ScalaSelectionEngine(environment, requestor, scu.scalaProject.javaProject.getOptions(true))
//      val offset = wordRegion.getOffset
//      engine.select(scu, offset, offset + wordRegion.getLength - 1)
//      val elements = requestor.getElements.toList
//
//      lazy val qualify = elements.length > 1
//      lazy val openAction = new OpenAction(textEditor.asInstanceOf[JavaEditor])
//      elements.map(new JavaElementHyperlink(wordRegion, openAction, _, qualify))
//    } catch {
//      case t: Throwable => 
//        logger.debug("Exception while computing hyperlink", t)
//        Nil
//    }
//  }
}

object TemplateDeclarationHyperlinkDetector {
  def apply(): BaseHyperlinkDetector = new TemplateDeclarationHyperlinkDetector
}