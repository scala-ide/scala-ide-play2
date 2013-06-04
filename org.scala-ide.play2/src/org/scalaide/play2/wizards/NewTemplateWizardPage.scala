package org.scalaide.play2.wizards

import java.io.ByteArrayInputStream
import java.io.InputStream
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.dialogs.WizardNewFileCreationPage
import org.scalaide.play2.PlayPlugin

/**
 * Wizard page based of the new file creation page from the framework.
 * The advanced section has be removed.
 */
class NewTemplateWizardPage(selection: IStructuredSelection) extends WizardNewFileCreationPage("main", selection) {

  // from org.eclipse.ui.dialogs.WizardNewFileCreationPage

  override def createAdvancedControls(parent: Composite) {
    // do nothing, we don't want the 'advanced' section
  }

  override def createLinkTarget() {
    // do nothing, we don't have this section
  }

  override def getInitialContents(): InputStream = {
    new ByteArrayInputStream(
      """@* %s Template File *@
@(param: Any)
"""
        .format(objectName).getBytes())
  }

  override def validateLinkedResource(): IStatus = {
    // do nothing, we don't have this section
    Status.OK_STATUS
  }
  
  override def validatePage(): Boolean = {
    if (super.validatePage()) {
      if (validateIdentifier) {
        true
      } else {
        setErrorMessage("Invalid template name")
        false
      }
    } else {
      false
    }
  }

  // ------

  // set the page values
  setTitle("Play Template")
  setDescription("Create a new Play Template")
  setFileExtension(PlayPlugin.TemplateExtension)

  /**
   * Return the name of the object to be created, or empty if not available.
   */
  def objectName = {
    val fileName = getFileName()
    val extenstionLength = ("." + PlayPlugin.TemplateExtension).length()
    if (fileName.length > extenstionLength) {
    	fileName.substring(0, fileName.length() - extenstionLength)
    } else {
      ""
    }
  }
  
  /**
   * check if the object name is a valid identifier.
   * TODO: switch to Scala identifier check. Right now it is checking for a Java identifier
   */
  def validateIdentifier: Boolean = {
    objectName.toList match {
      case Nil =>
        true
      case head :: Nil =>
        Character.isJavaIdentifierStart(head)
      case head :: tail =>
        Character.isJavaIdentifierStart(head) && tail.foldLeft(true)((b: Boolean, c: Char) => b && Character.isJavaIdentifierPart(c))
    }
  }
  
}