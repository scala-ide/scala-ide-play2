package org.scalaide.play2.routeeditor.tools

import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.search.IJavaSearchConstants
import org.eclipse.jdt.core.search.IJavaSearchScope
import org.eclipse.jdt.core.search.SearchEngine
import org.eclipse.jdt.core.search.SearchPattern
import org.eclipse.jdt.internal.ui.search.JavaSearchScopeFactory
import org.eclipse.jdt.internal.core.search.BasicSearchEngine

object MethodFinder {
  // Usage:
  // val methodName = scu.getContents.subSequence(offset, offset+wordRegion.getLength).toString
  // val elements = MethodFinder.searchMethod(methodName, Array(), scu).toList
  def searchMethod(methodName: String, parameterTypes: Array[String]): Array[IJavaElement] = {
    //    val methodPattern = new MethodPattern(null, //selector
    //	null, //declaringQualification
    //	methodName.toCharArray, //declaringSimpleName
    //	null, //returnQualification
    //	null, //returnSimpleName
    //	null, //parameterQualifications
    //	parameterTypes map (_.toCharArray), //parameterSimpleNames
    //	null, //declaringType
    //	IJavaSearchConstants.DECLARATIONS,//limitTo
    //	0// matchRule
    //	)
    val paramsString = getParametersString(parameterTypes)
    val stringPattern = methodName + paramsString
    val methodPattern = SearchPattern.createPattern(stringPattern, IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH)
    searchMethod(methodPattern)
  }

  private def searchMethod(methodPattern: SearchPattern): Array[IJavaElement] = {
    val requestor = doSearch(methodPattern)
    requestor.matchedElements.toArray
  }

  private def doSearch(pattern: SearchPattern): MethodSearchRequestor = {
    val participants = Array(BasicSearchEngine.getDefaultSearchParticipant)
    val scope = createJavaSearchScope()
    val requestor = new MethodSearchRequestor
    val monitor = null
    new BasicSearchEngine().search(pattern, participants, scope, requestor, monitor)
    requestor
  }

  private[tools] def getParametersString(parameterTypes: Array[String]) = {
    if (parameterTypes == null || parameterTypes.length == 0)
      "()"
    else {
      val tmp = parameterTypes.foldLeft("")((prev, s) => prev + s + ",")
      val tmp2 = "(" + tmp.substring(0, tmp.length - 1) + ")"
      tmp2
    }
  }

  private def createJavaSearchScope(): IJavaSearchScope = {
    //    SearchEngine.createJavaSearchScope(Array[IJavaElement](scu.getJavaProject()), true)
    //    SearchEngine.createJavaSearchScope(Array[IJavaElement](), true)
    BasicSearchEngine.createWorkspaceScope()
  }
}