package org.scalaide.play2.templateeditor.lexical

import org.eclipse.jdt.ui.text.IColorManager
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.rules.IRule
import org.eclipse.jface.text.rules.IWordDetector
import org.eclipse.jface.text.rules.WordRule
import org.scalaide.play2.routeeditor.lexical.AbstractRouteScanner
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses

class TemplateDefaultScanner(prefStore: IPreferenceStore) extends AbstractRouteScanner(TemplateSyntaxClasses.DEFAULT, prefStore) {
  val atToken = getToken(TemplateSyntaxClasses.MAGIC_AT);
  val braceToken = getToken(TemplateSyntaxClasses.BRACE);

  val rules = Array[IRule](
    new WordRule(new OperatorDetector('@'),
      atToken),
    new WordRule(new OperatorDetector('{', '}'), braceToken))

  setRules(rules);

  private class OperatorDetector(operators: Char*) extends IWordDetector {

    override def isWordStart(c: Char) = {
      operators exists (_ == c)
    }

    override def isWordPart(c: Char) = {
      false
    }

  }

}
