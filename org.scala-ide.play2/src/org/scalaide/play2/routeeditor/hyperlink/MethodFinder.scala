package org.scalaide.play2.routeeditor.hyperlink

import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.search.IJavaSearchConstants
import org.eclipse.jdt.core.search.IJavaSearchScope
import org.eclipse.jdt.core.search.SearchPattern
import org.eclipse.jdt.internal.core.search.BasicSearchEngine
import scala.Array.apply

object MethodFinder {
  /**
   * Returns an array of method elements which matches with the
   * criterion.
   * 
   * @param methodName the name of method which we'd like to find
   * @param parameterTypes array of type of parameter of desired method
   * @return array of method element which matches the search
   */
  def searchMethod(methodName: String, parameterTypes: Array[String]): Array[IJavaElement] = {
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

  private[hyperlink] def getParametersString(parameterTypes: Array[String]) = {
    if (parameterTypes == null || parameterTypes.length == 0)
      "()"
    else {
      val tmp = parameterTypes.foldLeft("")((prev, s) => prev + s + ",")
      val tmp2 = "(" + tmp.substring(0, tmp.length - 1) + ")"
      tmp2
    }
  }

  private def createJavaSearchScope(): IJavaSearchScope = {
    BasicSearchEngine.createWorkspaceScope()
  }
}