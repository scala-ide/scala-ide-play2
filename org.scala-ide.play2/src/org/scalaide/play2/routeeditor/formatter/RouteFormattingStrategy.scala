package org.scalaide.play2.routeeditor.formatter

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IAdaptable
import org.eclipse.jface.text._
import org.eclipse.jface.text.TextUtilities.getDefaultLineDelimiter
import org.eclipse.jface.text.formatter._
import org.eclipse.jface.text.source.ISourceViewer
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.text.undo.DocumentUndoManagerRegistry
import org.eclipse.text.edits.{ TextEdit => EclipseTextEdit, _ }
import org.eclipse.ui.texteditor.ITextEditor
import scalariform.formatter.ScalaFormatter
import scalariform.formatter.preferences._
import scalariform.parser.ScalaParserException
import scalariform.utils.TextEdit
import scala.tools.eclipse.properties.PropertyStore
import scala.tools.eclipse.ScalaPlugin
import scala.tools.eclipse.util.EclipseUtils._
import org.eclipse.core.resources.IResource
import org.scalaide.play2.routeeditor.lexical.RoutePartitionTokeniser
import scala.tools.eclipse.lexical.ScalaPartitionRegion
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer
import org.scalaide.play2.routeeditor.scanners.RoutePartitions.ROUTE_DEFAULT
import org.scalaide.play2.routeeditor.scanners.RoutePartitions.ROUTE_COMMENT
import org.scalaide.play2.routeeditor.scanners.RoutePartitions.ROUTE_URI
import org.scalaide.play2.routeeditor.scanners.RoutePartitions.ROUTE_ACTION
import org.scalaide.play2.routeeditor.scanners.RoutePartitions.ROUTE_HTTP
import org.scalaide.play2.routeeditor.rules.HTTPKeywordRule
import org.scalaide.play2.PlayPlugin

class RouteFormattingStrategy(val editor: ITextEditor) extends IFormattingStrategy with IFormattingStrategyExtension {

  private var document: IDocument = _

  private var regionOpt: Option[IRegion] = None

  def formatterStarts(context: IFormattingContext) {
    this.document = context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM).asInstanceOf[IDocument]
    this.regionOpt = Option(context.getProperty(FormattingContextProperties.CONTEXT_REGION).asInstanceOf[IRegion])
  }

  case class Route(httpVerb: Option[ScalaPartitionRegion], uri: Option[ScalaPartitionRegion], action: Option[ScalaPartitionRegion])

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
    val margin = PlayPlugin.prefStore.getInt(PlayPlugin.plugin.routeFormatterMarginId)
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
    //    val eclipseEdits = List(
    //        new ReplaceEdit(1, 1, "Hello"),
    //        new ReplaceEdit(2, 1, "World")
    //    )
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