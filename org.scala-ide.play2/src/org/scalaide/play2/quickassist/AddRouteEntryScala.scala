package org.scalaide.play2.quickassist

import org.scalaide.core.internal.jdt.model.ScalaCompilationUnit
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.jdt.ui.text.java.IInvocationContext
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.jdt.ui.text.java.IProblemLocation
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.Region
import org.eclipse.jface.text.templates.DocumentTemplateContext
import org.eclipse.jface.text.templates.Template
import org.eclipse.jface.text.templates.TemplateContextType
import org.eclipse.jface.text.templates.TemplateProposal
import org.eclipse.swt.graphics.Image
import org.eclipse.ui.PartInitException
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.ide.IDE
import org.scalaide.editor.Editor
import org.scalaide.editor.EditorUI
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.routeeditor.RouteEditor
import org.scalaide.play2.routeeditor.formatter.RouteFormattingStrategy
import org.scalaide.play2.util.Images

class AddRouteEntryScala extends IQuickAssistProcessor {
  private lazy val scalaResolver = new ScalaControllerMethodResolver
  private lazy val javaResolver = new JavaControllerMethodResolver

  override def hasAssists(context: IInvocationContext): Boolean = {
    context.getCompilationUnit() match {
      case scu: ScalaCompilationUnit =>
        scalaResolver.getControllerMethod(scu, context.getSelectionOffset()).isDefined
      case cu => javaResolver.getControllerMethod(cu, context.getSelectionOffset()).isDefined
    }
  }

  /** Return quick assists for adding an entry in the route file.
   *
   *  TODO: Check that a similar entry does not already exist.
   */
  override def getAssists(context: IInvocationContext, locations: Array[IProblemLocation]): Array[IJavaCompletionProposal] = {
    val unit = context.getCompilationUnit()
    val (resolver, prj) = unit match {
      case scu: ScalaCompilationUnit => (scalaResolver, scu.scalaProject.underlying)
      case _                         => (javaResolver, unit.getJavaProject().getProject())
    }
    (for (cmeth <- resolver.getControllerMethod(unit, context.getSelectionOffset())) yield {
      createAssistProposals(prj, cmeth)
    }) getOrElse Array()
  }

  /** Create assist proposals to add the given controller method to all route files in the project.
   *
   *  This method is language-agnostic, so it should be used for assists in both Java and Scala files.
   */
  private def createAssistProposals(project: IProject, cmeth: ControllerMethod): Array[IJavaCompletionProposal] = {
    val image = PlayPlugin.instance.getImageRegistry().get(Images.ROUTES_ICON)

    for (routeFile <- getRoutePaths(project).toArray)
      yield BaseAssistProposal(s"Add entry to ${routeFile.getName}", image) { _ =>
      insertRouteEntry(routeFile, cmeth.toRouteCallSyntax)
    }
  }

  private def getRoutePaths(prj: IProject): Seq[IFile] = {
    def isRouteFile(name: String) = (name == "routes") || (name.endsWith(".routes"))
    for {
      confFolder <- Option(prj.getFolder("conf")).toSeq
      file <- confFolder.members()
      if (file.exists && file.isInstanceOf[IFile] && isRouteFile(file.getName()))
    } yield file.asInstanceOf[IFile]
  }

  /** Insert a route entry in the given route file, in the given project.
   *
   *  This method opens the route editor and appends a new entry, entering the template,
   *  linked-mode UI.
   *
   *  @param routeFile a route file
   *  @param routeCall a controller method call, e.g. {{{controllers.Application.index()}}}
   */
  private def insertRouteEntry(routeFile: IFile, routeCall: String): Unit = try {
    for {
      part <- Option(IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), routeFile))
    } part match {
      case routeEditor: RouteEditor =>
        Editor.getEditorDocument(routeEditor) foreach { routeDoc =>
          ensureNewLine(routeDoc)
          val template = newTemplateProposal(routeDoc, /* image = */ null, routeCall)
          template.apply(routeEditor.getViewer, ' ', 0, routeDoc.getLength())
        }
      case _ => // do nothing in case we're not in the route editor
    }
  } catch {
    case _: PartInitException | _: IllegalStateException =>
    // couldn't open the editor for some reason, just don't crash
  }

  /** Ensure a fresh new line at the end of the document. */
  private def ensureNewLine(doc: IDocument) {
    val lineSep = Option(doc.getLineDelimiter(0)).getOrElse(EditorUI.defaultLineSeparator)
    val lastLineRegion = doc.getLineInformationOfOffset(doc.getLength())
    val lastLine = doc.get(lastLineRegion.getOffset(), lastLineRegion.getLength())
    if (lastLine.trim != "")
      doc.replace(doc.getLength(), 0, lineSep)
  }

  // TODO: Everything below can be extracted when we add Template support for routes
  // and expose them through completions (this is a platform-supported feature)

  private val contextTypeId = "routeContextId"
  private def routeEntryTemplateString(implicit doc: IDocument) = {
    import RouteFormattingStrategy._
    val lines = getLines(doc)
    "${httpMethod}" + spaces(getMaxHttpVerbLength(lines)) + "${url}" + spaces(getMaxUriLength(lines)) + "${call}"
  }
  private def routeEntryTemplate(implicit doc: IDocument) =
    new Template("newRoute", "Add a new route", contextTypeId, routeEntryTemplateString, true)

  private def newTemplateContext(doc: IDocument) = {
    new DocumentTemplateContext(new TemplateContextType(contextTypeId), doc, doc.getLength, 0)
  }

  /** Create a template proposal that can be used to insert a route-entry template in a document.
   *
   *  We reuse here the logic to create the linked-mode UI (tabbing through fields, etc) and bridge
   *  towards full template support in route files.
   */
  private def newTemplateProposal(doc: IDocument, image: Image, call: String) = {
    val ctx = newTemplateContext(doc)
    ctx.setVariable("httpMethod", "GET")
    ctx.setVariable("call", call)
    new TemplateProposal(routeEntryTemplate(doc), ctx, /* not used */ new Region(0, 0), image)
  }
}