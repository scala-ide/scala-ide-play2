package org.scalaide.play2.properties

import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.jface.viewers.ITreeContentProvider

object PlayTypes {
  type PlaySourceViewer = SourceViewerConfiguration with PropertyChangeHandler
  type LabelContentProvider = LabelProvider with ITreeContentProvider
}