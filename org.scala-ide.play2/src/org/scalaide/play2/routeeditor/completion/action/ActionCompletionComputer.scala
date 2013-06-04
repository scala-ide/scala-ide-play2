package org.scalaide.play2.routeeditor.completion.action

import scala.tools.eclipse.ScalaPresentationCompiler
import scala.tools.eclipse.completion.MemberKind

import org.eclipse.jdt.core.IMethod
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Region
import org.scalaide.editor.WordFinder
import org.scalaide.play2.quickassist.ControllerMethod

class ActionCompletionComputer(compiler: ScalaPresentationCompiler) {

  def computeCompletionProposals(document: IDocument, offset: Int): List[ActionCompletionProposal] = {
    val inputRegion = WordFinder.findWord(document, offset)
    val rawInput = document.get(inputRegion.getOffset, (offset - inputRegion.getOffset()))
    lazy val replaceRegion = computeCompletionOverwriteReplaceRegion(inputRegion, rawInput)

    val input = new ActionRouteInput(rawInput)

    for {
      memberr <- MembersComputer(compiler, input).members
      // I need the cast because of path-dependent type mismatch. Feel free to suggest a fix, if you see one.
      member = memberr.asInstanceOf[compiler.Symbol]
      textReplacement = completionText(member)
      if textReplacement.startsWith(input.suffix)
      kind = kindOf(member)
      isJava = !member.isPackage && member.isJava
    } yield new ActionCompletionProposal(replaceRegion, textReplacement, kind, isJava)
  }

  /** Based on the passed `input` and its current `region` in the document, compute the
    * document's region that will be replaced when one of the proposed completions is applied.
    */
  private def computeCompletionOverwriteReplaceRegion(region: IRegion, input: String): IRegion = {
    // skip past either the last period or, if there is none, past the starting '@' if one exists.
    val suffixStart = Math.max(input.lastIndexOf('.'), input.indexOf('@')) + 1
    val suffixOffset = region.getOffset + suffixStart
    val length = Math.max(0, region.getLength - suffixStart)
    new Region(suffixOffset, length)
  }

  private def completionText(member: compiler.Symbol): String = {
    val name = member.decodedName
    member match {
      case m: compiler.MethodSymbol =>
        lazy val defaultFormattedParamss = m.paramss.flatten.map(param => (param.decodedName.toString, param.tpe.toString))
        val formattedParamss = m.isJava match {
          case true  => {
            val paramNames = compiler.getJavaElement(m) map {
              case imethod: IMethod => imethod.getParameterNames.toList
            }
            paramNames match {
              case Some(names) => names.zip(m.paramss.flatten.map(_.tpe.toString))
              case None        => defaultFormattedParamss
            }
          }
          case false => defaultFormattedParamss
        }
        val controllerMethod = ControllerMethod.apply(member.fullNameString, formattedParamss).toRouteCallSyntax
        val fullyQualifiedMethodCall = {
          // remove empty-parens if member was declared with no-parens. This is nice in general, and especially when when completing Action val. 
          if (m.paramss.isEmpty && controllerMethod.endsWith("()")) controllerMethod.substring(0, controllerMethod.length - 2)
          else controllerMethod
        }
        fullyQualifiedMethodCall.substring(fullyQualifiedMethodCall.lastIndexOf('.') + 1)
      case _ => name
    }
  }

  private def kindOf(member: compiler.Symbol): MemberKind.Value = {
    import MemberKind._
    if (member.isSourceMethod && !(member.isAccessor || member.isParamAccessor)) Def
    else if (member.isPackage) Package
    else if (member.isPackageObject) PackageObject
    else if (member.isClass || (member.isJava && member.isModule)) Class
    else if (member.isModule) Object
    else {
      val accessed = member.accessedOrSelf
      if (accessed.isVar) Var
      else Val
    }
  }
}