package org.scalaide.play2.templateeditor

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.source.IOverviewRuler
import org.eclipse.jface.text.source.IVerticalRuler
import org.eclipse.jface.text.source.projection.ProjectionViewer
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.swt.widgets.Composite
import org.eclipse.jface.text.source.SourceViewer

class TemplateSourceViewer(parent: Composite, ruler: IVerticalRuler, overviewRuler: IOverviewRuler, showsAnnotationOverview: Boolean, styles: Int, store: IPreferenceStore) extends /*ProjectionViewer(parent, ruler, overviewRuler, showsAnnotationOverview, styles) */SourceViewer(parent, ruler, null, false/*overviewRuler, showsAnnotationOverview*/, styles) /* with IPropertyChangeListener */{

  
  /*override def propertyChange(event: PropertyChangeEvent) = {
    invalidateTextPresentation()
  }*/
  
}