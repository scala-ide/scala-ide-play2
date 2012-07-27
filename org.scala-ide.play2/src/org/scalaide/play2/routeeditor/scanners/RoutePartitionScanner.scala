package org.scalaide.play2.routeeditor.scanners

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.scalaide.play2.routeeditor.rules.RouteActionRule;
import org.scalaide.play2.routeeditor.rules.RouteCommentRule;
import org.scalaide.play2.routeeditor.rules.RouteURIRule;

class RoutePartitionScanner extends RuleBasedPartitionScanner {
  val routeURI = new Token(RoutePartitions.ROUTE_URI);
  val routeAction = new Token(RoutePartitions.ROUTE_ACTION);
  val routeComment = new Token(RoutePartitions.ROUTE_COMMENT);

  val rules = Array[IPredicateRule](
    new RouteCommentRule(routeComment),
    new RouteURIRule(routeURI),
    new RouteActionRule(routeAction))
  setPredicateRules(rules);
}

object RoutePartitionScanner {
  
}