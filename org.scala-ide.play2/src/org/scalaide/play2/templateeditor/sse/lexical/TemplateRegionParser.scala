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
import scala.collection.mutable.HashMap
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeNameRegion
import org.eclipse.wst.sse.core.internal.provisional.events.StructuredDocumentEvent
import org.eclipse.wst.sse.core.internal.provisional.events.RegionChangedEvent
import org.eclipse.wst.sse.core.internal.util.Debug
import org.eclipse.wst.xml.core.internal.parser.regions.RegionUpdateRule
import org.eclipse.wst.sse.core.internal.util.Utilities
import play.templates.ScalaTemplateParser
import org.eclipse.jface.text.TypedRegion
import scala.collection.mutable.ListBuffer
import org.eclipse.jface.text.Document

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
  private val scalaTokeniserDocument = new org.eclipse.jface.text.Document("")
  
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
    val sb = new StringBuffer
    var c = reader.read()
    while (c != -1) {
      sb.append(c.toChar)
      c = reader.read()
    }
    contents = sb.toString()
    cachedRegions.reset()
  }
  
  def computeRegions(codeString: String): Array[IStructuredDocumentRegion] = {
    
    def buildDocRegionMap(headRegion: IStructuredDocumentRegion): collection.Map[Int, IStructuredDocumentRegion] = {
      val map = new HashMap[Int, IStructuredDocumentRegion]
      map.sizeHint(codeString.length / 50) // just a gut feeling that doc regions average around 50 characters in size
      var current = headRegion
      while (current != null) {
        for (i <- current.getStart() to (current.getEnd() - 1))
          map(i) = current
        current = current.getNext()
      }
      map
    }
    
    def getNumberOfDocumentRegions(headRegion: IStructuredDocumentRegion) = {
      @tailrec
      def aux(region: IStructuredDocumentRegion, count: Int): Int = {
        if (region == null) count
        else aux(region.getNext(), count + 1)
      }
      aux(headRegion, 0)
    }
    
    // This is a copy and pasted implementation of AttributeNameRegion, with the only change being that adjustTextLength
    // actually adds the parameter to the field :). Had to reimplement the whole class because the fields in AttributeNameRegion are private
    class FixedAttributeNameRegion(start: Int, textLength: Int, length: Int) extends AttributeNameRegion(start, textLength, length) { 
      private var fLength: Int = length
      private var fStart: Int = start
      private var fTextLength: Int = textLength

      override def adjustLength(i: Int) { fLength += i }
      override def adjustStart(i: Int) { fStart += i }
      override def adjustTextLength(i: Int) { fTextLength += i } // this is the only thing that's changed!!
      override def equatePositions(region: ITextRegion) {
        fStart = region.getStart
        fLength = region.getLength
        fTextLength = region.getTextLength
      }
      override def getEnd(): Int = fStart + fLength
      override def getLength(): Int = fLength
      override def getStart(): Int = fStart
      override def getTextEnd(): Int = fStart + fTextLength
      override def getTextLength(): Int = fTextLength
      override def updateRegion(requester: AnyRef, 
          parent: IStructuredDocumentRegion, 
          changes: String, 
          requestStart: Int, 
          lengthToReplace: Int): StructuredDocumentEvent = {
        var result: RegionChangedEvent = null
        if (Debug.debugStructuredDocument) {
          println("\t\tContextRegion::updateModel")
          println("\t\t\tregion type is " + getType())
        }
        var canHandle = false
        canHandle = if ((changes == null) || (changes.length == 0)) if ((fStart >= getTextEnd) || 
          (Math.abs(lengthToReplace) >= getTextEnd - getStart)) false else true else if ((RegionUpdateRule.canHandleAsWhiteSpace(this, 
          parent, changes, requestStart, lengthToReplace)) || 
          RegionUpdateRule.canHandleAsLetterOrDigit(this, parent, changes, requestStart, lengthToReplace)) true else false
        if (canHandle) {
          if (Debug.debugStructuredDocument) {
            println("change handled by region")
          }
          val lengthDifference = Utilities.calculateLengthDifference(changes, lengthToReplace)
          if (!RegionUpdateRule.canHandleAsWhiteSpace(this, parent, changes, requestStart, lengthToReplace)) {
            fTextLength += lengthDifference
          }
          fLength += lengthDifference
          result = new RegionChangedEvent(parent.getParentDocument, requester, parent, this, changes, requestStart, 
            lengthToReplace)
        }
        result
      }
    }

    def copyXMLTextRegion(region: ITextRegion): ITextRegion = region match {
      case attribEquals: AttributeEqualsRegion => new AttributeEqualsRegion(attribEquals.getStart(), attribEquals.getTextLength(), attribEquals.getLength())
      case attribName: AttributeNameRegion => new FixedAttributeNameRegion(attribName.getStart(), attribName.getTextLength(), attribName.getLength())
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
    lazy val htmlDocumentRegionsMap = buildDocRegionMap(htmlDocumentRegions)
    lazy val htmlDocumentRegionsLength = getNumberOfDocumentRegions(htmlDocumentRegions)
    var resultRegions = htmlDocumentRegions
    
    // tokenise, merge '@' tokens with scala code tokens, and then merge any adjacent tokens of the same type.
    // Merging here has the affect of not having neighbouring document regions of the same type
    val tokens = TemplatePartitionTokeniser.tokenise(codeString).toVector
    val tokenIndexLookup = new collection.mutable.HashMap[Int, TypedRegion]
    tokenIndexLookup.sizeHint(tokens.length)
    for (reg <- tokens) {
      val kv = (reg.getOffset(), reg)
      tokenIndexLookup += kv
    }

    val mergedTokens = PartitionHelpers.mergeAdjacentWithSameType(PartitionHelpers.combineMagicAt(tokens, codeString)).toArray
    val dummyDoc = new Document(codeString)
    for (token <- mergedTokens) {
      val representsHTML =
        (token.getType() == TemplatePartitions.TEMPLATE_PLAIN ||
         token.getType() == TemplatePartitions.TEMPLATE_TAG   ||
         (token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && !PartitionHelpers.isBrace(token, codeString) && !PartitionHelpers.isCombinedBraceMagicAt(token, codeString)))
      if (!representsHTML) {
        val (templateTextRegions: Seq[ScalaTextRegion], scalaDocType: String) = {
          if (token.getType == TemplatePartitions.TEMPLATE_SCALA) {
            val regions = new ListBuffer[ScalaTextRegion]
            var currentIndex = token.getOffset()
            while (currentIndex < (token.getOffset() + token.getLength())) {
              val t = tokenIndexLookup(currentIndex)
              if (PartitionHelpers.isMagicAt(t, codeString)) {
                regions += new ScalaTextRegion(TemplateSyntaxClasses.MAGIC_AT, t.getOffset() - token.getOffset(), t.getLength(), t.getLength())
              } // actual scala code
              else { //if (t.getType() == TemplatePartitions.TEMPLATE_SCALA) {
                // TODO - figure out a good way to get the prefstore from the editor
                val prefStore = new ChainedPreferenceStore(Array((EditorsUI.getPreferenceStore()), PlayPlugin.preferenceStore))
                val scanner = new ScalaCodeScanner(prefStore, ScalaVersions.Scala_2_10)
                val tokens = scanner.tokenize(dummyDoc, t.getOffset(), t.getLength())
                tokens.foreach{ v =>
                  regions += new ScalaTextRegion(v.syntaxClass, v.start - token.getOffset(), v.length, v.length)
                }
              }
              currentIndex += t.getLength()
            } 
            
            (regions, TemplateDocumentRegions.SCALA_DOC_REGION)
          }
          else if (PartitionHelpers.isBrace(token, codeString))
            (List(new ScalaTextRegion(TemplateSyntaxClasses.BRACE, 0, token.getLength(), token.getLength())), TemplateDocumentRegions.SCALA_DOC_REGION)
          else if (token.getType == TemplatePartitions.TEMPLATE_COMMENT)
            (List(new ScalaTextRegion(TemplateSyntaxClasses.COMMENT, 0, token.getLength(), token.getLength())), TemplateDocumentRegions.COMMENT_DOC_REGION)
          else if (PartitionHelpers.isCombinedBraceMagicAt(token, codeString)) {
            val splitIndex = codeString.indexWhere({c: Char => (c == '}' || c == '{')}, token.getOffset())
            val braceLen = splitIndex - token.getOffset()
            val magicAtLen = token.getLength() - braceLen
            val brace = new ScalaTextRegion(TemplateSyntaxClasses.BRACE, 0, braceLen, braceLen)
            val magicAt = new ScalaTextRegion(TemplateSyntaxClasses.MAGIC_AT, braceLen, magicAtLen, magicAtLen)
            (List(brace, magicAt), TemplateDocumentRegions.SCALA_DOC_REGION)
          }
          else (List(), "UNDEFINED")
        }
        
        if (templateTextRegions.nonEmpty) {
          // absolute offsets
          val (startEffectedOffset, endEffectedOffset) = (token.getOffset(), token.getOffset() + token.getLength())
          val startEffectedDocumentRegionOpt = htmlDocumentRegionsMap.get(startEffectedOffset)
          val endEffectedDocumentRegionOpt = htmlDocumentRegionsMap.get(endEffectedOffset - 1)
          
          (startEffectedDocumentRegionOpt, endEffectedDocumentRegionOpt) match {
            case (Some(startEffectedDocumentRegion), Some(endEffectedDocumentRegion)) => {
              // If the scala text regions spans exactly the same space as the document regions it overlaps
              // Then replace those document regions with our own document regions
              if (startEffectedDocumentRegion.getStart() == startEffectedOffset && endEffectedDocumentRegion.getEnd() == endEffectedOffset) {
                val region = new BasicStructuredDocumentRegion { override def getType() = scalaDocType }
                region.setStart(startEffectedOffset)
                region.setLength(token.getLength())
                templateTextRegions.foreach(region.addRegion(_))
                
                if (htmlDocumentRegionsLength == 1) {
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
                def insertScalaRegions(effectedDocRegion: IStructuredDocumentRegion, templateTextRegions: Seq[ScalaTextRegion], startEffectedOffset: Int, endEffectedOffset: Int) = {
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
                    val adjustment = -(effectedDocRegion.getEndOffset(leftTextRegion) - startEffectedOffset)
                    leftSplit.adjustLength(adjustment)
                    leftSplit.adjustTextLength(adjustment)
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
                    rightSplit.adjustTextLength(-adjustment)
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
                
                // handle the case where we need to trim text regions and then insert our text regions
                // Note: this code does not try to handle scala code that spans two (or more) xml document regions
                if (startEffectedDocumentRegion == endEffectedDocumentRegion) {
                  val effectedDocRegion = startEffectedDocumentRegion
                  insertScalaRegions(effectedDocRegion, templateTextRegions, startEffectedOffset, endEffectedOffset)
                }
                else {
                  // Get a list of all effected document regions, plus their corresponding start and end effected offsets
                  //   (which will correspond exactly to the document's start and end for the non-head and non-tail elements.
                  var effectedDocuments: ListBuffer[(IStructuredDocumentRegion, Int, Int)] = {
                     val result: ListBuffer[(IStructuredDocumentRegion, Int, Int)] = ListBuffer((startEffectedDocumentRegion, startEffectedOffset, startEffectedDocumentRegion.getEnd()))
                     var doc = startEffectedDocumentRegion.getNext()
                     while (doc != endEffectedDocumentRegion) {
                       doc = doc.getNext()
                       if (doc != endEffectedDocumentRegion)
                         result += ((doc, doc.getStart(), doc.getEnd()))
                     } 
                     result += ((endEffectedDocumentRegion, endEffectedDocumentRegion.getStart(), endEffectedOffset))
                     
                     // there can be gaps.. so create new doc regions for the gaps
                     for (i <- 0 to (result.length - 2)) {
                       val ((l, lstart, lend), (r, rstart, _)) = result(i) -> result(i+1)
                       if (lend != rstart) {
                         val newRegion = new BasicStructuredDocumentRegion
                         newRegion.addRegion(new ContextRegion("UNDEFINED", 0, rstart - lend, rstart - lend))
                         newRegion.setStart(lend)
                         newRegion.setLength(rstart - lend)
                         l.setNext(newRegion)
                         newRegion.setPrevious(l)
                         newRegion.setNext(r)
                         r.setPrevious(newRegion)
                         result.insert(i+1, (newRegion, lend, rstart))
                       }
                     }
                     
                     result
                   }
                  
                  var currentTextRegion = 0 
                  val scalaRegions: ArrayBuffer[ScalaTextRegion] = ArrayBuffer()
                  for ((effectedDocRegion, startEffectedOffset, endEffectedOffset) <- effectedDocuments) {
                    def globalOffset(offset: Int) = token.getOffset() + offset
                    
                    var done = false
                    while (!done && currentTextRegion < templateTextRegions.length) {
                      val tr = templateTextRegions(currentTextRegion)
                      val trGlobalStart = globalOffset(tr.getStart())
                      val trGlobalEnd = globalOffset(tr.getEnd())
                      
                      val contained = trGlobalStart >= startEffectedOffset && trGlobalEnd <= endEffectedOffset
                      val leftOverlap = trGlobalStart < startEffectedOffset && trGlobalEnd <= endEffectedOffset
                      val rightOverlap = trGlobalStart >= startEffectedOffset && trGlobalStart < endEffectedOffset && trGlobalEnd > endEffectedOffset
                      val overspan = trGlobalStart <= startEffectedOffset && trGlobalEnd >= endEffectedOffset
                      
                      // the scala text region is fully within the doc region
                      if (contained) {
                        scalaRegions += tr
                        currentTextRegion += 1
                      }
                      // the scala text region overlaps the left hand side of the doc region
                      else if (leftOverlap) {
                        val trcopy = tr.copy()
                        trcopy.setStart(startEffectedOffset - effectedDocRegion.getStart())
                        trcopy.setLength(trcopy.getLength() - (startEffectedOffset - trGlobalStart))
                        scalaRegions += trcopy
                        currentTextRegion += 1
                      }
                      // the scala text region overlaps the right hand side of the doc region
                      else if (rightOverlap) {
                        val trcopy = tr.copy()
                        trcopy.setLength(trcopy.getLength() - (trGlobalEnd - endEffectedOffset))
                        scalaRegions += trcopy
                        // don't increment currentTextRegion because it might also be used by the next structured document
                      }
                      // the scala text region fully encompasses the doc region 
                      else if (overspan) {
                        val trcopy = tr.copy()
                        trcopy.setStart(0)
                        trcopy.setLength(effectedDocRegion.getLength())
                        scalaRegions += tr.copy()
                        // don't increment currentTextRegion because it might also be used by the next structured document
                      }
                      
                      done = overspan || rightOverlap || (trGlobalStart >= endEffectedOffset)
                    }
                    
                    insertScalaRegions(effectedDocRegion, scalaRegions, startEffectedOffset, endEffectedOffset)
                    scalaRegions.clear()
                  }
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
