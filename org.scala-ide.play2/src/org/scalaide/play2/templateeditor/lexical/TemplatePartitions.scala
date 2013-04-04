package org.scalaide.play2.templateeditor.lexical

import org.eclipse.jface.text.IDocument


object TemplatePartitions {
  val TEMPLATE_PARTITIONING = "___template_partitioning";
  val TEMPLATE_DEFAULT = IDocument.DEFAULT_CONTENT_TYPE
  val TEMPLATE_SCALA = "__template_scala"
  val TEMPLATE_COMMENT = "__template_comment"
  val TEMPLATE_PLAIN = "__template_plain"
  val TEMPLATE_TAG = "__template_tag"

  def getTypes() = {
    Array(TEMPLATE_DEFAULT, TEMPLATE_SCALA, TEMPLATE_COMMENT, TEMPLATE_TAG, TEMPLATE_PLAIN);
  }

}
