package org.scalaide.play2.routeeditor.properties

import org.eclipse.jface.preference.IPreferenceStore
import org.scalaide.ui.syntax.preferences.PreviewerFactoryConfiguration
import org.scalaide.play2.routeeditor.RouteConfiguration
import org.scalaide.play2.routeeditor.lexical.RouteDocumentPartitioner
import java.util.HashMap
import org.eclipse.jface.text.IDocumentPartitioner
import org.scalaide.play2.routeeditor.lexical.RoutePartitions
import org.eclipse.jface.text.IDocumentExtension3

object RoutePreviewerFactoryConfiguration extends PreviewerFactoryConfiguration {
  
  def getConfiguration(preferenceStore: IPreferenceStore) = 
    new RouteConfiguration(preferenceStore, null)
  
   def getDocumentPartitioners(): Map[String, IDocumentPartitioner] =
    Map((IDocumentExtension3.DEFAULT_PARTITIONING, new RouteDocumentPartitioner(true)))

}