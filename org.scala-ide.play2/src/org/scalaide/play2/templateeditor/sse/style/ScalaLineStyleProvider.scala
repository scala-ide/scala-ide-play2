package org.scalaide.play2.templateeditor.sse.style

import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.wst.html.ui.internal.style.LineStyleProviderForHTML
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion
import org.eclipse.wst.sse.ui.internal.provisional.style.LineStyleProvider
import org.scalaide.play2.templateeditor.sse.lexical.TemplateTextRegion

class ScalaLineStyleProvider(prefStore: IPreferenceStore) extends LineStyleProviderForHTML with LineStyleProvider {
  
   protected override def getAttributeFor(region: ITextRegion) = {
     region match {
       case scalaRegion: TemplateTextRegion => {
         scalaRegion.syntaxClass.getTextAttribute(getColorPreferences)
       }
       case _ => super.getAttributeFor(region)
     }
   }
   
   protected override def getColorPreferences() = prefStore
}
