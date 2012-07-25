package org.scalaide.play2.routeeditor

import org.eclipse.swt.graphics.RGB
import org.eclipse.jface.text.rules.Token
import org.eclipse.jface.text.TextAttribute
import scala.collection.mutable.HashMap

object RouteColorConstants {
  val map = new HashMap[String, RGB]
  map += "ROUTE_COMMENT" -> new RGB(0, 128, 0);
  map += "ROUTE_URI" -> new RGB(0, 0, 128);
  map += "ROUTE_URI_DYNAMIC" -> new RGB(128, 128, 255);
  map += "ROUTE_ACTION" -> new RGB(128, 0, 0);
  map += "ROUTE_ACTION_PACKAGE" -> new RGB(196, 196, 196);
  map += "ROUTE_CLASS" -> new RGB(128, 255, 128);
  map += "ROUTE_METHOD" -> new RGB(255, 0, 0);
  map += "DEFAULT" -> new RGB(0, 0, 0);
  map += "HTTP_KEYWORD" -> new RGB(196, 0, 128);
  
  def getToken(rgb: String, manager: ColorManager) = {
    new Token(new TextAttribute(
				manager.getColor(map.get(rgb).get))){
      override def toString = rgb // For test purposes!
    }
  }
}