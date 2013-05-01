package org.scalaide.play2.properties

import org.eclipse.jface.util.PropertyChangeEvent

trait PropertyChangeHandler {
  def handlePropertyChangeEvent(event: PropertyChangeEvent): Unit
}