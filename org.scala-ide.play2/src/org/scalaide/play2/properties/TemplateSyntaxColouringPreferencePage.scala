package org.scalaide.play2.properties

import org.scalaide.play2.templateeditor.TemplateSyntaxClasses.ALL_SYNTAX_CLASSES
import org.scalaide.play2.templateeditor.TemplateSyntaxClasses.scalaCategory

//import SyntaxColouringPreviewText.ColouringLocation

/**
 * @see org.eclipse.jdt.internal.ui.preferences.JavaEditorColoringConfigurationBlock
 */
class TemplateSyntaxColouringPreferencePage extends SyntaxColouringPreferencePage(ALL_SYNTAX_CLASSES, 
    scalaCategory, 
    TemplateSyntaxColouringTreeContentAndLabelProvider, 
    TemplateSyntaxColouringPreviewText.previewText, 
    TemplatePreviewerFactory)


