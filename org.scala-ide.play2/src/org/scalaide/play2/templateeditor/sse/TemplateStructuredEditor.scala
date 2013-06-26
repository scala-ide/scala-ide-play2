package org.scalaide.play2.templateeditor.sse

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.wst.sse.ui.StructuredTextEditor
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.templateeditor.TTemplateEditor


class TemplateStructuredEditor extends StructuredTextEditor with TTemplateEditor {
  
  override protected lazy val preferenceStore: IPreferenceStore = new ChainedPreferenceStore(Array((EditorsUI.getPreferenceStore()), PlayPlugin.preferenceStore))
  
  override def setSourceViewerConfiguration(config: SourceViewerConfiguration) = {
    config match {
      case templateConfig: TemplateStructuredTextViewerConfiguration => {
        super.setSourceViewerConfiguration(new TemplateStructuredTextViewerConfiguration(preferenceStore, this))
      }
      case _ => super.setSourceViewerConfiguration(config)
    }
  }
}

