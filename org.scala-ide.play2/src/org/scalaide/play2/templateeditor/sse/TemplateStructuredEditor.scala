package org.scalaide.play2.templateeditor.sse

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.source.SourceViewerConfiguration
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.wst.sse.ui.StructuredTextEditor
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.templateeditor.AbstractTemplateEditor


class TemplateStructuredEditor extends StructuredTextEditor with AbstractTemplateEditor {
  
  override protected lazy val preferenceStore: IPreferenceStore =
    new ChainedPreferenceStore(Array((EditorsUI.getPreferenceStore()), PlayPlugin.preferenceStore))
  
  /* This is a nasty hack. 
   * The problem:  The TemplateStructuredTextViewerConfiguration needs the pref store and a reference to the editor.
   *               However, the viewer configuration is instantiated through an extension point, so we don't have the opportunity to give it the pref store and a reference to the editor.
   * The solution: Intercept the instance of the TemplateStructuredTextViewerConfiguration and inject it/create a new one with the 
   *               pref store and reference to the editor (self) 
   * Note: the TemplateStructuredTextViewerConfiguration has additional logic to support this hack.
   */
  override def setSourceViewerConfiguration(config: SourceViewerConfiguration) = {
    config match {
      case templateConfig: TemplateStructuredTextViewerConfiguration => {
        templateConfig.editor = this
        templateConfig.prefStore = preferenceStore
      }
      case _ =>
    }
    super.setSourceViewerConfiguration(config)
  }
}

