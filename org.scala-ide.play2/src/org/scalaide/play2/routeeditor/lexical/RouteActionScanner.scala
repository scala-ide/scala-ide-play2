package org.scalaide.play2.routeeditor.lexical

import org.eclipse.jdt.ui.text.IColorManager
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.rules.IRule
import org.eclipse.jface.text.rules.IWordDetector
import org.eclipse.jface.text.rules.WordRule
import org.scalaide.play2.routeeditor.RouteSyntaxClasses.ACTION
import org.scalaide.play2.routeeditor.RouteSyntaxClasses.ACTION_CLASS
import org.scalaide.play2.routeeditor.RouteSyntaxClasses.ACTION_METHOD
import org.scalaide.play2.routeeditor.RouteSyntaxClasses.ACTION_PACKAGE
/**
 * scanner for action part of route file
 */
class RouteActionScanner(prefStore: IPreferenceStore, manager: IColorManager) extends AbstractRouteScanner(ACTION, prefStore, manager) {

  val packageToken = getToken(ACTION_PACKAGE);
  val classToken = getToken(ACTION_CLASS);
  val methodToken = getToken(ACTION_METHOD);
  val methodArgumentToken = fDefaultReturnToken

  val rules = Array[IRule](
    // Add a rule for method argument
    new WordRule(new MethodArgumentDetector(),
      methodArgumentToken),
    // Add a rule for class
    new WordRule(new ClassDetector(), classToken),
    // Add a rule for method and package
    new MethodPackageRule(packageToken, methodToken))

  setRules(rules);

  private class ClassDetector extends IWordDetector {

    override def isWordStart(c: Char) = {
      Character.isUpperCase(c)
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
