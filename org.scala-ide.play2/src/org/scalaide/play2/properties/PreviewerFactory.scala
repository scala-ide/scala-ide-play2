package org.scalaide.play2.properties

import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.resource.JFaceResources
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocumentPartitioner
import org.eclipse.jface.text.source.SourceViewer
import org.eclipse.jface.text.source.projection.ProjectionViewer
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.texteditor.ChainedPreferenceStore

trait PreviewerFactory extends IPropertyChangeListener {
  var preferenceStore: ChainedPreferenceStore = _
  var previewViewer: ProjectionViewer = _
  var configuration: PlayTypes.PlaySourceViewer = _

  def getConfiguration(preferenceStore: IPreferenceStore): PlayTypes.PlaySourceViewer

  def getDocumentPartitioner(): IDocumentPartitioner

  def createPreviewer(parent: Composite, playPreferenceStore: IPreferenceStore, initialText: String): SourceViewer = {
    preferenceStore = new ChainedPreferenceStore(Array(playPreferenceStore, EditorsUI.getPreferenceStore))
    previewViewer = new ProjectionViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER)
    configuration = getConfiguration(preferenceStore)
    val font = JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT)
    previewViewer.getTextWidget.setFont(font)
    previewViewer.setEditable(false)
    previewViewer.configure(configuration)

    val document = new Document
    document.set(initialText)

    val partitioner = getDocumentPartitioner
    partitioner.connect(document)
    document.setDocumentPartitioner(partitioner)
    previewViewer.setDocument(document)

    preferenceStore.addPropertyChangeListener(this)
    previewViewer
  }
  
  def disposePreviewer() {
    preferenceStore.removePropertyChangeListener(this)
  }

  def propertyChange(event: PropertyChangeEvent) {
    configuration.handlePropertyChangeEvent(event)
    previewViewer.invalidateTextPresentation()
  }

}