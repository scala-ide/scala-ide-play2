package org.scalaide.play2.routeeditor

import org.eclipse.jface.text.IDocument
import org.scalaide.play2.routeeditor.lexical.RoutePartitions
import org.eclipse.jface.text.IRegion
import org.scalaide.play2.quickassist.ControllerMethod

object RouteAction {

  /** Regex for the an action with parameters.
   *  "package.Object.action(...xxx...)"
   */
  private final val ActionWithParametersRegex = """([^\(]*)\.([^\.\(]*)\(([^\)]*)\)""".r

  /** Regex for the an action without parameters.
   *  "package.Object.action"
   */
  private final val ActionWithoutParametersRegex = """([^\(]*)\.([^\.\(]*)""".r

  /** Regex for the individual parameters.
   *  The support format is: "page: Int ?= 1"
   */
  private final val ParameterWithTypeRegex = """(.*):([^\?]*)(\?=.*)?""".r

  private final val ParameterWithoutTypeRegex = """([^\?]*)(\?=.*)?""".r

  /** Return the route action description at the location, using the document
   *  partitions.
   */
  def routeActionAt(document: IDocument, offset: Int): Option[RouteAction] = {
    val partition = document.getPartition(offset)
    if (RoutePartitions.isRouteAction(partition.getType())) {
      document.get(partition.getOffset(), partition.getLength()) match {
        case ActionWithoutParametersRegex(typeName, methodName) =>
          Some(new RouteAction(typeName, methodName, Nil, partition))
        case ActionWithParametersRegex(typeName, methodName, parameters) =>
          Some(new RouteAction(typeName, methodName, parseParameterTypes(parameters), partition))
        case s =>
          None
      }
    } else {
      None
    }
  }

  private def parseParameterTypes(parameters: String): List[(String, String)] = {
    if (parameters.isEmpty()) {
      Nil
    } else {
      parameters.split(",").toList.map {
        case ParameterWithTypeRegex(name, tpe, _) =>
          (name.trim, tpe.trim)
        case ParameterWithoutTypeRegex(name, _) =>
          // if the type is not specified, the default is String
          (name.trim, "String")
      }
    }
  }

}

/** Description of an action in a route file.
 */
class RouteAction(typeName: String, methodName: String, params: List[(String, String)], val region: IRegion) extends ControllerMethod(typeName, methodName, params)