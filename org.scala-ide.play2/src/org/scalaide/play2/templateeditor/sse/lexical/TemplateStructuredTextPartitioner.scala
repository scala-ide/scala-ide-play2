package org.scalaide.play2.templateeditor.sse.lexical

import org.eclipse.jface.text.DocumentEvent
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IDocumentPartitioner
import org.eclipse.wst.html.core.internal.text.StructuredTextPartitionerForHTML
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.sse.core.internal.text.rules.StructuredTextPartitioner
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses
import org.scalaide.play2.templateeditor.lexical.TemplateDocumentPartitioner
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions

class TemplateStructuredTextPartitioner extends StructuredTextPartitioner {
  
  private val htmlPartitioner: StructuredTextPartitionerForHTML = new StructuredTextPartitionerForHTML
  
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
    instance.connect(fStructuredDocument)
    instance
  }
}