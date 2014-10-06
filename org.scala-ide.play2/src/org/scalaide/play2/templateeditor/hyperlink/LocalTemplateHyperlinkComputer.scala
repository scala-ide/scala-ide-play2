package org.scalaide.play2.templateeditor.hyperlink

import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.eclipse.jface.text.IRegion
import org.scalaide.play2.templateeditor._
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions
import org.scalaide.util.ScalaWordFinder
import org.eclipse.jface.text.ITextViewer
import org.eclipse.ui.texteditor.ITextEditor
import org.scalaide.play2.templateeditor.compiler.PositionHelper
import org.eclipse.jface.text.IDocument
import org.scalaide.play2.util.StoredEditorUtils
import org.scalaide.ui.editor.SourceConfiguration
import org.eclipse.jface.text.Region

/** A hyperlink detector that only looks for local definitions.
 *
 *  Template source files are not handled properly by the Scala IDE default hyperlink detectors because they
 *  have different filenames, and `LocateSymbol` wouldn't be able to find the template compilation unit.
 *
 *  This detector only handles symbols that are defined in the *same* compilation unit.
 */
class LocalTemplateHyperlinkComputer extends AbstractHyperlinkDetector {
  final override def detectHyperlinks(viewer: ITextViewer, currentSelection: IRegion, canShowMultipleHyperlinks: Boolean): Array[IHyperlink] = {
    val doc: IDocument = viewer.getDocument()

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
            compiler.askTypeAt(pos).get match {
              case Left(tree: Tree) if localSymbol(tree.symbol) =>
                val sym = tree.symbol
                val offset = icu.lastSourceMap().originalPos(sym.pos.point)
                List(SourceConfiguration.scalaHyperlink(icu, new Region(offset, sym.decodedName.length), sym.kindString + sym.nameString, sym.nameString, wordRegion))
              case _ => Nil
            }
          } getOrElse Nil

        case None => Nil
      }
    }

    if (doc == null) null // can be null if generated through ScalaPreviewerFactory
    else {
      val fileOption = StoredEditorUtils.getFileOfViewer(viewer)
      fileOption match {
        case Some(file) => {
          val scu = new TemplateCompilationUnitProvider(false).fromFileAndDocument(file, doc)
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