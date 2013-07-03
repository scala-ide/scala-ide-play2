package org.scalaide.play2.templateeditor.sse.style

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.sse.ui.internal.provisional.style.AbstractLineStyleProvider
import org.eclipse.wst.sse.ui.internal.provisional.style.LineStyleProvider
import org.scalaide.play2.templateeditor.sse.lexical.ScalaTextRegion

class ScalaLineStyleProvider(prefStore: IPreferenceStore) extends AbstractLineStyleProvider with LineStyleProvider {
  
   protected override def getAttributeFor(region: ITextRegion) = {
     region match {
       case scalaRegion: ScalaTextRegion => {
         scalaRegion.syntaxClass.getTextAttribute(getColorPreferences)
       }
       case _ => null
     }
   }
   
   protected override def getColorPreferences() = prefStore
   
   protected override def loadColors(): Unit =
     { /* ScalaSyntaxClass instances are stored internally in the scala text regions*/ }
}