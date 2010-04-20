package scala.tools.eclipse.jdt.search.util

import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.search._

class SearchHandler(matchFound: IJavaElement => Unit, searchDone: => Unit) 
    extends SearchRequestor {
  
  override def acceptSearchMatch(searchMatch: SearchMatch) = {
    searchMatch.getElement match {
      case javaElement: IJavaElement => {
        println("match found: %s".format(javaElement))
        matchFound(javaElement)
//        matchedElements ::= javaElement
      }
    }
  }
  
  override def endReporting = {
    searchDone
//    searchRunning.release
  }
}

