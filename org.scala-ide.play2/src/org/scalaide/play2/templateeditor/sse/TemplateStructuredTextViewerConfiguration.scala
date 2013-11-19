package org.scalaide.play2.templateeditor.sse

import org.eclipse.core.runtime.IAdaptable
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.wst.html.core.internal.provisional.contenttype.ContentTypeIdForHTML
import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML
import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration
import org.eclipse.wst.xml.core.internal.provisional.contenttype.ContentTypeIdForXML
import org.eclipse.wst.xml.core.text.IXMLPartitions
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML
import org.scalaide.play2.templateeditor.AbstractTemplateEditor
import org.scalaide.play2.templateeditor.TemplateConfiguration
import org.scalaide.play2.templateeditor.sse.style.ScalaLineStyleProvider

// According to http://www.eclipsezone.com/eclipse/forums/t73617.html#92026455 in the SourceViewer and SourceViewerConfiguration hierarchy
// "content type" actually means "partition type"
class TemplateStructuredTextViewerConfiguration extends StructuredTextViewerConfiguration {

  // public and mutable so that the TemplateStructuredEditor can inject the values
  @volatile private var prefStore: IPreferenceStore = null
  private val htmlConfiguration = new StructuredTextViewerConfigurationHTML
  private val xmlConfiguration = new StructuredTextViewerConfigurationXML
  // must be lazy because creation depends on injected prefStore and editor fields
  private var scalaConfiguration: Option[TemplateConfiguration] = None

  private sealed trait ContentType
  private case object HTMLContent extends ContentType
  private case object XMLContent extends ContentType
  private case object ScalaContent extends ContentType
  private case object DefaultContent extends ContentType

  private object ContentType {
    def apply(sourceViewer: ISourceViewer, contentType: String) = {
      var htmlContentTypes = htmlConfiguration.getConfiguredContentTypes(sourceViewer).toSet
      val xmlContentTypes = xmlConfiguration.getConfiguredContentTypes(sourceViewer).toSet
      htmlContentTypes = htmlContentTypes -- xmlContentTypes
      val scalaPartitions = (scalaConfiguration.map(_.getConfiguredContentTypes(sourceViewer)) getOrElse (Array[String]())).toSet
      if (htmlContentTypes contains contentType) HTMLContent
      else if (xmlContentTypes contains contentType) XMLContent
      else if (scalaPartitions contains contentType) ScalaContent
      else DefaultContent
    }
  }
  
  def initialize(prefStore: IPreferenceStore, editor: AbstractTemplateEditor) = {
    this.prefStore = prefStore
    scalaConfiguration = Some(new TemplateConfiguration(prefStore, editor))
  }
  
  override def getDoubleClickStrategy(sourceViewer: ISourceViewer, contentType: String) = {
    lazy val default = super.getDoubleClickStrategy(sourceViewer, contentType)
    ContentType(sourceViewer, contentType) match {
      case HTMLContent => htmlConfiguration.getDoubleClickStrategy(sourceViewer, contentType)
      case XMLContent => xmlConfiguration.getDoubleClickStrategy(sourceViewer, contentType)
      case ScalaContent => scalaConfiguration.map(_.getDoubleClickStrategy(sourceViewer, contentType)) getOrElse default 
      case _ => default
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
      case HTMLContent => htmlConfiguration.getLineStyleProviders(sourceViewer, partitionType)
      case XMLContent => xmlConfiguration.getLineStyleProviders(sourceViewer, partitionType)
      case ScalaContent => Array(new ScalaLineStyleProvider(prefStore))
      case _ => super.getLineStyleProviders(sourceViewer, partitionType)
    }
  }

  override def getConfiguredContentTypes(sourceViewer: ISourceViewer): Array[String] = {
    (htmlConfiguration.getConfiguredContentTypes(sourceViewer) ++
     xmlConfiguration.getConfiguredContentTypes(sourceViewer) ++
     scalaConfiguration.map(_.getConfiguredContentTypes(sourceViewer)).getOrElse(Array[String]())).toSet.toArray
  }

  override def getAutoEditStrategies(sourceViewer: ISourceViewer, contentType: String) = {
    lazy val default = super.getAutoEditStrategies(sourceViewer, contentType)
    ContentType(sourceViewer, contentType) match {
      case HTMLContent => htmlConfiguration.getAutoEditStrategies(sourceViewer, contentType)
      case XMLContent => xmlConfiguration.getAutoEditStrategies(sourceViewer, contentType)
      case ScalaContent => scalaConfiguration.map(_.getAutoEditStrategies(sourceViewer, contentType)) getOrElse default
      case _ => default
    }
  }

  override protected def getHyperlinkDetectorTargets(sourceViewer: ISourceViewer) = {
    val targets: java.util.Map[String, IAdaptable] = super.getHyperlinkDetectorTargets(sourceViewer).asInstanceOf[java.util.Map[String, IAdaptable]]
    targets.put(ContentTypeIdForHTML.ContentTypeID_HTML, null)
    targets.put(ContentTypeIdForXML.ContentTypeID_XML, null)
    targets.put(ContentTypeIdForScala.ContentTypeID_Scala, null)
    targets
  }
}
