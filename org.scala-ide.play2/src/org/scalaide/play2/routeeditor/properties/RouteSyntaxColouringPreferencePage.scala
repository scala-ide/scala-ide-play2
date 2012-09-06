package org.scalaide.play2.routeeditor.properties

import org.scalaide.play2.routeeditor.RouteSyntaxClasses.ALL_SYNTAX_CLASSES
import org.scalaide.play2.routeeditor.RouteSyntaxClasses.routeOtherCategory
import org.scalaide.play2.properties.SyntaxColouringPreferencePage

//import SyntaxColouringPreviewText.ColouringLocation

/**
 * @see org.eclipse.jdt.internal.ui.preferences.JavaEditorColoringConfigurationBlock
 */
class RouteSyntaxColouringPreferencePage extends SyntaxColouringPreferencePage(ALL_SYNTAX_CLASSES, 
    routeOtherCategory, 
    RouteSyntaxColouringTreeContentAndLabelProvider, 
    RouteSyntaxColouringPreviewText.previewText, 
    RoutePreviewerFactory)


