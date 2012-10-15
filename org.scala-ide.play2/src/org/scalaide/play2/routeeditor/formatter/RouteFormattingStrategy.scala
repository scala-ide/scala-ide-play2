package org.scalaide.play2.routeeditor.formatter

import scala.collection.mutable.ArrayBuffer
import scala.tools.eclipse.lexical.ScalaPartitionRegion
import scala.tools.eclipse.util.EclipseUtils.adaptableToPimpedAdaptable

import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.IAdaptable
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
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
 * formatter of route files
 */
class RouteFormattingStrategy(val editor: ITextEditor) extends IFormattingStrategy with IFormattingStrategyExtension {

  private var document: IDocument = _

  private var regionOpt: Option[IRegion] = None

  def formatterStarts(context: IFormattingContext) {
    this.document = context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM).asInstanceOf[IDocument]
    this.regionOpt = Option(context.getProperty(FormattingContextProperties.CONTEXT_REGION).asInstanceOf[IRegion])
  }

  /**
   * a helper class for each line of code
   */
  case class Route(httpVerb: Option[ScalaPartitionRegion], uri: Option[ScalaPartitionRegion], action: Option[ScalaPartitionRegion])

  /**
   * Returns lines of code which should be formatted.
   * Formatted lines are the lines which are complete, which means
   * have all 3 parts.
   * @param regions     all of regions of route file
   */
  def getRoutes(regions: List[ScalaPartitionRegion]): List[Route] = {
    val routes = new ArrayBuffer[Route]
    sealed trait State
    case object Http extends State
    case object Uri extends State
    case object Action extends State
    var state: State = Http
    var httpVerb: Option[ScalaPartitionRegion] = None
    var uri: Option[ScalaPartitionRegion] = None
    var action: Option[ScalaPartitionRegion] = None

    def reset = {
      state = Http
      httpVerb = None
      uri = None
      action = None
    }

    for (region <- regions) {
      region.contentType match {
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
        case ROUTE_DEFAULT => {
          reset
        }
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

  def getMaxHttpVerbLength(lines: List[Route]): Int = {
    val lengthList = lines map (_.httpVerb.get.length)
    lengthList.max
  }

  def spaces(times: Int) = {
    var result = ""
    for (i <- 1 to times) {
      result += " "
    }
    result
  }

  def format() {
    val regions = RoutePartitionTokeniser.tokenise(document.get)
    val lines = getRoutes(regions)
    val margin = PlayPlugin.prefStore.getInt(PlayPlugin.RouteFormatterMarginId)
    val maxHttpVerbLength = (lines map (_.httpVerb.get.length)).max + margin
    val maxUriLength = (lines map (_.uri.get.length)).max + margin
    val eclipseEdits = lines flatMap { route =>
      val httpEnd = route.httpVerb.get.end + 1
      val httpSpaceLength = route.uri.get.start - httpEnd
      val httpNeededLength = maxHttpVerbLength - route.httpVerb.get.length
      val uriEnd = route.uri.get.end + 1
      val uriSpaceLength = route.action.get.start - uriEnd
      val uriNeededLength = maxUriLength - route.uri.get.length
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

  private def getProject = editor.getEditorInput.asInstanceOf[IAdaptable].adaptTo[IResource].getProject

  def format(content: String, isLineStart: Boolean, indentation: String, positions: Array[Int]): String = null

  def formatterStarts(initialIndentation: String) {}

}