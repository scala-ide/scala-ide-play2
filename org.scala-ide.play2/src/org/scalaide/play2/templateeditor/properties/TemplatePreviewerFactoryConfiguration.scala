package org.scalaide.play2.templateeditor.properties

import org.eclipse.jface.preference.IPreferenceStore
import org.scalaide.play2.templateeditor.TemplateConfiguration
import org.scalaide.play2.templateeditor.lexical.TemplateDocumentPartitioner
import org.scalaide.ui.syntax.preferences.PreviewerFactoryConfiguration
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions
import java.util.HashMap
import org.eclipse.jface.text.IDocumentPartitioner
import org.eclipse.jface.text.IDocumentExtension3

object TemplatePreviewerFactoryConfiguration extends PreviewerFactoryConfiguration {
  
  def getConfiguration(preferenceStore: IPreferenceStore) = 
    new TemplateConfiguration(preferenceStore, null)
  
   def getDocumentPartitioners(): Map[String, IDocumentPartitioner] = 
     Map((IDocumentExtension3.DEFAULT_PARTITIONING, new TemplateDocumentPartitioner(true)))

}