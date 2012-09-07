package org.scalaide.play2.routeeditor.properties

import org.eclipse.jface.preference.IPreferenceStore
import org.scalaide.play2.properties.PreviewerFactory
import org.scalaide.play2.routeeditor.RouteConfiguration
import org.scalaide.play2.routeeditor.lexical.RouteDocumentPartitioner

object RoutePreviewerFactory extends PreviewerFactory {
  
  def getConfiguration(preferenceStore: IPreferenceStore) = 
    new RouteConfiguration(preferenceStore, null)
  
   def getDocumentPartitioner() = 
     new RouteDocumentPartitioner(true)

}