package controllers

import model.Element

object ScalaApplication {
  
  def intro: AnyRef = ???
  
  def withEmptyParams(): AnyRef = ???
  
  def pInt(a: Int): AnyRef = ???
  
  def pString(a: String): AnyRef = ???
  
  def pRef(a: Element): AnyRef = ???
  
  def overloaded(b: Int): AnyRef = ???
  
  def overloaded(c: String): AnyRef = ???
  
  def overloaded(b: Element): AnyRef = ???

  def overloaded(s: String, b: Element): AnyRef = ???
}

class ScalaClass {

  def intro: AnyRef = ???
  
  def withEmptyParams(): AnyRef = ???
  
  def pInt(a: Int): AnyRef = ???
  
  def pString(a: String): AnyRef = ???
  
  def pRef(a: Element): AnyRef = ???
  
  def overloaded(b: Int): AnyRef = ???
  
  def overloaded(c: String): AnyRef = ???
  
  def overloaded(b: Element): AnyRef = ???

  def overloaded(s: String, b: Element): AnyRef = ???
}