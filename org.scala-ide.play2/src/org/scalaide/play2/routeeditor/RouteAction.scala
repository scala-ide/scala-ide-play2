package org.scalaide.play2.routeeditor

import org.eclipse.jface.text.IDocument
import org.scalaide.play2.routeeditor.lexical.RoutePartitions
import org.eclipse.jface.text.IRegion

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
  private final val ParameterWithTypeRegex = """.*:(.*)(\?=.*)?""".r

  /** Return the route action description at the location, using the document
   *  partitions.
   */
  def routeActionAt(document: IDocument, offset: Int): Option[RouteAction] = {
    val partition = document.getPartition(offset)
    if (RoutePartitions.isRouteAction(partition.getType())) {
      document.get(partition.getOffset(), partition.getLength()) match {
        case ActionWithoutParametersRegex(typeName, methodName) =>
          Some(RouteAction(typeName, methodName, Nil, partition))
        case ActionWithParametersRegex(typeName, methodName, parameters) =>
          Some(RouteAction(typeName, methodName, parseParameterTypes(parameters), partition))
        case s =>
          None
      }
    } else {
      None
    }
  }

  private def parseParameterTypes(parameters: String): List[String] = {
    if (parameters.isEmpty()) {
      Nil
    } else {
      parameters.split(",").toList.map {
        case ParameterWithTypeRegex(tpe, _) =>
          tpe.trim
        case s =>
          // if the type is not specified, the default is String
          "String"
      }
    }
  }

}

/** Description of an action in a route file.
 */
case class RouteAction(typeName: String, methodName: String, parameterTypes: List[String], region: IRegion) {

  def fullMethodName = "%s.%s".format(typeName, methodName)

}