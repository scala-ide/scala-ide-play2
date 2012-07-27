package org.scalaide.play2.routeeditor.scanners

import org.eclipse.jdt.ui.text.IColorManager
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.rules.IRule
import org.eclipse.jface.text.rules.RuleBasedScanner
import org.eclipse.jface.text.rules.WhitespaceRule
import org.scalaide.play2.routeeditor.RouteSyntaxClasses._
import org.scalaide.play2.routeeditor.RouteWhitespaceDetector
import org.scalaide.play2.routeeditor.rules.HTTPKeywordRule

class RouteScanner(prefStore: IPreferenceStore, manager: IColorManager) extends AbstractRouteScanner(DEFAULT, prefStore, manager) {
  val httpToken = getToken(HTTP_KEYWORD);

  val rules = Array[IRule](
    // Add generic whitespace rule.
    new WhitespaceRule(new RouteWhitespaceDetector()),
    // Add HTTP rule
    new HTTPKeywordRule(fDefaultReturnToken, httpToken))

  setRules(rules);
}
