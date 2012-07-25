package org.scalaide.play2.routeeditor

import org.eclipse.ui.editors.text.TextEditor

class RouteEditor extends TextEditor {
  private val colorManager = ColorManager.colorManager;
  setSourceViewerConfiguration(new RouteConfiguration(colorManager, this));
  setDocumentProvider(new RouteDocumentProvider());

  override def dispose() = {
    colorManager.dispose();
    super.dispose();
  }
}