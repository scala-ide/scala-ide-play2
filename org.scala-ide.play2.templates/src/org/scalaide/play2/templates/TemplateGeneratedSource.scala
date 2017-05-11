package org.scalaide.play2.templates

import org.scalaide.play2.templateeditor.processing.GeneratedSource

import play.twirl.compiler.GeneratedSourceVirtual

case class TemplateGeneratedSource(wrapped: GeneratedSourceVirtual) extends GeneratedSource {
  override def content: String = wrapped.content
  override def matrix: Seq[(Int, Int)] = wrapped.matrix
  override def mapPosition(generatedPosition: Int): Int = wrapped.mapPosition(generatedPosition)
}
