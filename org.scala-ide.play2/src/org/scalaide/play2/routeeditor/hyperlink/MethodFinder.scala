package org.scalaide.play2.routeeditor.hyperlink

import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.search.IJavaSearchConstants
import org.eclipse.jdt.core.search.IJavaSearchScope
import org.eclipse.jdt.core.search.SearchPattern
import org.eclipse.jdt.internal.core.search.BasicSearchEngine
import scala.Array.apply
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaProject

/**
 * Find Java methods in a given project.
 */
class MethodFinder(project: IJavaProject) {
  /**
   * Returns an array of method elements which matches with the
   * criterion.
   * 
   * @param methodName the name of method which we'd like to find
   * @param parameterTypes array of type of parameter of desired method
   * @return array of method element which matches the search
   */
  def searchMethod(methodName: String, parameterTypes: Array[String]): Array[IJavaElement] = {
    val stringPattern = methodName + getParametersString(parameterTypes)
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
    new BasicSearchEngine().search(pattern, participants, scope, requestor, new NullProgressMonitor)
    requestor
  }

  private[hyperlink] def getParametersString(parameterTypes: Array[String]) = {
    parameterTypes.mkString("(", ",", ")")
  }

  private def createJavaSearchScope(): IJavaSearchScope = {
    BasicSearchEngine.createJavaSearchScope(Array(project))
  }
}