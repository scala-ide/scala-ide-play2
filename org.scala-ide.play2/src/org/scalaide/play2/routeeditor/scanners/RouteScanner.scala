package org.scalaide.play2.routeeditor.scanners

import org.eclipse.jface.text.rules.IRule
import org.eclipse.jface.text.rules.RuleBasedScanner
import org.eclipse.jface.text.rules.WhitespaceRule
import org.scalaide.play2.routeeditor.ColorManager
import org.scalaide.play2.routeeditor.RouteColorConstants
import org.scalaide.play2.routeeditor.RouteWhitespaceDetector
import org.scalaide.play2.routeeditor.rules.HTTPKeywordRule
import org.scalaide.play2.routeeditor.rules.RouteCommentRule

class RouteScanner(manager: ColorManager) extends AbstractRouteScanner(RouteColorConstants.getToken("DEFAULT", manager)) {
  val httpToken = RouteColorConstants
    .getToken("HTTP_KEYWORD", manager);

  val rules = Array[IRule](
    // Add generic whitespace rule.
    new WhitespaceRule(new RouteWhitespaceDetector()),
    // Add HTTP rule
    new HTTPKeywordRule(fDefaultReturnToken, httpToken))

  setRules(rules);
}
