package org.scalaide.play2.templateeditor.lexical

import scala.util.parsing.input.CharSequenceReader
import scala.util.parsing.input.Positional
import scala.util.parsing.input.OffsetPosition
import scala.util.parsing.input.NoPosition
import play.twirl.parser.TwirlParser
import play.twirl.parser.TreeNodes._

/**
 * A helper for using tmeplate parser
 */
object TemplateParsing {
  implicit def stringToCharSeq(str: String) = new CharSequenceReader(str)

  val parser = new TwirlParser(true)

  sealed abstract class PlayTemplate(input: Positional, kind: String) {
    val offset = input.pos match {
      case offsetPosition: OffsetPosition =>
        offsetPosition.offset
      case _ =>
        -1
    }
    val length = input match {
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
    override def toString = kind + "[" + offset + " - " + length + "]: " + input
  }
  case class ScalaCode(input: Positional) extends PlayTemplate(input, "sc")
  case class DefaultCode(input: Plain) extends PlayTemplate(input, "df")
  case class CommentCode(input: Positional) extends PlayTemplate(input, "cm")

  // removes the generated yield after for
  def fixFor(s: Simple): Simple = {
    if (s.code.startsWith("for(")) {
      val indexOfYield = s.code.indexOf(" yield ")
      if (indexOfYield == -1){
        return s
      }
      val newS = Simple(s.code.substring(0, indexOfYield))
      newS.pos = s.pos
      return newS
    }
    s
  }

  def handleScalaExpPart(scalaExpPart: ScalaExpPart): List[PlayTemplate] = scalaExpPart match {
    case s @ Simple(code: String) =>
      List(ScalaCode(fixFor(s)))
    case Block(whitespace, args, content) =>
      args.map(ScalaCode(_)).toList ::: content.flatMap(handleTemplateTree).toList
  }

  def handleDef(defn: Def): List[PlayTemplate] = defn match {
    case Def(name: PosString, params: PosString, code: Simple) =>
      List(ScalaCode(name), ScalaCode(params), ScalaCode(code))
  }

  def handleTemplateTree(templateTree: TemplateTree): List[PlayTemplate] = templateTree match {
    case p @ Plain(text: String) =>
      List(DefaultCode(p))
    case Display(exp: ScalaExp) =>
          handleTemplateTree(exp)
    case cm @ Comment(msg: String) =>
      List(CommentCode(cm))
    case ScalaExp(parts) =>
      parts.flatMap(handleScalaExpPart).toList
  }

  def handleTemplate(template: Template): List[PlayTemplate] = template match {
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
  def handleTemplateCode(templateCode: String) = {
    val result = parser.parse(templateCode) match {
      case parser.Success(p, _)          => handleTemplate(p)
      case parser.Error(p, rest, errors) => handleTemplate(p)
    }
    result
  }

}