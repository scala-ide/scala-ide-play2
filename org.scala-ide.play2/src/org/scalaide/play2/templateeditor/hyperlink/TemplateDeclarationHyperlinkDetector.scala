package org.scalaide.play2.templateeditor.hyperlink

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlink
import org.eclipse.jdt.ui.actions.OpenAction
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.eclipse.ui.texteditor.ITextEditor
import org.scalaide.util.ScalaWordFinder
import org.scalaide.core.compiler.InteractiveCompilationUnit
import org.eclipse.jdt.internal.core.JavaProject
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner
import org.scalaide.play2.templateeditor.lexical.TemplatePartitions
import org.eclipse.jface.text.Region
import org.scalaide.play2.templateeditor.compiler.PositionHelper
import org.scalaide.play2.templateeditor.TemplateCompilationUnit
import org.scalaide.ui.editor.SourceConfiguration


object TemplateDeclarationHyperlinkDetector {
  def apply()= SourceConfiguration.scalaDeclarationDetector
}