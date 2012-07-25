package org.scalaide.play2.routeeditor.scanners

import org.eclipse.jface.text.rules.IRule
import org.eclipse.jface.text.rules.RuleBasedScanner
import org.eclipse.jface.text.rules.WhitespaceRule
import org.scalaide.play2.routeeditor.ColorManager
import org.scalaide.play2.routeeditor.RouteColorConstants
import org.scalaide.play2.routeeditor.RouteWhitespaceDetector
import org.scalaide.play2.routeeditor.rules.HTTPKeywordRule
import org.scalaide.play2.routeeditor.rules.RouteCommentRule
import org.eclipse.jface.text.rules.IToken

abstract class AbstractRouteScanner(defaultToken: IToken) extends RuleBasedScanner {
  fDefaultReturnToken = defaultToken
    
  def getDefaultReturnToken = fDefaultReturnToken
}
