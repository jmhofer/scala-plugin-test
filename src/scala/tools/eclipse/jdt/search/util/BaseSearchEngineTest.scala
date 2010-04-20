package scala.tools.eclipse.jdt.search.util

import java.util.concurrent.Semaphore

import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.ResourcesPlugin

import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.search._

import scala.tools.eclipse.pde.util.BackgroundJobWaiter
import scala.tools.eclipse.pde.util.ScalaProject

/**
 * Extend this if you want to create new search engine tests.
 * You have to call setUp in your test setup and tearDown in your test teardown.
 * You have to override setUpScalaProject to fit your needs. 
 * You can use searchForMatch freely in your tests.
 */
abstract class BaseSearchEngineTest(matchType: Int) extends BackgroundJobWaiter {

  private val searchRunning = new Semaphore(1)

  private var searchEngine: SearchEngine = null
  private var searchScope: IJavaSearchScope = null

  protected var scalaProject: ScalaProject = null
  protected var matchedElements = List[IJavaElement]()
  
  def setUp : Unit = {
    waitForBackgroundJobs
    setUpSearchEngine
    scalaProject = setUpScalaProject(ResourcesPlugin.getWorkspace)
  }

  private def setUpSearchEngine = {
    searchEngine = new SearchEngine 
    searchScope = SearchEngine.createWorkspaceScope
    matchedElements = List[IJavaElement]()
  }
  
  protected def setUpScalaProject(workspace: IWorkspace): ScalaProject
  
  protected def searchForMatch(className: String) = {
    val scalaSearchPattern = SearchPattern.createPattern(
        className, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, 
        matchType)
    val searchHandler = new SearchHandler(
            matchedElements ::= _, 
            searchRunning.release)
    
    searchRunning.acquire
    searchEngine.search(scalaSearchPattern, 
        Array[SearchParticipant](SearchEngine.getDefaultSearchParticipant), 
        searchScope, searchHandler, null)
        
    waitForBackgroundJobs
    waitForEndOfSearch
  }
  
  private def waitForEndOfSearch = {
    searchRunning.acquire
    searchRunning.release
  }
  
  def tearDown : Unit = {
    scalaProject.remove
    waitForBackgroundJobs
  }
}