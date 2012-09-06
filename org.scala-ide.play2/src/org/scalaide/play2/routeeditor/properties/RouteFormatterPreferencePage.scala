package org.scalaide.play2.routeeditor.properties

import scala.PartialFunction.condOpt
import scala.tools.eclipse.properties.syntaxcolouring.GridDataHelper.gridData
import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass
import scala.tools.eclipse.util.EclipseUtils
import scala.tools.eclipse.util.SWTUtils.fnToDoubleClickListener
import scala.tools.eclipse.util.SWTUtils.fnToPropertyChangeListener
import scala.tools.eclipse.util.SWTUtils.fnToSelectionAdapter
import scala.tools.eclipse.util.SWTUtils.noArgFnToSelectionAdapter
import scala.tools.eclipse.util.SWTUtils.noArgFnToSelectionChangedListener
import org.eclipse.jdt.internal.ui.preferences.OverlayPreferenceStore
import org.eclipse.jdt.internal.ui.preferences.OverlayPreferenceStore.BOOLEAN
import org.eclipse.jdt.internal.ui.preferences.OverlayPreferenceStore.OverlayKey
import org.eclipse.jdt.internal.ui.preferences.OverlayPreferenceStore.INT
import org.eclipse.jdt.internal.ui.preferences.PreferencesMessages
import org.eclipse.jdt.internal.ui.preferences.ScrolledPageContent
import org.eclipse.jface.layout.PixelConverter
import org.eclipse.jface.preference.ColorSelector
import org.eclipse.jface.preference.PreferenceConverter
import org.eclipse.jface.preference.PreferencePage
import org.eclipse.jface.text.source.SourceViewer
import org.eclipse.jface.util.PropertyChangeEvent
import org.eclipse.jface.viewers.DoubleClickEvent
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Link
import org.eclipse.swt.widgets.Scrollable
import org.eclipse.ui.IWorkbench
import org.eclipse.ui.IWorkbenchPreferencePage
import org.eclipse.ui.dialogs.PreferencesUtil
import org.scalaide.play2.PlayPlugin
import org.eclipse.ui.texteditor.ChainedPreferenceStore
import org.eclipse.ui.editors.text.EditorsUI
import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.jface.preference.FieldEditorPreferencePage
import org.eclipse.jface.preference.IntegerFieldEditor

class RouteFormatterPreferencePage extends FieldEditorPreferencePage with IWorkbenchPreferencePage {

  setPreferenceStore(PlayPlugin.plugin.getPreferenceStore)
  
  override def createFieldEditors() {
    val marginField = new IntegerFieldEditor(PlayPlugin.plugin.routeFormatterMarginId, "Number of spaces between columns", getFieldEditorParent)
    marginField.setValidRange(1, 10)
    addField(marginField)
  }

  def init(workbench: IWorkbench) {}

}