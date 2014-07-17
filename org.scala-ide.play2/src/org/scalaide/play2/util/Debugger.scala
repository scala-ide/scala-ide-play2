package org.scalaide.play2.util

import javax.swing.JFrame

object Debugger {
  def show(message: String) {
    new JFrame(message){
      setVisible(true)
      setSize(800, 100)
    }
  }
}