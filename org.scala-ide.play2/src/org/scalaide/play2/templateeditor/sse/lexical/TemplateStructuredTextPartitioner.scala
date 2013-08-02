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
import org.eclipse.jface.text.IDocumentPartitionerExtension2
import org.eclipse.jface.text.ITypedRegion

class TemplateStructuredTextPartitioner extends StructuredTextPartitioner with IDocumentPartitionerExtension2 {
  
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
      case scalaRegion: TemplateTextRegion => scalaRegion.syntaxClass match {
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
  
  /* IDocumentPartitionerExtension2 methods */
  
  override def getManagingPositionCategories(): Array[String] = null
  
  override def getContentType(offset: Int, preferOpenPartitions: Boolean): String = 
    getPartition(offset, preferOpenPartitions).getType()
  
  override def computePartitioning(offset: Int, length: Int, includeZeroLengthPartitions: Boolean): Array[ITypedRegion] =
    computePartitioning(offset, length)
  
  override def getPartition(offset: Int, preferOpenPartitions: Boolean): ITypedRegion = {
    val partition = getPartition(offset)
    if (preferOpenPartitions && partition.getOffset() == offset && offset > 0) {
      getPartition(offset - 1)
    }
    else partition
  }
}