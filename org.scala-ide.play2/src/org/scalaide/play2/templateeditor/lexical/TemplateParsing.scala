package org.scalaide.play2.templateeditor.lexical

import scala.util.parsing.input.CharSequenceReader
import play.templates.ScalaTemplateCompiler
import play.templates.ScalaTemplateCompiler.Block
import play.templates.ScalaTemplateCompiler.Comment
import play.templates.ScalaTemplateCompiler.Def
import play.templates.ScalaTemplateCompiler.Display
import play.templates.ScalaTemplateCompiler.Plain
import play.templates.ScalaTemplateCompiler.PosString
import play.templates.ScalaTemplateCompiler.ScalaExp
import play.templates.ScalaTemplateCompiler.ScalaExpPart
import play.templates.ScalaTemplateCompiler.Simple
import play.templates.ScalaTemplateCompiler.Template
import play.templates.ScalaTemplateCompiler.TemplateTree
import scala.util.parsing.input.Positional
import scala.util.parsing.input.OffsetPosition
import scala.util.parsing.input.NoPosition

object TemplateParsing {
  implicit def stringToCharSeq(str: String) = new CharSequenceReader(str)
  val compiler = ScalaTemplateCompiler
  val parser = compiler.templateParser

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
      val newS = Simple(s.code.substring(0, indexOfYield))
      newS.pos = s.pos
      return newS
    }
    s
  }

  def handleScalaExpPart(scalaExpPart: ScalaExpPart with Positional): List[PlayTemplate] = scalaExpPart match {
    case s @ Simple(code: String) =>
      List(ScalaCode(fixFor(s)))
    case Block(whitespace: String, args: Option[PosString], content: Seq[TemplateTree]) =>
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
    case ScalaExp(parts: Seq[ScalaExpPart with Positional]) =>
      parts.flatMap(handleScalaExpPart).toList
  }

  def handleTemplate(template: Template): List[PlayTemplate] = template match {
    case Template(name: PosString, comment: Option[Comment], params: PosString, imports: Seq[Simple], defs: Seq[Def], sub: Seq[Template], content: Seq[TemplateTree]) =>
      val namePart = if (name != null && name.str.length != 0) List(ScalaCode(name)) else List()
      val commentPart = comment.map(CommentCode(_)).toList
      val paramsPart = if (params.pos != NoPosition) List(ScalaCode(params)) else List()
      val importsPart = imports.map(ScalaCode(_)).toList
      val defsPart = defs.flatMap(handleDef).toList
      val subsPart = sub.flatMap(handleTemplate).toList
      val contentPart = content.flatMap(handleTemplateTree).toList
      namePart ::: commentPart ::: paramsPart ::: importsPart ::: defsPart ::: subsPart ::: contentPart
  }

  def handleTemplateCode(templateCode: String) = {
    val result = parser.parser(templateCode) match {
      case parser.Success(p, _) =>
        handleTemplate(p.asInstanceOf[Template])
      case parser.NoSuccess(message, input) => List()
    }
    result
  }

}