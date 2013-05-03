package controllers

import play.api.mvc.{Controller, Action}

object Application {

  def/*!*/ index = Action {
  }

  def/*!*/ post(id: Long) = Action {
  }

  def/*!*/ postText(text: String, id: Int) = Action {
  }

  def internalPostText1(text: String, /*!*/id: Char) = Action {
  }

  def internalPostText2(text: String, id: Short) = Action {
    /*!*/
  }
}