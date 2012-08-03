package org.scalaide.play2.templateeditor.scanners

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
  val ADAPT_AT = false

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
  
  // TODO should be removed after fixing import in original parser
  def fixImport(s: Simple): Simple = {
    if(s.code.startsWith("import")){
      val oldPos = s.pos.asInstanceOf[OffsetPosition]
      val offset = oldPos.offset
      val newPos = new OffsetPosition(oldPos.source, offset+1)
      s.pos = newPos
    }
    s
  }

  def handleScalaExpPart(scalaExpPart: ScalaExpPart with Positional): List[PlayTemplate] = scalaExpPart match {
    case s@Simple(code: String) =>
      List(ScalaCode(scalaExpPart))
//      List(ScalaCode(fixImport(s))) // TODO should be removed after fixing import in original parser
    case Block(whitespace: String, args: Option[String], content: Seq[TemplateTree]) =>
      content.flatMap(handleTemplateTree).toList
  }

  def handleDef(defn: Def): List[PlayTemplate] = defn match {
    case Def(name: PosString, params: PosString, code: Simple) =>
      List(ScalaCode(name), ScalaCode(params), ScalaCode(code))
  }

  def handleTemplateTree(templateTree: TemplateTree): List[PlayTemplate] = templateTree match {
    case p @ Plain(text: String) =>
      List(DefaultCode(p))
    case Display(exp: ScalaExp) =>
      //            List(ScalaCode(templateTree))
      handleTemplateTree(adaptAt(exp).asInstanceOf[ScalaExp])
    //      handleTemplateTree(exp)
    case cm @ Comment(msg: String) =>
      List(CommentCode(cm)) // FIXME for the moment, no support for comments
    //      List()
    case ScalaExp(parts: Seq[ScalaExpPart with Positional]) =>
      parts.flatMap(handleScalaExpPart).toList
  }

  def adaptAt(input: Positional): Positional = {
    if (!ADAPT_AT){
      return input
    }
    val newPos = {
      if (input.pos != NoPosition) { // for handling ScalaExp!
        val OffsetPosition(source, offset) = input.pos
        new OffsetPosition(source, offset - 1)
      } else
        NoPosition
    }
    val newInput = input match {
      case Simple(code) =>
        Simple("@" + code)
      case PosString(str) =>
        PosString("@" + str)
      case ScalaExp(parts: Seq[ScalaExpPart]) if parts.length > 0 =>
        val first = parts(0)
        val newFirst = adaptAt(first.asInstanceOf[Positional])
        val newParts = newFirst :: parts.slice(1, parts.length).toList
        ScalaExp(newParts.toSeq.asInstanceOf[Seq[ScalaExpPart]])
      case _ => input
    }
    newInput.pos = newPos
    newInput
  }

  def handleTemplate(template: Template): List[PlayTemplate] = template match {
    case Template(name: PosString, comment: Option[Comment], params: PosString, imports: Seq[Simple], defs: Seq[Def], sub: Seq[Template], content: Seq[TemplateTree]) =>
      val commentPart = comment.map(CommentCode(_)).toList
      val paramsPart = if (params.pos != NoPosition) List(ScalaCode(adaptAt(params))) else List()
//      val importsPart = imports.map(ScalaCode(_)).toList
      val importsPart = imports.map( s => ScalaCode(fixImport(s)) ).toList // TODO should be removed after fixing import in original parser
      val defsPart = defs.flatMap(handleDef).toList
      val subsPart = sub.flatMap(handleTemplate).toList
      val contentPart = content.flatMap(handleTemplateTree).toList
      commentPart ::: paramsPart ::: importsPart ::: defsPart ::: subsPart ::: contentPart
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