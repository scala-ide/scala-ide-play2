package org.scalaide.play2.routeeditor.lexical

import scala.tools.eclipse.lexical.AbstractScalaScanner
import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass

import org.eclipse.jdt.ui.text.IColorManager
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.rules.RuleBasedScanner

abstract class AbstractRouteScanner(defaultSyntax: ScalaSyntaxClass, prefStore: IPreferenceStore, manager: IColorManager) extends RuleBasedScanner with AbstractScalaScanner {
  fDefaultReturnToken = getToken(defaultSyntax)
  override def preferenceStore = prefStore
  override def colorManager = manager

  def getDefaultReturnToken = fDefaultReturnToken

}
