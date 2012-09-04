package org.scalaide.play2.properties

import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.LabelProvider

trait PropertyChangeHandler {
  def handlePropertyChangeEvent(event: PropertyChangeEvent)
}

object PlayTypes {
  type PlaySourceViewer = SourceViewerConfiguration with PropertyChangeHandler
  type LabelContentProvider = LabelProvider with ITreeContentProvider
}