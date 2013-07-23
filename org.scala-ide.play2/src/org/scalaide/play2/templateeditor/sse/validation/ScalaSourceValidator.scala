package org.scalaide.play2.templateeditor.sse.validation

import scala.util.Try
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IJavaModelMarker
import org.eclipse.wst.sse.core.StructuredModelManager
import org.eclipse.wst.validation.internal.provisional.core.IReporter
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext
import org.eclipse.wst.validation.internal.provisional.core.IValidator
import org.scalaide.play2.templateeditor.TemplateCompilationUnitProvider


class ScalaSourceValidator extends IValidator {
  
  /* IValidator methods */
  
  def cleanup(report: IReporter) = {}
  
  def validate(helper: IValidationContext, reporter: IReporter) = {

    val wsroot = ResourcesPlugin.getWorkspace().getRoot()
    for {
      uri <- helper.getURIs()
      if !reporter.isCancelled()
      currentFile <- Option(wsroot.getFile(new Path(uri)))
      if currentFile.exists()
      model <- Try(StructuredModelManager.getModelManager().getModelForRead(currentFile))
    }{
      try {
        val markerType = IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER
        for {
          markers <- Try(currentFile.findMarkers(markerType, true, IResource.DEPTH_ONE))
          marker <- markers
        } marker.delete()
        
        val doc = model.getStructuredDocument()
        val compilationUnit = TemplateCompilationUnitProvider(false).fromFileAndDocument(currentFile, doc)
        for (error <- compilationUnit.reconcile(doc.get())) {
          val (priority, severity) =
            if (error.isError()) (IMarker.PRIORITY_HIGH, IMarker.SEVERITY_ERROR)
            else if (error.isWarning()) (IMarker.PRIORITY_NORMAL, IMarker.SEVERITY_WARNING)
            else (IMarker.PRIORITY_LOW, IMarker.SEVERITY_INFO)

          val marker = currentFile.createMarker(markerType)
          marker.setAttribute(IMarker.LINE_NUMBER, doc.getLineOfOffset(error.getSourceStart()) + 1)
          marker.setAttribute(IMarker.CHAR_START, error.getSourceStart())
          marker.setAttribute(IMarker.CHAR_END, error.getSourceStart() + (error.getSourceEnd() - error.getSourceStart() + 1))
          marker.setAttribute(IMarker.MESSAGE, error.getMessage())
          marker.setAttribute(IMarker.USER_EDITABLE, java.lang.Boolean.FALSE)
          marker.setAttribute(IMarker.PRIORITY, priority)
          marker.setAttribute(IMarker.SEVERITY, severity)
        }
      }
      finally {
        model.releaseFromRead()
      }
    }
  }
}
