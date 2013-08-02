package org.scalaide.play2.templateeditor.lexical

import org.scalaide.play2.lexical.PlayDocumentPartitioner
import TemplatePartitions.TEMPLATE_COMMENT
import TemplatePartitions.TEMPLATE_DEFAULT
import TemplatePartitions.TEMPLATE_SCALA
import org.eclipse.jface.text.ITypedRegion

class TemplateDocumentPartitioner(conservative: Boolean = false) extends PlayDocumentPartitioner(new TemplatePartitionTokeniser, TemplatePartitions.TEMPLATE_DEFAULT, conservative) {

  import TemplateDocumentPartitioner._

  def getLegalContentTypes = LEGAL_CONTENT_TYPES

  override def getPartition(offset: Int, preferOpenPartitions: Boolean): ITypedRegion = {
    val region = super.getPartition(offset, preferOpenPartitions)
    if (preferOpenPartitions)
      if (region.getOffset == offset && region.getType != defaultPartition)
        if (offset > 0) {
          val previousRegion = getPartition(offset - 1)
          // FIXME: This doesn't make any sense to me. We should prioritize the previous 
          //        partition only if it's meaningful, i.e., if it is NOT the default partitioning. 
          //        Hence, I'm wondering if the template editor actually relies on this logic, as 
          //        it seems pretty useless and could actually cause some completion proposals to 
          //        no show up because the default partitioning (instead of a specific one) is returned.
          if (previousRegion.getType == defaultPartition)
            return previousRegion
        }
    region
  }
}

object TemplateDocumentPartitioner {

  private val LEGAL_CONTENT_TYPES = Array[String](
    TEMPLATE_DEFAULT, TEMPLATE_SCALA, TEMPLATE_COMMENT)

  val NO_PARTITION_AT_ALL = "__no_partition_at_all"

  final val EOF = '\u001A'

}

