package org.scalaide.play2.routeeditor.completion.action

import org.scalaide.core.completion.MemberKind
import org.scalaide.ui.completion.ScalaCompletionProposal

import org.eclipse.jdt.internal.ui.JavaPluginImages
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point

/** Completion for action method in route file.
  *
  * @param replaceRegion The region in the document that will be replaced if this proposal is applied.
  * @param simpleName    The name of a package, type or method.
  * @param kind          The kind of member of this proposal, i.e., val, var, object, class, package, ...
  * @param isJava        Is this completion for a Java member.
  */
class ActionCompletionProposal(replaceRegion: IRegion, val simpleName: String, val kind: MemberKind.Value, val isJava: Boolean)
  extends ICompletionProposal {

  import ActionCompletionProposal.javaFieldImage

  override def apply(document: IDocument): Unit = {
    document.replace(replaceRegion.getOffset, replaceRegion.getLength, simpleName)
  }

  override def getSelection(document: IDocument): Point =
    new Point(replaceRegion.getOffset + simpleName.length, 0) // always put caret *after* the inserted completion

  override def getAdditionalProposalInfo: String = null
  override def getDisplayString: String = simpleName
  override def getImage: Image = kind match {
    case MemberKind.Def              => ScalaCompletionProposal.defImage
    case MemberKind.Class if !isJava => ScalaCompletionProposal.classImage
    case MemberKind.Class if isJava  => ScalaCompletionProposal.javaClassImage
    case MemberKind.Object           => ScalaCompletionProposal.objectImage
    case MemberKind.Package          => ScalaCompletionProposal.packageImage
    case MemberKind.Val              => ScalaCompletionProposal.valImage
    case MemberKind.Var              => javaFieldImage
    case _                           => ScalaCompletionProposal.valImage
  }
  override def getContextInformation: IContextInformation = null

}

object ActionCompletionProposal {
  private val javaFieldImage = JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC)

  implicit object ByKindAndAlphabetically extends Ordering[ActionCompletionProposal] {
    override def compare(x: ActionCompletionProposal, y: ActionCompletionProposal): Int = {
      /* If `kind` is a package, then only types and packages can be suggested to the user. In this case, 
       * types should be shown before packages. The only other alternative is that `kind` is a member, 
       * in this case sort the members alphabetically.
       */
      val compared = relevance(y) - relevance(x)
      if (compared == 0) x.simpleName.compareTo(y.simpleName) else compared
    }

    private def relevance(that: ActionCompletionProposal): Int = {
      if (that.kind == MemberKind.Package) 10
      else 100
    }
  }
}