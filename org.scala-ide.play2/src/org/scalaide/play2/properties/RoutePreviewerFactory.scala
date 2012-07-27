package org.scalaide.play2.properties

import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.resource.JFaceResources
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.source.SourceViewer
import org.eclipse.jface.text.source.projection.ProjectionViewer
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.scalaide.play2.routeeditor.RouteConfiguration
import org.scalaide.play2.routeeditor.scanners.RouteDocumentPartitioner

object RoutePreviewerFactory {

  def createPreviewer(parent: Composite, scalaPreferenceStore: IPreferenceStore, initialText: String): SourceViewer = {
    val preferenceStore = new ChainedPreferenceStore(Array(scalaPreferenceStore, EditorsUI.getPreferenceStore))
    //    val previewViewer = new JavaSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER, preferenceStore)
    val previewViewer = new ProjectionViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER)
    val font = JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT)
    previewViewer.getTextWidget.setFont(font)
    previewViewer.setEditable(false)

    //    val configuration = new ScalaSourceViewerConfiguration(preferenceStore, preferenceStore, null)
    val configuration = new RouteConfiguration(preferenceStore, null)
    previewViewer.configure(configuration)

    val document = new Document
    document.set(initialText)
    //    val partitioners = new HashMap[String, IDocumentPartitioner]
    //    partitioners.put(RoutePartitions.ROUTE_PARTITIONING, new RouteDocumentPartitioner())
    //    TextUtilities.addDocumentPartitioners(document, partitioners)
    val partitioner = new RouteDocumentPartitioner()
    partitioner.connect(document)
    document.setDocumentPartitioner(partitioner)
    previewViewer.setDocument(document)

    preferenceStore.addPropertyChangeListener(new IPropertyChangeListener {
      def propertyChange(event: PropertyChangeEvent) {
        configuration.handlePropertyChangeEvent(event)
        previewViewer.invalidateTextPresentation()
      }
    })
    previewViewer
  }

}