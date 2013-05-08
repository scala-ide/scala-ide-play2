package org.scalaide.play2.routeeditor.completion.action

import scala.tools.eclipse.ScalaPresentationCompiler
import scala.tools.eclipse.completion.MemberKind
import org.eclipse.jface.text.IRegion
import org.scalaide.play2.quickassist.ControllerMethod
import org.eclipse.jface.text.IDocument
import org.scalaide.editor.WordFinder
import org.eclipse.jface.text.Region

class ActionCompletionComputer(compiler: ScalaPresentationCompiler) {

  private val wordFinder = new WordFinder

  def computeCompletionProposals(document: IDocument, offset: Int): List[ActionCompletionProposal] = {
    val region = wordFinder.findWord(document, offset)
    val input = document.get(region.getOffset, region.getLength)
    lazy val replaceRegion = computeReplaceRegion(region, input)
    
    val action = new ActionCall(input)

    for {
      member <- MembersComputer(compiler, action).members
      name = simpleName(member.asInstanceOf[compiler.Symbol])
      if name.startsWith(action.suffix)
      kind = kindOf(member.asInstanceOf[compiler.Symbol])
      isJava = !member.isPackage && member.isJava
    } yield new ActionCompletionProposal(replaceRegion, name, kind, isJava)
  }

  /** Based on the passed `input` and its current `region` in the document, compute the
    * document's region that will be replaced if one of action completion proposal is selected.
    */
  private def computeReplaceRegion(region: IRegion, input: String): IRegion = {
    val suffixStart = input.lastIndexOf('.') + 1
    val suffixLength = input.length - suffixStart
    val suffixOffset = region.getOffset + suffixStart
    new Region(suffixOffset, suffixLength)
  }

  private def simpleName(member: compiler.Symbol): String = {
    val name = member.decodedName
    member match {
      case m: compiler.MethodSymbol =>
        val formattedParamss = m.paramss.flatten.map(param => (param.name.toString, param.tpe.toString))
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
    else if (member.isClass) Class
    else if (member.isModule) Object
    else {
      val accessed = member.accessedOrSelf
      if (accessed.isVar) Var
      else Val
    }
  }
}