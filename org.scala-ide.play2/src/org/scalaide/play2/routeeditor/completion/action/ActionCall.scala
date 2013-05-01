package org.scalaide.play2.routeeditor.completion.action

private[action] class ActionCall(input: String) {
  
  /** If the passed `fullName` starts with a '@', the controller class is instantiated by the framework.
    * @see Section '''Managed Controller classes instantiation''' in http://www.playframework.com/documentation/2.1.1/Highlights
    * for more details.
    */
  def isControllerClassInstantiation: Boolean = input.startsWith("@")

  def actionMethodCall: String = {
    if(isControllerClassInstantiation) input.substring(1)
    else input
  }
  
  def prefix: String = actionMethodCall.substring(0, actionMethodCall.lastIndexOf('.'))
  def suffix: String = actionMethodCall.substring(actionMethodCall.lastIndexOf('.') + 1)
  
  override def toString: String = input  
}