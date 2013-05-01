package org.scalaide.play2.routeeditor.completion.action

import scala.tools.eclipse.completion.MemberKind
import scala.tools.eclipse.ui.ScalaCompletionProposal

import org.eclipse.jdt.internal.ui.JavaPluginImages
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point

class ActionCompletionProposal(replaceRegion: IRegion, val simpleName: String, val kind: MemberKind.Value, val isJava: Boolean) 
  extends ICompletionProposal {

  import ActionCompletionProposal.javaFieldImage

  override def apply(document: IDocument): Unit = {
    document.replace(replaceRegion.getOffset, replaceRegion.getLength, simpleName)
  }

  override def getSelection(document: IDocument): Point =
    new Point(replaceRegion.getOffset + simpleName.length, 0) // always put caret *after* the inserted completion

  override def getAdditionalProposalInfo: String = null
  override def getDisplayString: String = simpleName.split('.').lastOption.getOrElse("")
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

  def relevance: Int = {
    if (kind == MemberKind.Package) 10
    else 100
  }
}

object ActionCompletionProposal {
  private val javaFieldImage = JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC)

  implicit object ByRelevanceAndAlphabetically extends Ordering[ActionCompletionProposal] {
    override def compare(x: ActionCompletionProposal, y: ActionCompletionProposal): Int = {
      val compared = y.relevance - x.relevance
      if (compared == 0) x.simpleName.compareTo(y.simpleName) else compared
    }
  }
}