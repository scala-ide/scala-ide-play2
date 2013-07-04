package org.scalaide.play2.wizards

import scala.tools.eclipse.logging.HasLogger
import scala.tools.eclipse.util.SWTUtils

import org.eclipse.jface.viewers.IStructuredSelection

import org.eclipse.jface.wizard.Wizard
import org.eclipse.ui.INewWizard
import org.eclipse.ui.IWorkbench
import org.eclipse.ui.PartInitException
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.ide.IDE
import org.scalaide.play2.templateeditor.TemplateEditor

/**
 * A wizard to create a new Play template file.
 */
class NewTemplateWizard extends Wizard with INewWizard with HasLogger {

  // from org.eclipse.jface.wizard.Wizard

  override def performFinish(): Boolean = {
    val file = newFileWizardPage.createNewFile()
    
    if (file != null) {
      // if it worked, open the file
      SWTUtils.asyncExec {
        val page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
        try {
          val editor = IDE.openEditor(page, file, true)
        } catch {
          case e: PartInitException => eclipseLog.error("Failed to initialize editor for file "+ file.getName())
        }
      }
      true
    } else {
      false
    }
  }

  override def addPages() {
    newFileWizardPage = new NewTemplateWizardPage(selection)
    addPage(newFileWizardPage)
  }

  // from org.eclipse.ui.INewWizard

  override def init(workbench: IWorkbench, selection: IStructuredSelection) {
    this.selection = selection
  }

  // ------
  
  // set the dialog values
  setWindowTitle("New Play Template")

  /**
   * The wizard page
   */
  private var newFileWizardPage: NewTemplateWizardPage = _

  /**
   * The selection at the initialization of the wizard
   */
  private var selection: IStructuredSelection = _

}