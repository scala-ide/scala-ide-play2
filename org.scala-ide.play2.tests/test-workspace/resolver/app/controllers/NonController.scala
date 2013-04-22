package controllers

import play.api.mvc.{Controller, Action}

trait Action

object NonController {

  def/*!*/ index = new Action

  def/*!*/ post(id: Long) = "dummy"
}