package scala.tools.eclipse.jdt.search

import org.junit.Test
import org.junit.Before
import org.junit.After

import org.junit.Assert._

import scala.tools.eclipse.pde.util.BackgroundJobWaiter

class SearchEngineTest extends BackgroundJobWaiter {
  
  @Before
  def setUp = {
    waitForBackgroundJobs
  }
  
  @Test
  def helloWorld = {
    assertTrue(true)
  }
  
  @After
  def tearDown = {
    waitForBackgroundJobs
  }
  
}