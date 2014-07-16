package org.scalaide.play2.routeeditor.formatter

import scala.collection.mutable.ArrayBuffer
import org.scalaide.util.internal.eclipse.EclipseUtils
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.IAdaptable
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.TypedRegion
import org.eclipse.jface.text.formatter.FormattingContextProperties
import org.eclipse.jface.text.formatter.IFormattingContext
import org.eclipse.jface.text.formatter.IFormattingStrategy
import org.eclipse.jface.text.formatter.IFormattingStrategyExtension
import org.eclipse.text.edits.MultiTextEdit
import org.eclipse.text.edits.ReplaceEdit
import org.eclipse.text.edits.{TextEdit => EclipseTextEdit}
import org.eclipse.text.edits.TextEditProcessor
import org.eclipse.text.undo.DocumentUndoManagerRegistry
import org.eclipse.ui.texteditor.ITextEditor
import org.scalaide.play2.PlayPlugin
import org.scalaide.play2.routeeditor.lexical.RoutePartitionTokeniser
import org.scalaide.play2.routeeditor.lexical.RoutePartitions.ROUTE_ACTION
import org.scalaide.play2.routeeditor.lexical.RoutePartitions.ROUTE_COMMENT
import org.scalaide.play2.routeeditor.lexical.RoutePartitions.ROUTE_DEFAULT
import org.scalaide.play2.routeeditor.lexical.RoutePartitions.ROUTE_HTTP
import org.scalaide.play2.routeeditor.lexical.RoutePartitions.ROUTE_URI

/**
 * Utility methods and classes that aren't dependent on a specific RouteFormattingStrategy instance
 */
object RouteFormattingStrategy {

  /**
   * a helper class for each line of code
   */
  case class Route(httpVerb: Option[TypedRegion], uri: Option[TypedRegion], action: Option[TypedRegion])

  /**
   * Returns lines of code which should be formatted.
   * Formatted lines are the lines which are complete, which means
   * have all 3 parts.
   * @param regions     all of regions of route file
   */
  private def getRoutes(regions: List[TypedRegion]): List[Route] = {
    val routes = new ArrayBuffer[Route]
    sealed trait State
    case object Http extends State
    case object Uri extends State
    case object Action extends State
    var state: State = Http
    var httpVerb: Option[TypedRegion] = None
    var uri: Option[TypedRegion] = None
    var action: Option[TypedRegion] = None

    def reset = {
      state = Http
      httpVerb = None
      uri = None
      action = None
    }

    for (region <- regions) {
      region.getType() match {
        case ROUTE_HTTP => {
          state match {
            case Http =>
            case _ => reset
          }
          httpVerb = Some(region)
          state = Uri
        }
        case ROUTE_COMMENT => {
          reset
        }
        case ROUTE_DEFAULT =>
        case ROUTE_URI => {
          state match {
            case Uri => {
              uri = Some(region)
              state = Action
            }
            case _ => {
              reset
            }
          }
        }
        case ROUTE_ACTION => {
          state match {
            case Action => {
              action = Some(region)
              routes += Route(httpVerb, uri, action)
            }
            case _ => {
            }
          }
          reset
        }
      }
    }

    routes.toList
  }

  private def getRegions(document: IDocument): List[TypedRegion] =
    (new RoutePartitionTokeniser()).tokenise(document)

  def getLines(document: IDocument): List[Route] =
    getRoutes(getRegions(document))

  def getMaxHttpVerbLength(lines: List[Route]): Int =
    (lines map (_.httpVerb.get.getLength())).max

  def getMaxUriLength(lines: List[Route]): Int =
    (lines map (_.uri.get.getLength())).max

  def spaces(times: Int) = " " * times
}

/**
 * formatter of route files
 */
class RouteFormattingStrategy(val editor: ITextEditor) extends IFormattingStrategy with IFormattingStrategyExtension {

  private var document: IDocument = _

  private var regionOpt: Option[IRegion] = None

  def formatterStarts(context: IFormattingContext) {
    this.document = context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM).asInstanceOf[IDocument]
    this.regionOpt = Option(context.getProperty(FormattingContextProperties.CONTEXT_REGION).asInstanceOf[IRegion])
  }

  def format() {
    import RouteFormattingStrategy._

    val lines = getLines(document)
    val margin = PlayPlugin.preferenceStore.getInt(PlayPlugin.RouteFormatterMarginId)
    val maxHttpVerbLength = getMaxHttpVerbLength(lines) + margin
    val maxUriLength = getMaxUriLength(lines) + margin
    val eclipseEdits = lines flatMap { route =>
      val httpEnd = route.httpVerb.get.getOffset() + route.httpVerb.get.getLength()
      val httpSpaceLength = route.uri.get.getOffset() - httpEnd
      val httpNeededLength = maxHttpVerbLength - route.httpVerb.get.getLength()
      val uriEnd = route.uri.get.getOffset() + route.uri.get.getLength()
      val uriSpaceLength = route.action.get.getOffset() - uriEnd
      val uriNeededLength = maxUriLength - route.uri.get.getLength()
      List(new ReplaceEdit(httpEnd, httpSpaceLength, spaces(httpNeededLength)),
        new ReplaceEdit(uriEnd, uriSpaceLength, spaces(uriNeededLength)))
    }
    applyEdits(eclipseEdits)
  }

  private def applyEdits(edits: List[EclipseTextEdit]) {
    val multiEdit = new MultiTextEdit
    multiEdit.addChildren(edits.toArray)

    val undoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(document)
    undoManager.beginCompoundChange()
    new TextEditProcessor(document, multiEdit, EclipseTextEdit.NONE).performEdits
    undoManager.endCompoundChange()
  }

  def formatterStops() {
    this.document = null
    this.regionOpt = None
  }

  def format(content: String, isLineStart: Boolean, indentation: String, positions: Array[Int]): String = null

  def formatterStarts(initialIndentation: String) {}

}