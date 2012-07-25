package org.scalaide.play2.routeeditor.scanners

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WordRule;
import org.scalaide.play2.routeeditor.ColorManager;
import org.scalaide.play2.routeeditor.RouteColorConstants;
import org.scalaide.play2.routeeditor.rules.PackageRule;

class RouteActionScanner(manager: ColorManager) extends AbstractRouteScanner(RouteColorConstants.getToken("ROUTE_ACTION",
  manager)) {

  val packageToken = RouteColorConstants.getToken(
    "ROUTE_ACTION_PACKAGE", manager);
  val classToken = RouteColorConstants
    .getToken("ROUTE_CLASS", manager);
  val methodToken = RouteColorConstants.getToken("ROUTE_METHOD",
    manager);
  val methodArgumentToken = fDefaultReturnToken

  val rules = Array[IRule](
    // Add a rule for method argument
    new WordRule(new MethodArgumentDetector(),
      methodArgumentToken),
    // Add a rule for package
    new PackageRule(packageToken),
    // Add a rule for class
    new WordRule(new ClassDetector(), classToken),
    // Add a rule for method
    new WordRule(new MethodDetector(), methodToken))

  setRules(rules);

  private class ClassDetector extends IWordDetector {

    override def isWordStart(c: Char) = {
      Character.isUpperCase(c)
    }

    override def isWordPart(c: Char) = {
      Character.isJavaIdentifierPart(c);
    }

  }

  private class MethodDetector extends IWordDetector {

    override def isWordStart(c: Char) = {
      Character.isLowerCase(c)
    }

    override def isWordPart(c: Char) = {
      Character.isJavaIdentifierPart(c);
    }
  }

  private class MethodArgumentDetector extends IWordDetector {

    override def isWordStart(c: Char) = {
      c == '('
    }

    override def isWordPart(c: Char) = {
      true
    }

  }

}
