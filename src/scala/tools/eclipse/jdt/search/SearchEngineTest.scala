package scala.tools.eclipse.jdt.search

import org.junit.Test
import org.junit.Before
import org.junit.After

import org.junit.Assert._

import org.eclipse.core.resources.ResourcesPlugin

import scala.tools.eclipse.pde.util.BackgroundJobWaiter
import scala.tools.eclipse.pde.util.ScalaProject

class SearchEngineTest extends BackgroundJobWaiter {

  var scalaProject: ScalaProject = null
  
  @Before
  def setUp : Unit = {
    waitForBackgroundJobs
    val workspace = ResourcesPlugin.getWorkspace
    scalaProject = ScalaProject.createIn(workspace).named("SearchEngineTest")
    scalaProject
      .addSourceFile("SearchEngineTest", "ScalaInDefaultPackage.scala")
      .waitUntilAutomaticBuildCompletes
  }
  
  @Test
  def helloWorld = {
    assertTrue(true)
  }
  
  @After
  def tearDown : Unit = {
//    scalaProject.remove
    waitForBackgroundJobs
  }
  
}