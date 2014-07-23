package org.scalaide.play2.routeeditor.lexical

import org.scalaide.core.internal.lexical.AbstractScalaScanner
import org.scalaide.ui.syntax.ScalaSyntaxClass

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.rules.RuleBasedScanner

abstract class AbstractRouteScanner(defaultSyntax: ScalaSyntaxClass, prefStore: IPreferenceStore) extends RuleBasedScanner with AbstractScalaScanner {
  fDefaultReturnToken = getToken(defaultSyntax)
  override def preferenceStore = prefStore

  def getDefaultReturnToken = fDefaultReturnToken

}
