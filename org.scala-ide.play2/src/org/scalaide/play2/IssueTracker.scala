package org.scalaide.play2

object IssueTracker {
  final val Url: String = "https://github.com/scala-ide/scala-ide-play2/issues"

  def createATicketMessage: String = s"This is a bug. Please, file a ticket at ${Url}."
}