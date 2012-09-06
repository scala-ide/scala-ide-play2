package org.scalaide.play2.templateeditor

import scala.tools.eclipse.util.SWTUtils.fnToPropertyChangeListener
import org.eclipse.jdt.internal.ui.text.java.hover.SourceViewerInformationControl
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.IInformationControlCreator
import org.eclipse.jface.text.source.IOverviewRuler
import org.eclipse.jface.text.source.IVerticalRuler
import org.eclipse.jface.text.source.projection.ProjectionSupport
import org.eclipse.jface.text.source.projection.ProjectionViewer
import org.eclipse.jface.util.IPropertyChangeListener
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Shell
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.ui.editors.text.TextEditor
import org.eclipse.ui.texteditor.AnnotationPreference
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences
import org.scalaide.play2.PlayPlugin
import scala.tools.eclipse.ISourceViewerEditor
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.jdt.internal.ui.JavaPlugin
import scala.tools.eclipse.ui.InteractiveCompilationUnitEditor
import scala.tools.eclipse.InteractiveCompilationUnit

class TemplateEditor extends TextEditor with ISourceViewerEditor with InteractiveCompilationUnitEditor {
//  var fProjectionSupport: ProjectionSupport = _
  lazy val preferenceStore = new ChainedPreferenceStore(Array((EditorsUI.getPreferenceStore()), PlayPlugin.prefStore))
//  val prefStore = PlayPlugin.prefStore
  val sourceViewConfiguration = new TemplateConfiguration(preferenceStore, this)
  val documentProvider = new TemplateDocumentProvider()
  
  setSourceViewerConfiguration(sourceViewConfiguration);
  setPreferenceStore(preferenceStore)
  setDocumentProvider(documentProvider);

  override def dispose() = {
    super.dispose();
    PlayPlugin.prefStore.removePropertyChangeListener(preferenceListener)
//    if (fProjectionSupport != null) {
//      fProjectionSupport.dispose
//    }
  }

  private val preferenceListener: IPropertyChangeListener = handlePreferenceStoreChanged _

  override def handlePreferenceStoreChanged(event: PropertyChangeEvent) = {
    sourceViewConfiguration.handlePropertyChangeEvent(event)
    getSourceViewer().invalidateTextPresentation
  }

  PlayPlugin.prefStore.addPropertyChangeListener(preferenceListener)

  override def createSourceViewer(parent: Composite, verticalRuler: IVerticalRuler, styles: Int) = {
    super.createSourceViewer(parent, verticalRuler, styles)
  }
  
  override def editorSaved() = {
    super.editorSaved()
//    sourceViewConfiguration.getReconciler(getSourceViewer()).getReconcilingStrategy("").reconcile(null)
    sourceViewConfiguration.strategy.reconcile(null)
  }
  
  
  def getViewer: ISourceViewer = getSourceViewer
  
  override def getInteractiveCompilationUnit(): Option[InteractiveCompilationUnit] = TemplateCompilationUnit.fromEditor(this)

//  override def createSourceViewer(parent: Composite, verticalRuler: IVerticalRuler, styles: Int) = {
//    val composite = new Composite(parent, SWT.NONE);
//    val layout = new GridLayout(1, false);
//    layout.marginHeight = 0;
//    layout.marginWidth = 0;
//    layout.horizontalSpacing = 0;
//    layout.verticalSpacing = 0;
//    composite.setLayout(layout);
//
//    val editorComposite = new Composite(composite, SWT.NONE);
//    editorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//    val fillLayout = new FillLayout(SWT.VERTICAL);
//    fillLayout.marginHeight = 0;
//    fillLayout.marginWidth = 0;
//    fillLayout.spacing = 0;
//    editorComposite.setLayout(fillLayout);
//
//    val store = getPreferenceStore();
//    val sourceViewer = createTemplateSourceViewer(editorComposite, verticalRuler, getOverviewRuler(), isOverviewRulerVisible(), styles, store);
//
//    if (sourceViewer.isInstanceOf[ProjectionViewer]) {
//      fProjectionSupport = new ProjectionSupport(sourceViewer, getAnnotationAccess(), getSharedColors());
//      val markerAnnotationPreferences = getAdapter(classOf[MarkerAnnotationPreferences]).asInstanceOf[MarkerAnnotationPreferences];
//      if (markerAnnotationPreferences != null) {
//        val e = markerAnnotationPreferences.getAnnotationPreferences().iterator();
//        while (e.hasNext()) {
//          val annotationPreference = e.next().asInstanceOf[AnnotationPreference];
//          val annotationType = annotationPreference.getAnnotationType();
//          if (annotationType.isInstanceOf[String])
//            fProjectionSupport.addSummarizableAnnotationType(annotationType.asInstanceOf[String]);
//        }
//      } else {
//        fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
//        fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
//      }
//      fProjectionSupport.setHoverControlCreator(new IInformationControlCreator() {
//        override def createInformationControl(shell: Shell) = {
//          new SourceViewerInformationControl(shell, false, getOrientation(), EditorsUI.getTooltipAffordanceString());
//        }
//      });
//      fProjectionSupport.setInformationPresenterControlCreator(new IInformationControlCreator() {
//        override def createInformationControl(shell: Shell) = {
//          new SourceViewerInformationControl(shell, true, getOrientation(), null);
//        }
//      });
//      fProjectionSupport.install();
//
//    }
//
//    // ensure source viewer decoration support has been created and configured
//    getSourceViewerDecorationSupport(sourceViewer);
//    sourceViewer
//  }
//
//  def createTemplateSourceViewer(parent: Composite, ruler: IVerticalRuler, overviewRuler: IOverviewRuler, showsAnnotationOverview: Boolean, styles: Int, store: IPreferenceStore) = {
//    new TemplateSourceViewer(parent, ruler, overviewRuler, showsAnnotationOverview, styles, store)
//  }

}