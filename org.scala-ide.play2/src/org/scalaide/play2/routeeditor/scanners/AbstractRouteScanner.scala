package org.scalaide.play2.routeeditor.scanners

import org.eclipse.jface.text.rules.IToken
import org.eclipse.jface.text.rules.RuleBasedScanner
import scala.tools.eclipse.lexical.AbstractScalaScanner
import org.eclipse.jface.preference.IPreferenceStore
import scala.tools.eclipse.semantichighlighting.ColorManager
import org.eclipse.jdt.ui.text.IColorManager
import scala.tools.eclipse.properties.syntaxcolouring.ScalaSyntaxClass

abstract class AbstractRouteScanner(defaultSyntax: ScalaSyntaxClass, prefStore: IPreferenceStore, manager: IColorManager) extends RuleBasedScanner with AbstractScalaScanner {
  fDefaultReturnToken = getToken(defaultSyntax)
  override def preferenceStore = prefStore
  override def colorManager = manager

  def getDefaultReturnToken = fDefaultReturnToken

}
