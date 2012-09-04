package org.scalaide.play2.templateeditor.lexical

import org.scalaide.play2.lexical.PlayDocumentPartitioner

import TemplatePartitions.TEMPLATE_COMMENT
import TemplatePartitions.TEMPLATE_DEFAULT
import TemplatePartitions.TEMPLATE_SCALA

class TemplateDocumentPartitioner(conservative: Boolean = false) extends PlayDocumentPartitioner(TemplatePartitionTokeniser, TemplatePartitions.TEMPLATE_DEFAULT, conservative) {

  import TemplateDocumentPartitioner._

  def getLegalContentTypes = LEGAL_CONTENT_TYPES

}

object TemplateDocumentPartitioner {

  private val LEGAL_CONTENT_TYPES = Array[String](
    TEMPLATE_DEFAULT, TEMPLATE_SCALA, TEMPLATE_COMMENT)

  val NO_PARTITION_AT_ALL = "__no_partition_at_all"

  final val EOF = '\u001A'

}

