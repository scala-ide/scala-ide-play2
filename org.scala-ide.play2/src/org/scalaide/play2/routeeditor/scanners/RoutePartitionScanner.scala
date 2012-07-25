package org.scalaide.play2.routeeditor.scanners

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.scalaide.play2.routeeditor.rules.RouteActionRule;
import org.scalaide.play2.routeeditor.rules.RouteCommentRule;
import org.scalaide.play2.routeeditor.rules.RouteURIRule;

class RoutePartitionScanner extends RuleBasedPartitionScanner {
  {
    val routeURI = new Token(RoutePartitionScanner.ROUTE_URI);
    val routeAction = new Token(RoutePartitionScanner.ROUTE_ACTION);
    val routeComment = new Token(RoutePartitionScanner.ROUTE_COMMENT);

    val rules = Array[IPredicateRule](
      new RouteCommentRule(routeComment),
      new RouteURIRule(routeURI),
      new RouteActionRule(routeAction))
    setPredicateRules(rules);
  }
}

object RoutePartitionScanner {
  val ROUTE_URI = "__route_uri"
  val ROUTE_ACTION = "__route_action"
  val ROUTE_COMMENT = "__route_comment"

  def getTypes() = {
    Array(ROUTE_URI, ROUTE_ACTION , ROUTE_COMMENT  );
  }

  def isRouteAction(typeString: String) = {
    typeString == ROUTE_ACTION;
  }
}