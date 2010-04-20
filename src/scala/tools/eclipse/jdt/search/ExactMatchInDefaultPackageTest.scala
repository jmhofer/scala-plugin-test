package scala.tools.eclipse.jdt.search

import java.util.concurrent.Semaphore

import org.junit.Test
import org.junit.Before
import org.junit.After

import org.junit.Assert._

import org.hamcrest.core.IsEqual._

import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.ResourcesPlugin

import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.search._

import scala.tools.eclipse.pde.util.BackgroundJobWaiter
import scala.tools.eclipse.pde.util.ScalaProject

import scala.tools.eclipse.jdt.search.util.SearchHandler

class ExactMatchInDefaultPackageTest extends BackgroundJobWaiter {

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
      .addSourceFile("SearchEngineTest", "JavaInDefaultPackage.java")
      .waitUntilAutomaticBuildCompletes
  }

  private def setUpSearchEngine = {
    searchEngine = new SearchEngine 
    searchScope = SearchEngine.createWorkspaceScope
    matchedElements = List[IJavaElement]()
  }
  
  @Test
  def testExactMatchOfJavaClassInDefaultPackage = {
    searchForExactMatch("JavaInDefaultPackage")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  @Test
  def testExactMatchOfScalaClassInDefaultPackage = {
    searchForExactMatch("ScalaInDefaultPackage")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  @Test
  def testExactMatchOfScalaObjectInDefaultPackage = {
    searchForExactMatch("ScalaInDefaultPackage$")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  @Test
  def testExactMatchOfScalaAnonymousFunctionInDefaultPackage = {
    searchForExactMatch("ScalaInDefaultPackage$$anonfun$1")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  private def searchForExactMatch(className: String) = {
    val scalaSearchPattern = SearchPattern.createPattern(
        className, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, 
        SearchPattern.R_EXACT_MATCH)
        
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
  
  @After
  def tearDown : Unit = {
    scalaProject.remove
    waitForBackgroundJobs
  }
  
}