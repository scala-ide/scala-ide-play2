package org.scalaide.play2.templateeditor.sse.lexical

import java.io.Reader
import java.io.StringReader
import scala.Array.canBuildFrom
import scala.tools.eclipse.lexical.ScalaCodeScanner
import org.eclipse.jface.text.IDocument
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.wst.sse.core.internal.ltk.parser.BlockMarker
import org.eclipse.wst.sse.core.internal.ltk.parser.RegionParser
import org.eclipse.wst.sse.core.internal.parser.ContextRegion
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.sse.core.internal.text.BasicStructuredDocumentRegion
import org.eclipse.wst.xml.core.internal.parser.XMLSourceParser
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses
import org.scalaide.play2.templateeditor.lexical.TemplatePartitionTokeniser
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions
import scalariform.ScalaVersions
import org.scalaide.editor.util.EditorHelper

object TemplateDocumentRegions {
  val SCALA_DOC_REGION = "SCALA_CONTENT"
  val COMMENT_DOC_REGION = "TEMPLATE_COMMENT"
}

class TemplateRegionParser extends RegionParser {
  
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
    
    // The block regions enable javascript and css support, as BLOCK_TEXT regions are treated special by the html component
    import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext
    import org.eclipse.wst.sse.core.internal.ltk.parser.BlockMarker
    val htmlParser = new XMLSourceParser
    htmlParser.addBlockMarker(new BlockMarker("script", null, DOMRegionContext.BLOCK_TEXT, false))
    htmlParser.addBlockMarker(new BlockMarker("style", null, DOMRegionContext.BLOCK_TEXT, false))
    
    // tokenise, merge '@' tokens with scala code tokens, and then merge any adjacent tokens of the same type.
    // Merging here has the affect of not having neighbouring document regions of the same type
    val tokens = TemplatePartitionTokeniser.tokenise(codeString)
    val mergedTokens = PartitionHelpers.mergeAdjacentWithSameType(PartitionHelpers.combineMagicAt(tokens, codeString)).toArray
    
    // map through the tokens, generating document regions with their child text regions
    val docRegions: Array[IStructuredDocumentRegion] = mergedTokens.map(token => {
      // Handle the empty codeString case
      if (token.getOffset() == 0 && token.getLength() == 0) {
        val docRegion = new BasicStructuredDocumentRegion
        docRegion.setStart(0)
        docRegion.setLength(0)
        docRegion.addRegion(new ContextRegion("UNDEFINED", 0, 0, 0))
        Array[IStructuredDocumentRegion](docRegion)
      }
      // Generate HTML regions using the html parser essentially in any case where
      // the token doesn't represent scala code, a template comment, or 
      else if (token.getType() == TemplatePartitions.TEMPLATE_PLAIN ||
               token.getType() == TemplatePartitions.TEMPLATE_TAG   ||
               (token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && !PartitionHelpers.isBrace(token, codeString))) {
        import scala.collection.JavaConversions._
        val tokenCode = codeString.substring(token.getOffset, token.getOffset + token.getLength)
        htmlParser.reset(tokenCode)
        var htmlFirstDocRegion = htmlParser.getDocumentRegions()
        val arrayBuilder = new scala.collection.mutable.ArrayBuffer[IStructuredDocumentRegion]
        while(htmlFirstDocRegion != null) {
          htmlFirstDocRegion.adjustStart(token.getOffset)
          arrayBuilder += htmlFirstDocRegion
          htmlFirstDocRegion = htmlFirstDocRegion.getNext()
        }
        arrayBuilder.result.toArray
      }
      else {
        // calculate the type of the document region to be created, and its child text regions
        val (tpe, textRegions): Tuple2[String, Seq[ITextRegion]] =
          if (token.getType == TemplatePartitions.TEMPLATE_SCALA) {
            val textRegions = {
              // I can probably do this is a smarter way by just checking if the string starts with an @, and if so
              // add the appropriate text region for the magic at, and then the rest of token becomes a normal
              // scala code text region. I don't believe there would ever be a case where an @ in the middle of the token,
              // but I really must verify that assumption before making the change.
              val scalaCode = codeString.substring(token.getOffset(), token.getOffset() + token.getLength())
              val textRegions: List[ITextRegion] = TemplatePartitionTokeniser.tokenise(scalaCode).map(t => {
                val baseOffset = token.getOffset() + t.getOffset()
                if (PartitionHelpers.isMagicAt(t, scalaCode)) {
                  List(new ScalaTextRegion(TemplateSyntaxClasses.MAGIC_AT, t.getOffset(), t.getLength(), t.getLength()))
                }
                // actual scala code
                else { //if (t.getType() == TemplatePartitions.TEMPLATE_SCALA) {
                  // TODO - figure out a good way to get the prefstore from the editor
                  val prefStore = new ChainedPreferenceStore(Array((EditorsUI.getPreferenceStore()), PlayPlugin.preferenceStore))
                  val scanner = new ScalaCodeScanner(prefStore, ScalaVersions.Scala_2_10)
                  val dummyDoc: IDocument = new org.eclipse.jface.text.Document(codeString)
                  val tokens = scanner.tokenize(dummyDoc, baseOffset, t.getLength())
                  tokens.map(v => {new ScalaTextRegion(v.syntaxClass, v.start - token.getOffset(), v.length, v.length)})
                }
              }).flatten
              textRegions
            }
            (TemplateDocumentRegions.SCALA_DOC_REGION, textRegions)
          }
          else if (PartitionHelpers.isBrace(token, codeString)) {
            val textRegion = new ScalaTextRegion(TemplateSyntaxClasses.BRACE, 0, token.getLength(), token.getLength())
            (TemplateDocumentRegions.SCALA_DOC_REGION, List(textRegion))
          }
          else if (token.getType == TemplatePartitions.TEMPLATE_COMMENT) {
            val textRegion = new ScalaTextRegion(TemplateSyntaxClasses.COMMENT, 0, token.getLength(), token.getLength())
            (TemplateDocumentRegions.COMMENT_DOC_REGION, List(textRegion))
          }
          else {
            // Should never happen, maybe throw an exception here?
            ("UNDEFINED", List(new ContextRegion("UNDEFINED", 0, 0, 0)))
          }
        // finally create the document region
        val region = new BasicStructuredDocumentRegion { override def getType() = tpe }
        region.setStart(token.getOffset)
        region.setLength(token.getLength)
        region.setEnded(false)
        textRegions.foreach(region.addRegion(_))
        Array[IStructuredDocumentRegion](region)
      }
    }).flatten
    
    // link up the internal intrusive list references
    docRegions.sliding(2).foreach(_ match {
      case Array(l, r) => {
        l.setNext(r)
        r.setPrevious(l)
        r.setNext(null)
      }
      case _ => // only happens when array is empty or of size one
    })
    docRegions.lastOption.foreach(_.setEnded(true))
    
    docRegions
  }
}
