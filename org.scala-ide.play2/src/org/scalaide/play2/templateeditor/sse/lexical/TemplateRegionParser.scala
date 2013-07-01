package org.scalaide.play2.templateeditor.sse.lexical

import java.io.Reader
import java.io.StringReader

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer
import scala.tools.eclipse.lexical.ScalaCodeScanner
import scala.tools.eclipse.logging.HasLogger

import org.eclipse.jface.text.IDocument
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.wst.sse.core.internal.ltk.parser.BlockMarker
import org.eclipse.wst.sse.core.internal.ltk.parser.RegionParser
import org.eclipse.wst.sse.core.internal.parser.ContextRegion
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.sse.core.internal.text.BasicStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.text.TextRegionListImpl
import org.eclipse.wst.xml.core.internal.parser.XMLSourceParser
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeEqualsRegion
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeNameRegion
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeValueRegion
import org.eclipse.wst.xml.core.internal.parser.regions.EmptyTagCloseRegion
import org.eclipse.wst.xml.core.internal.parser.regions.EndTagOpenRegion
import org.eclipse.wst.xml.core.internal.parser.regions.TagCloseRegion
import org.eclipse.wst.xml.core.internal.parser.regions.TagNameRegion
import org.eclipse.wst.xml.core.internal.parser.regions.TagOpenRegion
import org.eclipse.wst.xml.core.internal.parser.regions.WhiteSpaceOnlyRegion
import org.eclipse.wst.xml.core.internal.parser.regions.XMLCDataTextRegion
import org.eclipse.wst.xml.core.internal.parser.regions.XMLContentRegion
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses
import org.scalaide.play2.templateeditor.lexical.TemplatePartitionTokeniser
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions

import scalariform.ScalaVersions

object TemplateDocumentRegions {
  val SCALA_DOC_REGION = "SCALA_CONTENT"
  val COMMENT_DOC_REGION = "TEMPLATE_COMMENT"
}

class TemplateRegionParser extends RegionParser with HasLogger {
  
  private class LazyCache[T](value: => T) {
    
    private var cache: Option[T] = None

    def apply(): T = cache.getOrElse {
      cache = Some(value)
      cache.get
    }
    
    def reset(): Unit = cache = None
  }
  
  private var contents: String = ""
  private val cachedRegions = new LazyCache(computeRegions(contents))
  
  /**
   * RegionParser interface methods
   */
  override def newInstance() = new TemplateRegionParser

  override def getDocumentRegions() = cachedRegions().head

  /* Get the full list of known regions */
  override def getRegions(): java.util.List[ITextRegion] = {
    import scala.collection.JavaConversions._
    val resultList = new java.util.ArrayList[ITextRegion]()
    cachedRegions().foreach(dr => {
      for (textRegion: ITextRegion <- dr.getRegions().toArray) {
        resultList.add(textRegion)
      }
    })
    resultList
  }
  
  override def reset(input: String) =
    reset(new StringReader(input))
  
  override def reset(input: String, offset: Int) =
    reset(new StringReader(input), offset)
  
  override def reset(reader: Reader) = reset(reader, 0)

  override def reset(reader: Reader, offset: Int) = {
    var c = reader.read()
    contents = ""
    while (c != -1) {
      contents = contents + c.toChar
      c = reader.read()
    }
    cachedRegions.reset()
  }
  
  def computeRegions(codeString: String): Array[IStructuredDocumentRegion] = {
    @tailrec
    def getDocumentRegionAtOffset(headRegion: IStructuredDocumentRegion, offset: Int): Option[IStructuredDocumentRegion] = {
      if (headRegion == null) None
      else {
        if (headRegion.containsOffset(offset))
          Some(headRegion)
        else
          getDocumentRegionAtOffset(headRegion.getNext(), offset)
      }
    }
    
    
    def getNumberOfDocumentRegions(headRegion: IStructuredDocumentRegion) = {
      @tailrec
      def aux(region: IStructuredDocumentRegion, count: Int): Int = {
        if (region == null) count
        else aux(region.getNext(), count + 1)
      }
      aux(headRegion, 0)
    }

    def copyXMLTextRegion(region: ITextRegion): ITextRegion = region match {
      case attribEquals: AttributeEqualsRegion => new AttributeEqualsRegion(attribEquals.getStart(), attribEquals.getTextLength(), attribEquals.getLength())
      case attribName: AttributeNameRegion => new AttributeNameRegion(attribName.getStart(), attribName.getTextLength(), attribName.getLength())
      case attribValue: AttributeValueRegion => new AttributeValueRegion(attribValue.getStart(), attribValue.getTextLength(), attribValue.getLength())
      case emptyTagClose: EmptyTagCloseRegion => new EmptyTagCloseRegion(emptyTagClose.getStart(), emptyTagClose.getTextLength(), emptyTagClose.getLength())
      case endTagOpen: EndTagOpenRegion => new EndTagOpenRegion(endTagOpen.getStart(), endTagOpen.getTextLength(), endTagOpen.getLength())
      case tagClose: TagCloseRegion => new TagCloseRegion(tagClose.getStart())
      case tagName: TagNameRegion => new TagNameRegion(tagName.getStart(), tagName.getTextLength(), tagName.getLength())
      case tagOpen: TagOpenRegion => new TagOpenRegion(tagOpen.getStart(), tagOpen.getTextLength(), tagOpen.getLength())
      case whitespace: WhiteSpaceOnlyRegion => new WhiteSpaceOnlyRegion(whitespace.getStart(), whitespace.getLength())
      case cdata: XMLCDataTextRegion => new XMLCDataTextRegion(cdata.getStart(), cdata.getTextLength(), cdata.getLength())
      case content: XMLContentRegion => new XMLContentRegion(content.getStart(), content.getLength())
      case context: ContextRegion => new ContextRegion(context.getType(), context.getStart(), context.getTextLength(), context.getLength())
      case _ => {
        logger.error(s"TemplateRegionParser: Unhandled attempt to copy XML region: $region")
        null
      }
    }
    
    // The block regions enable javascript and css support, as BLOCK_TEXT regions are treated special by the html component
    import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext
    import org.eclipse.wst.sse.core.internal.ltk.parser.BlockMarker
    val htmlParser = new XMLSourceParser
    htmlParser.addBlockMarker(new BlockMarker("script", null, DOMRegionContext.BLOCK_TEXT, false))
    htmlParser.addBlockMarker(new BlockMarker("style", null, DOMRegionContext.BLOCK_TEXT, false))
    htmlParser.reset(codeString)
    val htmlDocumentRegions = htmlParser.getDocumentRegions()
    var resultRegions = htmlDocumentRegions
    
    // tokenise, merge '@' tokens with scala code tokens, and then merge any adjacent tokens of the same type.
    // Merging here has the affect of not having neighbouring document regions of the same type
    val tokens = TemplatePartitionTokeniser.tokenise(codeString)
    val mergedTokens = PartitionHelpers.mergeAdjacentWithSameType(PartitionHelpers.combineMagicAt(tokens, codeString)).toArray
    for (token <- mergedTokens) {
      val representsHTML =
        (token.getType() == TemplatePartitions.TEMPLATE_PLAIN ||
         token.getType() == TemplatePartitions.TEMPLATE_TAG   ||
         (token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && !PartitionHelpers.isBrace(token, codeString)))
      if (!representsHTML) {
        val (templateTextRegions: Seq[ScalaTextRegion], scalaDocType: String) = {
          if (token.getType == TemplatePartitions.TEMPLATE_SCALA) {
            // I can probably do this is a smarter way by just checking if the string starts with an @, and if so
            // add the appropriate text region for the magic at, and then the rest of token becomes a normal
            // scala code text region. I don't believe there would ever be a case where an @ in the middle of the token,
            // but I really must verify that assumption before making the change.
            val scalaCode = codeString.substring(token.getOffset(), token.getOffset() + token.getLength())
            val regions = TemplatePartitionTokeniser.tokenise(scalaCode).map(t => {
              val baseOffset = token.getOffset() + t.getOffset()
              if (PartitionHelpers.isMagicAt(t, scalaCode)) {
                List(new ScalaTextRegion(TemplateSyntaxClasses.MAGIC_AT, t.getOffset(), t.getLength(), t.getLength()))
              } // actual scala code
              else { //if (t.getType() == TemplatePartitions.TEMPLATE_SCALA) {
                // TODO - figure out a good way to get the prefstore from the editor
                val prefStore = new ChainedPreferenceStore(Array((EditorsUI.getPreferenceStore()), PlayPlugin.preferenceStore))
                val scanner = new ScalaCodeScanner(prefStore, ScalaVersions.Scala_2_10)
                val dummyDoc: IDocument = new org.eclipse.jface.text.Document(codeString)
                val tokens = scanner.tokenize(dummyDoc, baseOffset, t.getLength())
                tokens.map(v => { new ScalaTextRegion(v.syntaxClass, v.start - token.getOffset(), v.length, v.length) })
              }
            }).flatten
            (regions, TemplateDocumentRegions.SCALA_DOC_REGION)
          }
          else if (PartitionHelpers.isBrace(token, codeString))
            (List(new ScalaTextRegion(TemplateSyntaxClasses.BRACE, 0, token.getLength(), token.getLength())), TemplateDocumentRegions.SCALA_DOC_REGION)
          else if (token.getType == TemplatePartitions.TEMPLATE_COMMENT)
            (List(new ScalaTextRegion(TemplateSyntaxClasses.COMMENT, 0, token.getLength(), token.getLength())), TemplateDocumentRegions.COMMENT_DOC_REGION)
          else (List(), "UNDEFINED")
        }
        
        if (templateTextRegions.isEmpty == false) {
          // absolute offsets
          val (startEffectedOffset, endEffectedOffset) = (token.getOffset(), token.getOffset() + token.getLength())
          val startEffectedDocumentRegionOpt = getDocumentRegionAtOffset(htmlDocumentRegions, startEffectedOffset)
          val endEffectedDocumentRegionOpt = getDocumentRegionAtOffset(htmlDocumentRegions, endEffectedOffset - 1)
          
          (startEffectedDocumentRegionOpt, endEffectedDocumentRegionOpt) match {
            case (Some(startEffectedDocumentRegion), Some(endEffectedDocumentRegion)) => {
              // If the scala text regions spans exactly the same space as the document regions it overlaps
              // Then replace those document regions with our own document regions
              if (startEffectedDocumentRegion.getStart() == startEffectedOffset && endEffectedDocumentRegion.getEnd() == endEffectedOffset) {
                val region = new BasicStructuredDocumentRegion { override def getType() = scalaDocType }
                region.setStart(startEffectedOffset)
                region.setLength(token.getLength())
                templateTextRegions.foreach(region.addRegion(_))
                
                if (getNumberOfDocumentRegions(htmlDocumentRegions) == 1) {
                  resultRegions = region
                }
                else {
                  // insert our new region where the effected regions were
                  Option(startEffectedDocumentRegion.getPrevious()).foreach { previous =>
                    previous.setNext(region)
                    region.setPrevious(previous)
                  }
                  Option(endEffectedDocumentRegion.getNext()) match {
                    case Some(next) => {
                      next.setPrevious(region)
                      region.setNext(next)
                    }
                    case None => region.setEnded(true)
                  }
                }
              }
              else {
                // handle the case where we need to trim text regions and then insert our text regions
                // Note: this code does not try to handle scala code that spans two (or more) xml document regions
                if (startEffectedDocumentRegion == endEffectedDocumentRegion) {
                  val effectedDocRegion = startEffectedDocumentRegion
                  val textRegions = effectedDocRegion.getRegions().toArray().toArray // "textregionlist to java array to scala array
                  val leftTextRegion = effectedDocRegion.getRegionAtCharacterOffset(startEffectedOffset)
                  val rightTextRegion = effectedDocRegion.getRegionAtCharacterOffset(endEffectedOffset - 1) // end offsets are not inclusive
                  val newTextRegions = new ArrayBuffer[ITextRegion]
                  
                  // add text regions up to leftTextRegion
                  var i = 0
                  while (textRegions(i) != leftTextRegion) {
                    newTextRegions += textRegions(i)
                    i += 1
                  }
                  
                  // split left text region if necessary
                  if (effectedDocRegion.getStartOffset(leftTextRegion) < startEffectedOffset) {
                    val leftSplit = copyXMLTextRegion(leftTextRegion)
                    leftSplit.adjustLength(-(effectedDocRegion.getEndOffset(leftTextRegion) - startEffectedOffset))
                    newTextRegions += leftSplit
                  }
                  
                  // add template text regions
                  var currentTemplateOffset = startEffectedOffset - effectedDocRegion.getStart()
                  for (tr <- templateTextRegions) {
                    tr.setStart(currentTemplateOffset)
                    newTextRegions += tr
                    currentTemplateOffset += tr.getLength()
                  }
                  
                  // split right text region if necessary
                  if (effectedDocRegion.getEndOffset(rightTextRegion) > endEffectedOffset) {
                    val rightSplit = copyXMLTextRegion(rightTextRegion)
                    val adjustment = endEffectedOffset - effectedDocRegion.getStartOffset(rightTextRegion)
                    rightSplit.adjustStart(adjustment)
                    rightSplit.adjustLength(-adjustment)
                    newTextRegions += rightSplit
                  }
                  
                  // add text regions after right text region until end
                  i = 0
                  while (i < textRegions.length) {
                    val tr = textRegions(i)
                    if (tr.getStart() > rightTextRegion.getStart()) {
                      newTextRegions += tr
                    }
                    i += 1
                  }
                  
                  // replace the doc's text regions with the new text regions
                  val newTextRegionList = new TextRegionListImpl
                  for (tr <- newTextRegions) 
                    newTextRegionList.add(tr)
                  effectedDocRegion.setRegions(newTextRegionList)
                }
                else {
                  logger.debug("TemplateRegionParser: the scenario where scala code crosses two different xml document regions is not implemented.")
                }
              }
            }
            
            case _ => logger.debug("TemplateRegionParser: startEffectedDocumentRegion or endEffectedDocumentRegion could not be found.")
          }
        }
      }
    }

    // The html parser returns null on empty input.. Talk about handling corner cases :P
    if (resultRegions == null) {
      resultRegions = new BasicStructuredDocumentRegion
      resultRegions.setStart(0)
      resultRegions.setLength(codeString.length())
      resultRegions.addRegion(new ContextRegion("UNDEFINED", 0, codeString.length(), codeString.length()))
    }
    
    val ab = new ArrayBuffer[IStructuredDocumentRegion]
    var current = resultRegions
    while(current != null) {
      ab += current
      current = current.getNext() 
    }
    
    ab.result.toArray
  }
}
