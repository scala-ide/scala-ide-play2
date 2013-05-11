package org.scalaide.play2.routeeditor.completion.action

/** A simple wrapper for parsing action method invocation in a route file.
  *
  * An action method invocation in a route file is a fully qualified method (of return type Action -  
  * or Result, in Java), in a Play controller class.
  *
  * @param input The (possibly incomplete) action method invocation a route file.
  */
private[action] class ActionRouteInput(input: String) {

  /** If the passed `fullName` starts with a '@', the controller class is instantiated by the framework.
    *
    * @see Section '''Managed Controller classes instantiation''' in http://www.playframework.com/documentation/2.1.1/Highlights
    * for more details.
    */
  def isControllerClassInstantiation: Boolean = input.startsWith("@")

  /** Returns the `input` without the leading '@', if present. */
  def fullName: String = {
    if (isControllerClassInstantiation) input.substring(1)
    else input
  }

  /** Returns the prefix of the action method invocation. Hence, a valid prefix is either a fully
    * qualified package (could also be the empty package) or a class.
    *
    * @note If present, the leading '@' is removed.
    */
  def prefix: String = fullName.substring(0, Math.max(0, fullName.lastIndexOf('.')))

  /** Returns the suffix of the action method invocation. Hence, a valid suffix is either a
    * package, class or method name.
    *
    * @note If present, the leading '@' is removed.
    */
  def suffix: String = fullName.substring(fullName.lastIndexOf('.') + 1)

  override def toString: String = input
}