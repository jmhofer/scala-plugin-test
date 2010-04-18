package scala.tools.eclipse.jdt.search

import java.util.concurrent.Semaphore

import org.junit.Test
import org.junit.Before
import org.junit.After

import org.junit.Assert._

import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.ResourcesPlugin

import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.search._

import scala.tools.eclipse.pde.util.BackgroundJobWaiter
import scala.tools.eclipse.pde.util.ScalaProject

class SearchEngineTest extends BackgroundJobWaiter {

  class SearchHandler extends SearchRequestor {
    override def acceptSearchMatch(searchMatch: SearchMatch) = {
      searchMatch.getElement match {
        case javaElement: IJavaElement => {
          matchedElements ::= javaElement
          println("match found: %s".format(javaElement))
        }
      }
    }
    
    override def endReporting = {
      searchRunning.release
    }
  }
  
  val searchRunning = new Semaphore(1)

  var scalaProject: ScalaProject = null
  var searchEngine: SearchEngine = null
  var searchScope: IJavaSearchScope = null
  var matchedElements = List[IJavaElement]()
  
  @Before
  def setUp : Unit = {
    waitForBackgroundJobs
    setUpScalaProject(ResourcesPlugin.getWorkspace)
    setUpSearchEngine
  }

  private def setUpScalaProject(workspace: IWorkspace) = {
    scalaProject = ScalaProject.createIn(workspace).named("SearchEngineTest")
    scalaProject
      .addSourceFile("SearchEngineTest", "ScalaInDefaultPackage.scala")
      .waitUntilAutomaticBuildCompletes
  }

  private def setUpSearchEngine = {
    searchEngine = new SearchEngine 
    searchScope = SearchEngine.createWorkspaceScope
    matchedElements = List[IJavaElement]()
  }
  
  @Test
  def testExactMatchOfClassInDefaultPackage = {
    val searchPattern = SearchPattern.createPattern(
        "ScalaInDefaultPackage", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, 
        SearchPattern.R_EXACT_MATCH)
        
    searchRunning.acquire
    searchEngine.search(searchPattern, 
        Array[SearchParticipant](SearchEngine.getDefaultSearchParticipant), 
        searchScope, new SearchHandler, null)
        
    waitForEndOfSearch
    assertEquals(1, matchedElements.size)
  }
  
  private def waitForEndOfSearch = {
    searchRunning.acquire
    searchRunning.release
  }
  
  @After
  def tearDown : Unit = {
    scalaProject.remove
    waitForBackgroundJobs
  }
  
}