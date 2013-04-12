package org.scalaide.play2.properties

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IAdaptable
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jface.dialogs.IInputValidator
import org.eclipse.jface.dialogs.InputDialog
import org.eclipse.jface.preference.FieldEditorPreferencePage
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.preference.ListEditor
import org.eclipse.jface.window.Window
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.IWorkbenchPropertyPage
import org.scalaide.play2.PlayPlugin

/** Preference page displayed in the property dialog of (play) projects.
 *  Used from the UI thread.
 */
class ProjectPropertyPage extends FieldEditorPreferencePage(FieldEditorPreferencePage.GRID) with IWorkbenchPropertyPage {

  /** Preference field to display the list of extra imports.
   */
  private class ImportsFieldEditor(name: String, labelText: String, parent: Composite) extends ListEditor(name, labelText, parent) {

    override protected def createList(entries: Array[String]): String =
      PlayPreferences.serializeImports(entries)

    override protected def parseString(s: String): Array[String] =
      PlayPreferences.deserializeImports(s)

    override protected def getNewInputObject(): String = {

      val dlg = new InputDialog(
        Display.getCurrent().getActiveShell(),
        "Play template import",
        "Enter an import value:",
        "com.example._",
        new IInputValidator {
          def isValid(text: String) = null
        });

      if (dlg.open() == Window.OK) {
        dlg.getValue()
      } else {
        null
      }
    }

  }

  // The preference store being edited.
  // The data require to get the store is provided by the workbench during the page lifecycle.
  private var prefStore: IPreferenceStore = _

  // Members declared in org.eclipse.jface.preference.FieldEditorPreferencePage

  override def createFieldEditors() {
    addField(new ImportsFieldEditor(PlayPreferences.TemplateImports, "Template default imports", getFieldEditorParent()))
  }

  // Members declared in org.eclipse.ui.IWorkbenchPropertyPage

  // doesn't seem to be a real function for this method.
  // It looks like it leaked from the implementation of PropertyPage.
  override def getElement(): IAdaptable = null

  override def setElement(element: IAdaptable) {
    prefStore = element match {
      case project: IProject =>
        PlayPlugin.instance().asPlayProject(project).get.generateScopedPreferenceStore
      case project: IJavaProject =>
        PlayPlugin.instance().asPlayProject(project.getProject()).get.generateScopedPreferenceStore
    }
  }

  // ----

  override def doGetPreferenceStore(): IPreferenceStore = {
    prefStore
  }

}