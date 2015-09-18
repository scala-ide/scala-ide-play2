package org.scalaide.play2.templates24

import scala.util.parsing.input.NoPosition
import scala.util.parsing.input.Positional

import org.scalaide.play2.templateeditor.lexical.TemplateParsing.CommentCode
import org.scalaide.play2.templateeditor.lexical.TemplateParsing.DefaultCode
import org.scalaide.play2.templateeditor.lexical.TemplateParsing.PlayTemplate
import org.scalaide.play2.templateeditor.lexical.TemplateParsing.ScalaCode

import play.twirl.parser.TreeNodes.Block
import play.twirl.parser.TreeNodes.Comment
import play.twirl.parser.TreeNodes.Def
import play.twirl.parser.TreeNodes.Display
import play.twirl.parser.TreeNodes.Plain
import play.twirl.parser.TreeNodes.PosString
import play.twirl.parser.TreeNodes.ScalaExp
import play.twirl.parser.TreeNodes.ScalaExpPart
import play.twirl.parser.TreeNodes.Simple
import play.twirl.parser.TreeNodes.Template
import play.twirl.parser.TreeNodes.TemplateTree
import play.twirl.parser.TwirlParser

object Template24Parser {
  private[this] val parser = new TwirlParser(true)

  // removes the generated yield after for
  private def fixFor(s: Simple): Simple = {
    if (s.code.startsWith("for(")) {
      val indexOfYield = s.code.indexOf(" yield ")
      if (indexOfYield == -1) {
        return s
      }
      val newS = Simple(s.code.substring(0, indexOfYield))
      newS.pos = s.pos
      return newS
    }
    s
  }

  private[this] def handleScalaExpPart(scalaExpPart: ScalaExpPart): List[PlayTemplate] = scalaExpPart match {
    case s @ Simple(code: String) =>
      List(ScalaCode(fixFor(s)))
    case Block(whitespace, args, content) =>
      args.map(ScalaCode(_)).toList ::: content.flatMap(handleTemplateTree).toList
  }

  private def handleDef(defn: Def): List[PlayTemplate] = defn match {
    case Def(name: PosString, params: PosString, code: Simple) =>
      List(ScalaCode(name), ScalaCode(params), ScalaCode(code))
  }

  private def handleTemplateTree(templateTree: TemplateTree): List[PlayTemplate] = templateTree match {
    case p @ Plain(text: String) =>
      List(DefaultCode(p))
    case Display(exp: ScalaExp) =>
      handleTemplateTree(exp)
    case cm @ Comment(msg: String) =>
      List(CommentCode(cm))
    case ScalaExp(parts) =>
      parts.flatMap(handleScalaExpPart).toList
  }

  private def handleTemplate(template: Template): List[PlayTemplate] = template match {
    case Template(name, comment, params, topImports, imports, defs, sub, content) =>
      val namePart = if (name != null && name.str.length != 0) List(ScalaCode(name)) else List()
      val commentPart = comment.map(CommentCode(_)).toList
      val paramsPart = if (params.pos != NoPosition) List(ScalaCode(params)) else List()
      val importsPart = (topImports ++ imports).map(ScalaCode(_)).toList
      val defsPart = defs.flatMap(handleDef).toList
      val subsPart = sub.flatMap(handleTemplate).toList
      val contentPart = content.flatMap(handleTemplateTree).toList
      namePart ::: commentPart ::: paramsPart ::: importsPart ::: defsPart ::: subsPart ::: contentPart
  }

  /**
   * Returns list of different types of region of the template code
   */
  def parse(templateCode: String): List[PlayTemplate] = {
    val result = parser.parse(templateCode) match {
      case parser.Success(p, _) => handleTemplate(p)
      case parser.Error(p, rest, errors) => handleTemplate(p)
    }
    result
  }

  def length(input: Positional): Int = input match {
    case Simple(code) =>
      code.length
    case Plain(code) =>
      code.length
    case PosString(str) =>
      str.length
    case Comment(str) =>
      str.length + "@**@".length
    case _ =>
      -1
  }
}
