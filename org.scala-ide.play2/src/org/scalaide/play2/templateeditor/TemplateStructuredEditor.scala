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
import org.scalaide.play2.templateeditor.sse.lexical.ScalaTextRegion
import org.scalaide.play2.templateeditor.sse.lexical.TemplateRegionParser
import org.scalaide.play2.templateeditor.sse.lexical.TemplateStructuredTextPartitioner

object ContentTypeIdForScala {
  val ContentTypeID_Scala = "org.scalaide.play2.templateSource"
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

