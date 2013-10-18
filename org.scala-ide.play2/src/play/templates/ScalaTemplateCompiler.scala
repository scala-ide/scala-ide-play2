import scala.util.parsing.input.OffsetPosition

/** 
 *  This source was copied from Play20 codebase
 *  (@see https://github.com/playframework/Play20/blob/master/framework/src/templates-compiler/src/main/scala/play/templates/ScalaTemplateCompiler.scala).
 *   
 *  While the current implementation is identical to the original source, this might change in the future. 
 *  Please, have a look at the git history of this file to know the exact set of changes.
 *  
 *  @note Please, make sure to update the above notice the moment sensible changes are made to this source, as this is required by the section 
 *        4.2.b of the Apache2 license (Play2 is licensed under Apache2). 
 *        Specifically, 4.2.b in the Apache2 license states: '''You must cause any modified files to carry prominent notices stating that You changed the files'''.
 */
package play.templates {

  import scalax.file._
  import java.io.File
  import scala.annotation.tailrec
  import io.Codec

  object Hash {

    def apply(bytes: Array[Byte]): String = {
      import java.security.MessageDigest
      val digest = MessageDigest.getInstance("SHA-1")
      digest.reset()
      digest.update(bytes)
      digest.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
    }

  }

  case class TemplateCompilationError(source: File, message: String, line: Int, column: Int) extends RuntimeException(message)

  object MaybeGeneratedSource {

    def unapply(source: File): Option[GeneratedSource] = {
      val generated = GeneratedSource(source)
      if (generated.meta.isDefinedAt("SOURCE")) {
        Some(generated)
      } else {
        None
      }
    }

  }
  
  sealed trait AbstractGeneratedSource {
    def content: String
    
    lazy val meta: Map[String, String] = {
      val Meta = """([A-Z]+): (.*)""".r
      val UndefinedMeta = """([A-Z]+):""".r
      Map.empty[String, String] ++ {
        try {
          content.split("-- GENERATED --")(1).trim.split('\n').map { m =>
            m.trim match {
              case Meta(key, value) => (key -> value)
              case UndefinedMeta(key) => (key -> "")
              case _ => ("UNDEFINED", "")
            }
          }.toMap
        } catch {
          case _ => Map.empty[String, String]
        }
      }
    }
    
    lazy val matrix: Seq[(Int, Int)] = {
      for (pos <- meta("MATRIX").split('|'); val c = pos.split("->"))
        yield try {
        Integer.parseInt(c(0)) -> Integer.parseInt(c(1))
      } catch {
        case _ => (0, 0) // Skip if MATRIX meta is corrupted
      }
    }
    
    lazy val lines: Seq[(Int, Int)] = {
      for (pos <- meta("LINES").split('|'); val c = pos.split("->"))
        yield try {
        Integer.parseInt(c(0)) -> Integer.parseInt(c(1))
      } catch {
        case _ => (0, 0) // Skip if LINES meta is corrupted
      }
    }
    
    def mapPosition(generatedPosition: Int): Int = {
      matrix.indexWhere(p => p._1 > generatedPosition) match {
        case 0 => 0
        case i if i > 0 => {
          val pos = matrix(i - 1)
          pos._2 + (generatedPosition - pos._1)
        }
        case _ => {
          val pos = matrix.takeRight(1)(0)
          pos._2 + (generatedPosition - pos._1)
        }
      }
    }

    def mapLine(generatedLine: Int): Int = {
      lines.indexWhere(p => p._1 > generatedLine) match {
        case 0 => 0
        case i if i > 0 => {
          val line = lines(i - 1)
          line._2 + (generatedLine - line._1)
        }
        case _ => {
          val line = lines.takeRight(1)(0)
          line._2 + (generatedLine - line._1)
        }
      }
    }
  }

  case class GeneratedSource(file: File) extends AbstractGeneratedSource{
    
    def content = Path(file).string

    def needRecompilation: Boolean = (!file.exists ||
      // A generated source already exist but
      source.isDefined && ((source.get.lastModified > file.lastModified) || // the source has been modified
        (meta("HASH") != Hash(Path(source.get).byteArray))) // or the hash don't match
        )

    def toSourcePosition(marker: Int): (Int, Int) = {
      try {
        val targetMarker = mapPosition(marker)
        val line = Path(source.get).string.substring(0, targetMarker).split('\n').size
        (line, targetMarker)
      } catch {
        case _ => (0, 0)
      }
    }

    def source: Option[File] = {
      val s = new File(meta("SOURCE"))
      if (s == null || !s.exists) {
        None
      } else {
        Some(s)
      }
    }

    def sync() {
      if (file.exists && !source.isDefined) {
        file.delete()
      }
    }

  }
  
  case class GeneratedSourceVirtual(path: String) extends AbstractGeneratedSource {
    var _content = ""
    def setContent(newContent: String) {
      this._content = newContent
    }
    def content = _content
  }
  
  object ScalaTemplateCompiler {

    import play.templates.TreeNodes._
    import scala.util.parsing.input.CharSequenceReader
    import scala.util.parsing.combinator.JavaTokenParsers

    def compile(source: File, sourceDirectory: File, generatedDirectory: File, resultType: String, formatterType: String, additionalImports: String = "", inclusiveDot: Boolean) = {
      val (templateName, generatedSource) = generatedFile(source, sourceDirectory, generatedDirectory, inclusiveDot)
      if (generatedSource.needRecompilation) {
        val generated = parseAndGenerateCode(templateName, Path(source).byteArray, source.getAbsolutePath, resultType, formatterType, additionalImports, inclusiveDot)

        Path(generatedSource.file).write(generated.toString)

        Some(generatedSource.file)
      } else {
        None
      }
    }

    def compileVirtual(content: String, source: File, sourceDirectory: File, resultType: String, formatterType: String, additionalImports: String = "", inclusiveDot: Boolean) = {
      val (templateName, generatedSource) = generatedFileVirtual(source, sourceDirectory, inclusiveDot)
      val generated = parseAndGenerateCode(templateName, content.getBytes("UTF-8"), source.getAbsolutePath, resultType, formatterType, additionalImports, inclusiveDot)
      generatedSource.setContent(generated)
      generatedSource
    }
    
    def parseAndGenerateCode(templateName: Array[String], content: Array[Byte], absolutePath: String, resultType: String, formatterType: String, additionalImports: String, inclusiveDot: Boolean) = {
      val templateParser = new ScalaTemplateParser(inclusiveDot)
      templateParser.parse(new String(content, "UTF-8")) match {
        case templateParser.Success(parsed, rest) if rest.atEnd => {
          generateFinalTemplate(absolutePath, 
            content,
            templateName.dropRight(1).mkString("."),
            templateName.takeRight(1).mkString,
            parsed,
            resultType,
            formatterType,
            additionalImports)
        }
        case templateParser.Success(_, rest) => {
          throw new TemplateCompilationError(new File(absolutePath), "Not parsed?", rest.pos.line, rest.pos.column)
        }
        case templateParser.Error(_, rest, errors) => {
          val firstError = errors.head
          throw new TemplateCompilationError(new File(absolutePath), s"Errors ocurred while parsing.\n${firstError.str}", firstError.pos.line, firstError.pos.column)
        }
      }
    }

    def generatedFile(template: File, sourceDirectory: File, generatedDirectory: File, inclusiveDot: Boolean) = {
      val templateName = {
        val name = source2TemplateName(template, sourceDirectory, template.getName.split('.').takeRight(1).head).split('.')
        if (inclusiveDot) makeInclusiveDotFileNameModification(name)
        else name
      }
      templateName -> GeneratedSource(new File(generatedDirectory, templateName.mkString("/") + ".template.scala"))
    }

    def generatedFileVirtual(template: File, sourceDirectory: File, inclusiveDot: Boolean) = {
      val templateName = {
        val name = source2TemplateName(template, sourceDirectory, template.getName.split('.').takeRight(1).head).split('.')
        if (inclusiveDot) makeInclusiveDotFileNameModification(name)
        else name
      }
      templateName -> GeneratedSourceVirtual(templateName.mkString("/") + ".template.scala")
    }
    
    private def makeInclusiveDotFileNameModification(templateName: Array[String]): Array[String] = {
      templateName.isEmpty match {
        // FIXME: Used a better suffix besides my own name (even though I'm pretty sure I'm the only Jedd Haberstro on the planet, so it's unique ;))
        case false => templateName.patch(templateName.length - 1, List(templateName.last + "$$JeddHaberstro"), 1)
        case true => templateName
      }
    }
    
    @tailrec
    def source2TemplateName(f: File, sourceDirectory: File, ext: String, suffix: String = "", topDirectory: String = "views", setExt: Boolean = true): String = {
      val Name = """([a-zA-Z0-9_]+)[.]scala[.]([a-z]+)""".r
      (f, f.getName) match {
        case (f, _) if f == sourceDirectory => {
          if (setExt) {
            val parts = suffix.split('.')
            Option(parts.dropRight(1).mkString(".")).filterNot(_.isEmpty).map(_ + ".").getOrElse("") + ext + "." + parts.takeRight(1).mkString
          } else suffix
        }
        case (f, name) if name == topDirectory => source2TemplateName(f.getParentFile, sourceDirectory, ext, name + "." + ext + "." + suffix, topDirectory, false)
        case (f, Name(name, _)) if f.isFile => source2TemplateName(f.getParentFile, sourceDirectory, ext, name, topDirectory, setExt)
        case (f, name) if !f.isFile => source2TemplateName(f.getParentFile, sourceDirectory, ext, name + "." + suffix, topDirectory, setExt)
        case (f, name) => throw TemplateCompilationError(f, "Invalid template name [" + name + "]", 0, 0)
      }
    }

    def visit(elem: Seq[TemplateTree], previous: Seq[Any]): Seq[Any] = {
      elem.toList match {
        case head :: tail =>
          val tripleQuote = "\"\"\""
          visit(tail, head match {
            case p @ Plain(text) => (if (previous.isEmpty) Nil else previous :+ ",") :+ "format.raw" :+ Source("(", p.pos) :+ tripleQuote :+ text :+ tripleQuote :+ ")"
            case Comment(msg) => previous
            case Display(exp) => (if (previous.isEmpty) Nil else previous :+ ",") :+ "_display_(Seq[Any](" :+ visit(Seq(exp), Nil) :+ "))"
            case ScalaExp(parts) => previous :+ parts.map {
              case s @ Simple(code) => Source(code, s.pos)
              case b @ Block(whitespace, args, content) if (content.forall(_.isInstanceOf[ScalaExp])) => Nil :+ Source(whitespace + "{" + args.getOrElse(""), b.pos) :+ visit(content, Nil) :+ "}"
              case b @ Block(whitespace, args, content) => Nil :+ Source(whitespace + "{" + args.getOrElse(""), b.pos) :+ "_display_(Seq[Any](" :+ visit(content, Nil) :+ "))}"
            }
          })
        case Nil => previous
      }
    }

    def templateCode(template: Template, resultType: String): Seq[Any] = {

      val defs = (template.sub ++ template.defs).map { i =>
        i match {
          case t: Template if t.name == "" => templateCode(t, resultType)
          case t: Template => {
            Nil :+ (if (t.name.str.startsWith("implicit")) "implicit def " else "def ") :+ Source(t.name.str, t.name.pos) :+ Source(t.params.str, t.params.pos) :+ ":" :+ resultType :+ " = {_display_(" :+ templateCode(t, resultType) :+ ")};"
          }
          case Def(name, params, block) => {
            Nil :+ (if (name.str.startsWith("implicit")) "implicit def " else "def ") :+ Source(name.str, name.pos) :+ Source(params.str, params.pos) :+ " = {" :+ block.code :+ "};"
          }
        }
      }

      val imports = template.imports.map(_.code).mkString("\n")

      Nil :+ imports :+ "\n" :+ defs :+ "\n" :+ "Seq[Any](" :+ visit(template.content, Nil) :+ ")"
    }
    
    def generateCode(packageName: String, name: String, root: Template, resultType: String, formatterType: String, additionalImports: String) = {
      val extra = TemplateAsFunctionCompiler.getFunctionMapping(
        root.params.str,
        resultType)

      val generated = {
        Nil :+ """
package """ :+ packageName :+ """

import play.templates._
import play.templates.TemplateMagic._

""" :+ additionalImports :+ """
/*""" :+ root.comment.map(_.msg).getOrElse("") :+ """*/
object """ :+ name :+ """ extends BaseScalaTemplate[""" :+ resultType :+ """,Format[""" :+ resultType :+ """]](""" :+ formatterType :+ """) with """ :+ extra._3 :+ """ {

    /*""" :+ root.comment.map(_.msg).getOrElse("") :+ """*/
    def apply""" :+ Source(root.params.str, root.params.pos) :+ """:""" :+ resultType :+ """ = {
        _display_ {""" :+ templateCode(root, resultType) :+ """}
    }
    
    """ :+ extra._1 :+ """
    
    """ :+ extra._2 :+ """
    
    def ref: this.type = this

}"""
      }
      generated
    }

    @deprecated("use generateFinalTemplate with 8 parameters instead", "Play 2.1")
    def generateFinalTemplate(template: File, packageName: String, name: String, root: Template, resultType: String, formatterType: String, additionalImports: String): String = {
      generateFinalTemplate(template.getAbsolutePath, Path(template).byteArray, packageName, name, root, resultType, formatterType, additionalImports)
    }

    def generateFinalTemplate(absolutePath: String, contents: Array[Byte], packageName: String, name: String, root: Template, resultType: String, formatterType: String, additionalImports: String): String = {
      val generated = generateCode(packageName, name, root, resultType, formatterType, additionalImports)

      Source.finalSource(absolutePath, contents, generated)
    }

    object TemplateAsFunctionCompiler {

      // Note, the presentation compiler is not thread safe, all access to it must be synchronized.  If access to it
      // is not synchronized, then weird things happen like FreshRunReq exceptions are thrown when multiple sub projects
      // are compiled (done in parallel by default by SBT).  So if adding any new methods to this object, make sure you
      // make them synchronized.

      import java.io.File
      import scala.tools.nsc.interactive.{ Response, Global }
      import scala.tools.nsc.io.AbstractFile
      import scala.tools.nsc.util.{ SourceFile, Position, BatchSourceFile }
      import scala.tools.nsc.Settings
      import scala.tools.nsc.reporters.ConsoleReporter

      def getFunctionMapping(signature: String, returnType: String): (String, String, String) = synchronized {

        type Tree = PresentationCompiler.global.Tree
        type DefDef = PresentationCompiler.global.DefDef
        type TypeDef = PresentationCompiler.global.TypeDef

        def filterType(t: String) = t match {
          case vararg if vararg.startsWith("_root_.scala.<repeated>") => vararg.replace("_root_.scala.<repeated>", "Array")
          case synthetic if synthetic.contains("<synthetic>") => synthetic.replace("<synthetic>", "")
          case t => t
        }

        def findSignature(tree: Tree): Option[DefDef] = {
          tree match {
            case t: DefDef if t.name.toString == "signature" => Some(t)
            case t: Tree => t.children.flatMap(findSignature).headOption
          }
        }

        val params = findSignature(
          PresentationCompiler.treeFrom("object FT { def signature" + signature + " }")).get.vparamss

        val functionType = "(" + params.map(group => "(" + group.map {
          case a if a.symbol.isByNameParam => " => " + a.tpt.children(1).toString
          case a => filterType(a.tpt.toString)
        }.mkString(",") + ")").mkString(" => ") + " => " + returnType + ")"

        val renderCall = "def render%s: %s = apply%s".format(
          "(" + params.flatten.map {
            case a if a.symbol.isByNameParam => a.name.toString + ":" + a.tpt.children(1).toString
            case a => a.name.toString + ":" + filterType(a.tpt.toString)
          }.mkString(",") + ")",
           returnType,
          params.map(group => "(" + group.map { p =>
            p.name.toString + Option(p.tpt.toString).filter(_.startsWith("_root_.scala.<repeated>")).map(_ => ":_*").getOrElse("")
          }.mkString(",") + ")").mkString)

        var templateType = "play.api.templates.Template%s[%s%s]".format(
          params.flatten.size,
          params.flatten.map {
            case a if a.symbol.isByNameParam => a.tpt.children(1).toString
            case a => filterType(a.tpt.toString)
          }.mkString(","),
          (if (params.flatten.isEmpty) "" else ",") + returnType)

        val f = "def f:%s = %s => apply%s".format(
          functionType,
          params.map(group => "(" + group.map(_.name.toString).mkString(",") + ")").mkString(" => "),
          params.map(group => "(" + group.map { p =>
            p.name.toString + Option(p.tpt.toString).filter(_.startsWith("_root_.scala.<repeated>")).map(_ => ":_*").getOrElse("")
          }.mkString(",") + ")").mkString)

        (renderCall, f, templateType)
      }
      
      def shutdownPresentationCompiler(): Unit = synchronized {
        PresentationCompiler.shutdown()
      }

      private class CompilerInstance {

        def additionalClassPathEntry: Option[String] = None

        lazy val compiler = {

          val settings = new Settings

          val scalaObjectSource = Class.forName("scala.ScalaObject").getProtectionDomain.getCodeSource

          // is null in Eclipse/OSGI but luckily we don't need it there
          if (scalaObjectSource != null) {
            import java.security.CodeSource
            def toAbsolutePath(cs: CodeSource) = new File(cs.getLocation.getFile).getAbsolutePath
            val compilerPath = toAbsolutePath(Class.forName("scala.tools.nsc.Interpreter").getProtectionDomain.getCodeSource)
            val libPath = toAbsolutePath(scalaObjectSource)
            val pathList = List(compilerPath, libPath)
            val origBootclasspath = settings.bootclasspath.value
            settings.bootclasspath.value = ((origBootclasspath :: pathList) ::: additionalClassPathEntry.toList) mkString File.pathSeparator
          }

          val compiler = new Global(settings, new ConsoleReporter(settings) {
            override def printMessage(pos: Position, msg: String) = ()
          })

          compiler.ask(() => new compiler.Run)

          compiler
        }
      }

      private trait TreeCreationMethods {

        val global: scala.tools.nsc.interactive.Global

        val randomFileName = {
          val r = new java.util.Random
          () => "file" + r.nextInt
        }

        def treeFrom(src: String): global.Tree = {
          val file = new BatchSourceFile(randomFileName(), src)
          treeFrom(file)
        }

        def treeFrom(file: SourceFile): global.Tree = {
          import tools.nsc.interactive.Response

          type Scala29Compiler = {
            def askParsedEntered(file: SourceFile, keepLoaded: Boolean, response: Response[global.Tree]): Unit
            def askType(file: SourceFile, forceReload: Boolean, respone: Response[global.Tree]): Unit
          }

          val newCompiler = global.asInstanceOf[Scala29Compiler]

          val r1 = new Response[global.Tree]
          newCompiler.askParsedEntered(file, true, r1)
          r1.get.left.toOption.getOrElse(throw r1.get.right.get)
        }

      }

      private object CompilerInstance extends CompilerInstance

      private object PresentationCompiler extends TreeCreationMethods {
        val global = CompilerInstance.compiler

        def shutdown(): Unit = synchronized {
          global.askShutdown()
        }
      }

    }

  }

  /* ------- */

  import scala.util.parsing.input.{ Position, OffsetPosition, NoPosition }

  case class Source(code: String, pos: Position = NoPosition)

  object Source {

    import scala.collection.mutable.ListBuffer

    def finalSource(absolutePath: String, contents: Array[Byte], generatedTokens: Seq[Any]): String = {
      val scalaCode = new StringBuilder
      val positions = ListBuffer.empty[(Int, Int)]
      val lines = ListBuffer.empty[(Int, Int)]
      serialize(generatedTokens, scalaCode, positions, lines)
      scalaCode + """
                /*
                    -- GENERATED --
                    DATE: """ + new java.util.Date + """
                    SOURCE: """ + absolutePath.replace(File.separator, "/") + """
                    HASH: """ + Hash(contents) + """
                    MATRIX: """ + positions.map { pos =>
        pos._1 + "->" + pos._2
      }.mkString("|") + """
                    LINES: """ + lines.map { line =>
        line._1 + "->" + line._2
      }.mkString("|") + """
                    -- GENERATED --
                */
            """
    }
    
    @deprecated("use finalSource with 3 parameters instead", "Play 2.1")
    def finalSource(template: File, generatedTokens: Seq[Any]): String = {
      finalSource(template.getAbsolutePath, Path(template).byteArray, generatedTokens)
    }

    private def serialize(parts: Seq[Any], source: StringBuilder, positions: ListBuffer[(Int, Int)], lines: ListBuffer[(Int, Int)]) {
      parts.foreach {
        case s: String => source.append(s)
        case Source(code, pos @ OffsetPosition(_, offset)) => {
          source.append("/*" + pos + "*/")
          positions += (source.length -> offset)
          lines += (source.toString.split('\n').size -> pos.line)
          source.append(code)
        }
        case Source(code, NoPosition) => source.append(code)
        case s: Seq[any] => serialize(s, source, positions, lines)
      }
    }

  }

}