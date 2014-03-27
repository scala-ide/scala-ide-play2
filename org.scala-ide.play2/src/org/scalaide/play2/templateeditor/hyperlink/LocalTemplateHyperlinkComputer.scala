package org.scalaide.play2.templateeditor.hyperlink

import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector
import scala.tools.eclipse.util.EditorUtils
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.eclipse.jface.text.IRegion
import org.scalaide.play2.templateeditor._
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions
import scala.tools.eclipse.ScalaWordFinder
import scala.tools.eclipse.hyperlink.text.Hyperlink
import org.eclipse.jface.text.ITextViewer
import org.eclipse.ui.texteditor.ITextEditor
import org.scalaide.play2.templateeditor.compiler.PositionHelper
import org.scalaide.editor.util.EditorHelper
import org.eclipse.jface.text.IDocument

/** A hyperlink detector that only looks for local definitions.
 *
 *  Template source files are not handled properly by the Scala IDE default hyperlink detectors because they
 *  have different filenames, and `LocateSymbol` wouldn't be able to find the template compilation unit.
 *
 *  This detector only handles symbols that are defined in the *same* compilation unit.
 */
class LocalTemplateHyperlinkComputer extends AbstractHyperlinkDetector {
  final override def detectHyperlinks(viewer: ITextViewer, currentSelection: IRegion, canShowMultipleHyperlinks: Boolean): Array[IHyperlink] = {
    detectHyperlinks(viewer.getDocument(), currentSelection, canShowMultipleHyperlinks)
  }

  final def detectHyperlinks(doc: IDocument, currentSelection: IRegion, canShowMultipleHyperlinks: Boolean): Array[IHyperlink] = {
    def findHyperlinks(icu: TemplateCompilationUnit): List[IHyperlink] = {
      if (doc.getPartition(currentSelection.getOffset()).getType() != TemplatePartitions.TEMPLATE_SCALA) {
        return Nil // should not be null, if it was null, it would throw an exception
      }
      if (doc.getChar(currentSelection.getOffset()) == '.') // otherwise it will generate an error
        return Nil

      val wordRegion = ScalaWordFinder.findWord(doc.get, currentSelection.getOffset).asInstanceOf[IRegion]
      icu.mapTemplateToScalaRegion(wordRegion) match {
        case Some(mappedRegion) =>
          icu.withSourceFile { (source, compiler) =>
            import compiler._
            def localSymbol(sym: compiler.Symbol): Boolean = {
              (sym ne null) &&
              (sym ne NoSymbol) &&
              sym.pos.isDefined &&
              sym.pos.source == source
            }

            val pos = compiler.rangePos(source, mappedRegion.getOffset(), mappedRegion.getOffset(), mappedRegion.getOffset() + mappedRegion.getLength())
            val response = new Response[Tree]
            compiler.askTypeAt(pos, response)
            response.get match {
              case Left(tree: Tree) if localSymbol(tree.symbol) =>
                val sym = tree.symbol
                icu.templateOffset(sym.pos.startOrPoint) match {
                  case Some(offset) => 
                    val hyper = Hyperlink.withText(sym.name.toString)(icu, offset, sym.name.length, sym.kindString + sym.nameString, wordRegion)
                    List(hyper)
                  case None => 
                    Nil
                }
              case _ => Nil
            }
          }(Nil)

        case None => Nil
      }
    }

    if (doc == null) null // can be null if generated through ScalaPreviewerFactory
    else {
      val fileOption = EditorHelper.findFileOfDocument(doc)
      fileOption match {
        case Some(file) => {
          val scu = TemplateCompilationUnitProvider(false).fromFileAndDocument(file, doc)
          findHyperlinks(scu) match {
            // I know you will be tempted to remove this, but don't do it, JDT expects null when no hyperlinks are found.
            case Nil => null
            case links =>
              if (canShowMultipleHyperlinks) links.toArray
              else Array(links.head)
          }
        }
        case _ => null
      }
    }
  }
}