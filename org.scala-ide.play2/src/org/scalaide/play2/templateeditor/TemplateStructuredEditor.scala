package org.scalaide.play2.templateeditor

import java.io.Reader
import java.io.StringReader
import scala.Array.canBuildFrom
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.TraversableOnce.flattenTraversableOnce
import org.eclipse.core.runtime.IAdaptable
import org.eclipse.core.runtime.content.IContentDescriber
import org.eclipse.core.runtime.content.IContentDescription
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IDocumentPartitioner
import org.eclipse.jface.text.ITypedRegion
import org.eclipse.jface.text.contentassist.ContentAssistant
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.jst.jsp.core.internal.encoding.JSPDocumentHeadContentDetector
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.wst.html.core.internal.encoding.HTMLDocumentCharsetDetector
import org.eclipse.wst.html.core.internal.provisional.contenttype.ContentTypeIdForHTML
import org.eclipse.wst.html.core.internal.text.StructuredTextPartitionerForHTML
import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML
import org.eclipse.wst.html.ui.internal.contentassist.HTMLStructuredContentAssistProcessor
import org.eclipse.wst.html.ui.internal.contentoutline.JFaceNodeAdapterFactoryForHTML
import org.eclipse.wst.sse.core.internal.document.AbstractDocumentLoader
import org.eclipse.wst.sse.core.internal.document.DocumentReader
import org.eclipse.wst.sse.core.internal.document.IDocumentCharsetDetector
import org.eclipse.wst.sse.core.internal.document.IDocumentLoader
import org.eclipse.wst.sse.core.internal.document.StructuredDocumentFactory
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.AbstractModelHandler
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.IDocumentTypeHandler
import org.eclipse.wst.sse.core.internal.ltk.parser.RegionParser
import org.eclipse.wst.sse.core.internal.model.AbstractModelLoader
import org.eclipse.wst.sse.core.internal.model.AbstractStructuredModel
import org.eclipse.wst.sse.core.internal.parser.ContextRegion
import org.eclipse.wst.sse.core.internal.provisional.IModelLoader
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredTextPartitioner
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.sse.core.internal.text.BasicStructuredDocumentRegion
import org.eclipse.wst.sse.core.internal.text.CharSequenceReader
import org.eclipse.wst.sse.ui.StructuredTextEditor
import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration
import org.eclipse.wst.sse.ui.contentassist.StructuredContentAssistProcessor
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter
import org.eclipse.wst.sse.ui.internal.provisional.registry.AdapterFactoryProvider
import org.eclipse.wst.sse.ui.internal.provisional.style.AbstractLineStyleProvider
import org.eclipse.wst.sse.ui.internal.provisional.style.LineStyleProvider
import org.eclipse.wst.sse.ui.internal.util.Assert
import org.eclipse.wst.xml.core.internal.parser.XMLSourceParser
import org.eclipse.wst.xml.core.internal.provisional.contenttype.ContentTypeIdForXML
import org.eclipse.wst.xml.core.text.IXMLPartitions
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML
import org.eclipse.wst.xml.ui.internal.contentassist.XMLStructuredContentAssistProcessor
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.templateeditor.lexical.TemplateDocumentPartitioner
import org.scalaide.play2.templateeditor.lexical.TemplateParsing
import org.scalaide.play2.templateeditor.lexical.TemplateParsing.CommentCode
import org.scalaide.play2.templateeditor.lexical.TemplateParsing.DefaultCode
import org.scalaide.play2.templateeditor.lexical.TemplateParsing.ScalaCode
import org.scalaide.play2.templateeditor.lexical.TemplatePartitionTokeniser
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions
import scala.tools.eclipse.lexical.ScalaCodeScanner
import scalariform.ScalaVersions
import org.eclipse.wst.sse.core.internal.parser.ForeignRegion
import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass
import org.eclipse.wst.sse.core.internal.text.BasicStructuredDocument
import org.eclipse.wst.xml.core.internal.parser.XMLStructuredDocumentReParser
import org.eclipse.jface.text.TypedRegion
import org.eclipse.wst.sse.core.internal.text.rules.StructuredTextPartitioner
import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClasses
import org.eclipse.jst.jsp.core.internal.parser.JSPSourceParser
import org.eclipse.jface.text.DocumentEvent
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator
import org.eclipse.wst.validation._
import org.eclipse.wst.validation.internal.provisional.core.IValidator
import org.eclipse.wst.html.core.internal.document.DOMStyleModelImpl
import org.eclipse.wst.html.core.internal.encoding.HTMLDocumentLoader
import org.eclipse.wst.html.core.internal.encoding.HTMLModelLoader
import org.eclipse.wst.sse.core.StructuredModelManager
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.contentassist.IContextInformationValidator

object ContentTypeIdForScala {
  val ContentTypeID_Scala = "org.scalaide.play2.templateSource"
}

object TemplateStructuredPartitions {
  val TEMPLATE_HTML = "__template_html"
  val TEMPLATE_SCALA = "__template_scala"
  val TEMPLATE_COMMENT = "__template_comment"
  val TEMPLATE_PARTITIONING = "___template_partitioning";
  val TEMPLATE_DEFAULT = "__template_default"

  def getTypes() = {
    Array(TEMPLATE_DEFAULT, TEMPLATE_SCALA, TEMPLATE_COMMENT, TEMPLATE_HTML);
  }
}

object PartitionHelpers {

  def isMagicAt(token: ITypedRegion, codeString: String) = {
    val s = codeString.substring(token.getOffset(), token.getOffset() + token.getLength()).trim
    token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && s.length == 1 && s == "@"
  }
  
  def isBrace(token: ITypedRegion, codeString: String) = {
    val s = codeString.substring(token.getOffset(), token.getOffset() + token.getLength()).trim
    token.getType() == TemplatePartitions.TEMPLATE_DEFAULT && s.length == 1 && (s == "}" || s == "{")
  }
  
  /* Combines neighbouring regions that have the same type */
  def mergeAdjacent[Repr <: Seq[ITypedRegion]](partitions: Repr)(test: (ITypedRegion, ITypedRegion) => Option[String]): IndexedSeq[ITypedRegion] = {
    partitions.foldLeft(Array[ITypedRegion]())((accum, region) => {
      accum match {
        case Array() => Array(region)
        case _       => {
          def merge(l: ITypedRegion, r: ITypedRegion, t: String) = 
            new TypedRegion(l.getOffset, l.getLength + r.getLength, t)
          val htmlPartitions = Set(TemplatePartitions.TEMPLATE_PLAIN, TemplatePartitions.TEMPLATE_TAG)
          val previousRegion = accum.last
          test(previousRegion, region) match {
            case Some(tpe) => accum.dropRight(1) :+ merge(previousRegion, region, tpe)
            case None      => accum :+ region
          }
        }
      }
    })
  }
  
  /* Combines neighbouring regions that have the same type */
  def mergeAdjacentWithSameType[Repr <: Seq[ITypedRegion]](partitions: Repr): IndexedSeq[ITypedRegion] = {
    val htmlPartitions = Set(TemplatePartitions.TEMPLATE_PLAIN, TemplatePartitions.TEMPLATE_TAG)
    mergeAdjacent(partitions) { (previousRegion, region) =>
      if (((htmlPartitions contains region.getType) && (htmlPartitions contains previousRegion.getType)) ||
         (region.getType == TemplatePartitions.TEMPLATE_SCALA && previousRegion.getType == TemplatePartitions.TEMPLATE_SCALA))
        Some(region.getType())
      else None
    }
  }

  def combineMagicAt[Repr <: Seq[ITypedRegion]](partitions: Repr, codeString: String): IndexedSeq[ITypedRegion] = {
    mergeAdjacent(partitions) { (left, right) =>
      if ((isMagicAt(left, codeString) && right.getType() == TemplatePartitions.TEMPLATE_SCALA) ||
          (left.getType() == TemplatePartitions.TEMPLATE_SCALA && isMagicAt(right, codeString)))
        Some(TemplatePartitions.TEMPLATE_SCALA)
      else None
    }
  }
  
}

class TemplateStructuredTextPartitioner extends StructuredTextPartitioner {
  private val templatePartitioner: TemplateDocumentPartitioner = new TemplateDocumentPartitioner
  private val htmlPartitioner: StructuredTextPartitionerForHTML = new StructuredTextPartitionerForHTML
  private var document: IDocument = null
  
  override def connect(document: IDocument) = {
    super.connect(document)
    htmlPartitioner.connect(document)
  }
  
  override def disconnect() = {
    super.disconnect()
    htmlPartitioner.disconnect()
  }
  
  override def documentChanged(event: DocumentEvent): Boolean = {
    super.documentChanged(event)
  }
  
  override def computePartitioning(offset: Int, length: Int) = {
    htmlPartitioner.computePartitioning(offset, length)
    super.computePartitioning(offset, length)
  } 
  
  override def getDefaultPartitionType() = {
    TemplatePartitions.TEMPLATE_DEFAULT
  }
  
  override def getPartitionType(region: ITextRegion, offset: Int) = {
    region match {
      case scalaRegion: ScalaTextRegion => scalaRegion.syntaxClass match {
        case TemplateSyntaxClasses.COMMENT => TemplatePartitions.TEMPLATE_COMMENT
        case TemplateSyntaxClasses.MAGIC_AT => TemplatePartitions.TEMPLATE_PLAIN
        case TemplateSyntaxClasses.BRACE => TemplatePartitions.TEMPLATE_PLAIN
        case _ => TemplatePartitions.TEMPLATE_SCALA
      }
      case _ => htmlPartitioner.getPartitionType(region, offset)
    }
  }
  
  override protected def setInternalPartition(offset: Int, length: Int, tpe: String) = {
    val region = htmlPartitioner.createPartition(offset, length, tpe)
    super.setInternalPartition(region.getOffset, region.getLength, region.getType)
  }
  
  override def newInstance(): IDocumentPartitioner = {
    val instance = new TemplateStructuredTextPartitioner
    instance.connect(document)
    instance
  }
}

object TemplateRegions {
  val SCALA_DOC_REGION = "SCALA_CONTENT"
  val COMMENT_DOC_REGION = "TEMPLATE_COMMENT"
  
}

class ScalaTextRegion(val syntaxClass: ScalaSyntaxClass, newStart: Int, newTextLength: Int, newLength: Int)
  extends ContextRegion(syntaxClass.displayName, newStart, newTextLength, newLength)

class TemplateRegionParser extends RegionParser {
  
  class LazyCache[T](value: => T) {
    private var cache: Option[T] = None

    def apply() = cache.getOrElse {
      cache = Some(value)
      cache.get
    }
    
    def reset() { cache = None }
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
  
  def computeRegions(codeString: String) = {
    
    import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext
    import org.eclipse.wst.sse.core.internal.ltk.parser.BlockMarker
    val htmlParser = new XMLSourceParser
    htmlParser.addBlockMarker(new BlockMarker("script", null, DOMRegionContext.BLOCK_TEXT, false))
    htmlParser.addBlockMarker(new BlockMarker("style", null, DOMRegionContext.BLOCK_TEXT, false))
    
    val tokens = TemplatePartitionTokeniser.tokenise(codeString)
    val mergedTokens = PartitionHelpers.mergeAdjacentWithSameType(PartitionHelpers.combineMagicAt(tokens, codeString)).toArray
//    var ts = ""; tokens.foreach(t => {ts = ts + codeString.substring(t.getOffset(), t.getOffset() + t.getLength()) + " : " + t + "\n" + ("=" * 10) + "\n"})
//    var ts2 = ""; mergedTokens.foreach(t => {ts2 = ts2 + codeString.substring(t.getOffset(), t.getOffset() + t.getLength()) + " : " + t + "\n" + ("=" * 10) + "\n"})
    val docRegions: Array[IStructuredDocumentRegion] = mergedTokens.map(token => {
      // Handle the empty codeString case
      if (token.getOffset() == 0 && token.getLength() == 0) {
        val docRegion = new BasicStructuredDocumentRegion
        docRegion.setStart(0)
        docRegion.setLength(0)
        docRegion.addRegion(new ContextRegion("UNDEFINED", 0, 0, 0))
        Array[IStructuredDocumentRegion](docRegion)
      }
      // Generate HTML regions using the html parser
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
        val (tpe, textRegions): Tuple2[String, Seq[ITextRegion]] =
          if (token.getType == TemplatePartitions.TEMPLATE_SCALA) {
            val textRegions = {
              // I can probably do this is a smarter way by just checking if the string starts with an @, and if so
              // add the appropriate text region for the magic at, and then the rest of token becomes a normal
              // scala code text region
              val scalaCode = codeString.substring(token.getOffset(), token.getOffset() + token.getLength())
              val scalaCodeTokens = TemplatePartitionTokeniser.tokenise(scalaCode)
              val textRegions: List[ITextRegion] = scalaCodeTokens.map( t => {
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
            (TemplateRegions.SCALA_DOC_REGION, textRegions)
          }
          else if (PartitionHelpers.isBrace(token, codeString)) {
            val textRegion = new ScalaTextRegion(TemplateSyntaxClasses.BRACE, 0, token.getLength(), token.getLength())
            (TemplateRegions.SCALA_DOC_REGION, List(textRegion))
          }
          else if (token.getType == TemplatePartitions.TEMPLATE_COMMENT) {
            val textRegion = new ScalaTextRegion(TemplateSyntaxClasses.COMMENT, 0, token.getLength(), token.getLength())
            (TemplateRegions.COMMENT_DOC_REGION, List(textRegion))
          }
          else {
            // Should never happen
            ("UNDEFINED", List(new ContextRegion("UNDEFINED", 0, 0, 0)))
          }
        val region = new BasicStructuredDocumentRegion { override def getType() = tpe }
        region.setStart(token.getOffset)
        region.setLength(token.getLength)
        region.setEnded(false)
        textRegions.foreach(region.addRegion(_))
        Array[IStructuredDocumentRegion](region)
      }
    }).flatten
    docRegions.sliding(2).foreach(_ match {
      case Array(l, r) => {
        l.setNext(r)
        r.setPrevious(l)
        r.setNext(null)
      }
      case _ =>
    })
    docRegions.lastOption.foreach(_.setEnded(true))
    var s = ""; docRegions.foreach(r => s = s + r.toString + " : " + r.getType + "\n")
    docRegions
  }
}
class TemplateDocumentLoader extends HTMLDocumentLoader {
  
  override def newEncodedDocument() = {
    val structuredDocument = StructuredDocumentFactory.getNewStructuredDocumentInstance(getParser())
    val reparser = new XMLStructuredDocumentReParser {
      override protected def findDirtyEnd(end: Int): IStructuredDocumentRegion = {
        val result = fStructuredDocument.getLastStructuredDocumentRegion()
        if (result != null) fStructuredDocument.setCachedDocumentRegion(result)
        dirtyEnd = result
        dirtyEnd
      }

      override protected def findDirtyStart(start: Int): Unit = {
        val result = fStructuredDocument.getFirstStructuredDocumentRegion()
        if (result != null) fStructuredDocument.setCachedDocumentRegion(result)
        dirtyStart = result
      }
    }
    structuredDocument.asInstanceOf[BasicStructuredDocument].setReParser(reparser)
    // TODO - set the default embedded type content type handler.. whatever that means
    structuredDocument
  }

  override def newInstance() = new TemplateDocumentLoader
  
  override def getParser(): RegionParser = new TemplateRegionParser
  
  override val getDefaultDocumentPartitioner = new TemplateStructuredTextPartitioner
}

// This class (and the inner classes) will need to be overhauled when adding HTMLValidator support
class TemplateStructuredModel extends DOMStyleModelImpl { modelself =>
  
  import org.eclipse.wst.xml.core.internal.document.DOMModelImpl
  import org.eclipse.wst.xml.core.internal.document.XMLModelParser
  import org.eclipse.wst.xml.core.internal.document.XMLModelUpdater

  class NestedDOMModelParser(model: DOMModelImpl) extends XMLModelParser(model) 
  class NestedDOMModelUpdater(model: DOMModelImpl) extends XMLModelUpdater(model)
  override def createModelParser() = new NestedDOMModelParser(this)
  override def createModelUpdater() = new NestedDOMModelUpdater(this)
}
class TemplateModelLoader extends HTMLModelLoader {
 override def getDocumentLoader(): IDocumentLoader = new TemplateDocumentLoader 
 override def newModel(): IStructuredModel = new TemplateStructuredModel
 override def newInstance(): IModelLoader = new TemplateModelLoader
 
}

object TemplateModelHandler {
  val AssociatedContentTypeID = "org.scalaide.play2.templateSource"
  val ModelHandlerID = "org.scalaide.play2.templateModelHandler"
}

class TemplateModelHandler extends AbstractModelHandler() {
  setId(TemplateModelHandler.ModelHandlerID)
  setAssociatedContentTypeId(TemplateModelHandler.AssociatedContentTypeID)

  override def getEncodingDetector(): IDocumentCharsetDetector = new JSPDocumentHeadContentDetector
  override def getDocumentLoader(): IDocumentLoader = new TemplateDocumentLoader
  override def getModelLoader(): IModelLoader = new TemplateModelLoader
}

class TemplateAdapterFactoryProvider extends AdapterFactoryProvider {
  override def addAdapterFactories(structuredModel: IStructuredModel) = {
    val factoryRegistry = structuredModel.getFactoryRegistry()
    Assert.isNotNull(factoryRegistry, "Program error: client caller must ensure model has factory registry")
    if (factoryRegistry.getFactoryFor(classOf[IJFaceNodeAdapter]) == null) {
      factoryRegistry.addFactory(new JFaceNodeAdapterFactoryForHTML)
    }
  }

  override def isFor(contentTypeDescription: IDocumentTypeHandler) = contentTypeDescription match {
    case _: TemplateModelHandler => true
    case _ => false
  }
  
  override def reinitializeFactories(structuredModel: IStructuredModel) = { }
}

class TemplateContentDescriber extends IContentDescriber {
  override def describe(contents: java.io.InputStream, description: IContentDescription) = IContentDescriber.VALID
  override def getSupportedOptions() = Array()
}

class ScalaLineStyleProvider(prefStore: IPreferenceStore) extends AbstractLineStyleProvider with LineStyleProvider {
   protected override def getAttributeFor(region: ITextRegion) = {
     region match {
       case scalaRegion: ScalaTextRegion => {
         scalaRegion.syntaxClass.getTextAttribute(getColorPreferences)
       }
       case _ => null
     }
   }
   
   protected override def getColorPreferences() = prefStore
   
   protected override def loadColors(): Unit =
     { /* ScalaSyntaxClass instances are stored internally in the scala text regions*/ }
}

class ScalaSourceValidator extends IValidator {
  import org.eclipse.wst.validation.internal.provisional.core.IReporter
  import org.eclipse.wst.validation.internal.provisional.core.IValidationContext
  
  /* IValidator methods */
  
  def cleanup(report: IReporter) = {}
  
  def validate(helper: IValidationContext, reporter: IReporter) = {
    import org.eclipse.core.resources.IMarker
    import org.eclipse.core.resources.IResource
    import org.eclipse.core.resources.ResourcesPlugin
    import org.eclipse.core.runtime.Path
    import org.eclipse.jdt.core.IJavaModelMarker
    import org.eclipse.wst.validation.internal.operations.LocalizedMessage
    import org.eclipse.wst.validation.internal.provisional.core.IMessage
    import org.scalaide.play2.templateeditor.TemplateCompilationUnit
    import scala.util.Try

    val wsroot = ResourcesPlugin.getWorkspace().getRoot()
    for {
      uri <- helper.getURIs()
      if !reporter.isCancelled()
      currentFile <- Option(wsroot.getFile(new Path(uri)))
      if currentFile.exists()
      model <- Try(StructuredModelManager.getModelManager().getModelForRead(currentFile))
    }{
      try {
        val markerType = IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER
        for {
          markers <- Try(currentFile.findMarkers(markerType, true, IResource.DEPTH_ONE))
          marker <- markers
        } marker.delete()
        
        val doc = model.getStructuredDocument()
        val compilationUnit = TemplateCompilationUnit.fromFileAndDocument(currentFile, doc)
        for (error <- compilationUnit.reconcile(doc.get())) {
          val (priority, severity) =
            if (error.isError()) (IMarker.PRIORITY_HIGH, IMarker.SEVERITY_ERROR)
            else if (error.isWarning()) (IMarker.PRIORITY_NORMAL, IMarker.SEVERITY_WARNING)
            else (IMarker.PRIORITY_LOW, IMarker.SEVERITY_INFO)

          val marker = currentFile.createMarker(markerType)
          marker.setAttribute(IMarker.LINE_NUMBER, doc.getLineOfOffset(error.getSourceStart()) + 1)
          marker.setAttribute(IMarker.CHAR_START, error.getSourceStart())
          marker.setAttribute(IMarker.CHAR_END, error.getSourceStart() + (error.getSourceEnd() - error.getSourceStart() + 1))
          marker.setAttribute(IMarker.MESSAGE, error.getMessage())
          marker.setAttribute(IMarker.USER_EDITABLE, java.lang.Boolean.FALSE)
          marker.setAttribute(IMarker.PRIORITY, priority)
          marker.setAttribute(IMarker.SEVERITY, severity)
        }
      }
      finally {
        model.releaseFromRead()
      }
    }
  }
}

// According to http://www.eclipsezone.com/eclipse/forums/t73617.html#92026455 in the SourceViewer and SourceViewerConfiguration hierarchy
// "content type" actually means "partition type"
class TemplateStructuredTextViewerConfiguration(prefStore: IPreferenceStore, editor: TTemplateEditor) extends StructuredTextViewerConfiguration {

  def this() = this(null, null)

  private val htmlConfiguration = new StructuredTextViewerConfigurationHTML
  private val xmlConfiguration = new StructuredTextViewerConfigurationXML
  private val scalaConfiguration: TemplateConfiguration = if (prefStore != null && editor != null) new TemplateConfiguration(prefStore, editor) else null

  private sealed trait ContentType
  private case class HTMLContent extends ContentType
  private case class XMLContent extends ContentType
  private case class ScalaContent extends ContentType
  private case class DefaultContent extends ContentType

  private object ContentType {
    def apply(sourceViewer: ISourceViewer, contentType: String) = {
      var htmlContentTypes = htmlConfiguration.getConfiguredContentTypes(sourceViewer).toSet
      val xmlContentTypes = xmlConfiguration.getConfiguredContentTypes(sourceViewer).toSet
      htmlContentTypes = htmlContentTypes -- xmlContentTypes
      val scalaPartitions = (scalaConfiguration.getConfiguredContentTypes(sourceViewer)).toSet
      if (htmlContentTypes contains contentType) HTMLContent()
      else if (xmlContentTypes contains contentType) XMLContent()
      else if (scalaPartitions contains contentType) ScalaContent()
      else {
        println(contentType)
        DefaultContent()
      }
    }
  }
  
  override def getDoubleClickStrategy(sourceViewer: ISourceViewer, contentType: String) = {
    ContentType(sourceViewer, contentType) match {
      case HTMLContent() => htmlConfiguration.getDoubleClickStrategy(sourceViewer, contentType)
      case XMLContent() => xmlConfiguration.getDoubleClickStrategy(sourceViewer, contentType)
      case ScalaContent() => scalaConfiguration.getDoubleClickStrategy(sourceViewer, contentType)
      case _ => super.getDoubleClickStrategy(sourceViewer, contentType)
    }
  }

  // TODO - figure out what this method is used for, so I can customize it if needed.
  //        for now, stole it from the JSP implementation
  override def getIndentPrefixes(sourceViewer: ISourceViewer, contentType: String) = {
    if (contentType == IXMLPartitions.XML_DEFAULT)
      xmlConfiguration.getIndentPrefixes(sourceViewer, contentType);
    else
      htmlConfiguration.getIndentPrefixes(sourceViewer, contentType);
  }

  override def getLineStyleProviders(sourceViewer: ISourceViewer, partitionType: String) = {
    ContentType(sourceViewer, partitionType) match {
      case HTMLContent() => htmlConfiguration.getLineStyleProviders(sourceViewer, partitionType)
      case XMLContent() => xmlConfiguration.getLineStyleProviders(sourceViewer, partitionType)
      case ScalaContent() => Array(new ScalaLineStyleProvider(prefStore))
      case _ => super.getLineStyleProviders(sourceViewer, partitionType)
    }
  }

  override def getConfiguredContentTypes(sourceViewer: ISourceViewer): Array[String] = {
    (htmlConfiguration.getConfiguredContentTypes(sourceViewer) ++
     xmlConfiguration.getConfiguredContentTypes(sourceViewer) ++
     scalaConfiguration.getConfiguredContentTypes(sourceViewer)).toSet.toArray
  }

  override def getAutoEditStrategies(sourceViewer: ISourceViewer, contentType: String) = ContentType(sourceViewer, contentType) match {
    case HTMLContent()  => htmlConfiguration.getAutoEditStrategies(sourceViewer, contentType)
    case XMLContent()   => xmlConfiguration.getAutoEditStrategies(sourceViewer, contentType)
    case ScalaContent() => scalaConfiguration.getAutoEditStrategies(sourceViewer, contentType)
    case _              => super.getAutoEditStrategies(sourceViewer, contentType)
  }

  override protected def getHyperlinkDetectorTargets(sourceViewer: ISourceViewer) = {
    val targets: java.util.Map[String, IAdaptable] = super.getHyperlinkDetectorTargets(sourceViewer).asInstanceOf[java.util.Map[String, IAdaptable]]
    targets.put(ContentTypeIdForHTML.ContentTypeID_HTML, null)
    targets.put(ContentTypeIdForXML.ContentTypeID_XML, null)
    targets.put(ContentTypeIdForScala.ContentTypeID_Scala, null)
    targets
  }
}

class TemplateStructuredEditor extends StructuredTextEditor with TTemplateEditor {
  
  override protected lazy val preferenceStore: IPreferenceStore = new ChainedPreferenceStore(Array((EditorsUI.getPreferenceStore()), PlayPlugin.preferenceStore))
  
  override def setSourceViewerConfiguration(config: SourceViewerConfiguration) = {
    config match {
      case templateConfig: TemplateStructuredTextViewerConfiguration => {
        super.setSourceViewerConfiguration(new TemplateStructuredTextViewerConfiguration(preferenceStore, this))
      }
      case _ => super.setSourceViewerConfiguration(config)
    }
  }
}

