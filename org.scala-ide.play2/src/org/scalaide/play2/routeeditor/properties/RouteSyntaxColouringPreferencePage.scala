package org.scalaide.play2.routeeditor.properties

import org.scalaide.play2.routeeditor.RouteSyntaxClasses.ALL_SYNTAX_CLASSES
import org.scalaide.play2.routeeditor.RouteSyntaxClasses.routeOtherCategory
import org.scalaide.play2.properties.SyntaxColouringPreferencePage

class RouteSyntaxColouringPreferencePage extends SyntaxColouringPreferencePage(ALL_SYNTAX_CLASSES, 
    routeOtherCategory, 
    RouteSyntaxColouringTreeContentAndLabelProvider, 
    RouteSyntaxColouringPreviewText.previewText, 
    RoutePreviewerFactory)


