package org.scalaide.play2.templateeditor.processing

object TemplateVersionExhibitor {
  private val templateVersionStock: ThreadLocal[Option[String]] =
    new ThreadLocal[Option[String]] {
      override protected def initialValue: Option[String] = None
    }

  def get: Option[String] = templateVersionStock.get

  def set(templateVersion: Option[String]): Unit = templateVersionStock.set(templateVersion)

  def clean(): Unit = templateVersionStock.remove()
}
