package scala.tools.eclipse.jdt.search

import org.junit.Test
import org.junit.Before
import org.junit.After

import org.junit.Assert._

import org.hamcrest.core.IsEqual._

import org.eclipse.core.resources.IWorkspace

import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.search._

import scala.tools.eclipse.pde.util.ScalaProject

import scala.tools.eclipse.jdt.search.util.BaseSearchEngineTest

class PatternMatchTest extends BaseSearchEngineTest(SearchPattern.R_PATTERN_MATCH) {

  @Before
  override def setUp : Unit = {
    super.setUp
  }

  override protected def setUpScalaProject(workspace: IWorkspace) = {
    scalaProject = ScalaProject.createIn(workspace).named("SearchEngineTest")
    scalaProject
      .addSourceFile("SearchEngineTest", "ScalaInDefaultPackage.scala")
      .addSourceFile("SearchEngineTest", "JavaInDefaultPackage.java")
      .addSourcePackageFolder("package1")
      .addSourcePackageFolder("package1/package2")
      .addSourceFile("SearchEngineTest", "package1/package2/ScalaInSubPackage.scala")
      .addSourceFile("SearchEngineTest", "package1/package2/JavaInSubPackage.java")
      .waitUntilAutomaticBuildCompletes
  }

  @Test
  def testPatternMatchOfJavaClass = {
    searchForMatch("*JavaIn*Package")
    assertThat(matchedElements.size, equalTo(2))
  }
  
  @Test
  def testPatternMatchOfScalaClass = {
    searchForMatch("*ScalaIn*Package")
    assertThat(matchedElements.size, equalTo(2))
  }
  
  @Test
  def testPatternMatchOfScalaObject = {
    searchForMatch("*ScalaIn*Package$")
    assertThat(matchedElements.size, equalTo(2))
  }
  
  @Test
  def testPatternMatchOfScalaAnonymousFunctions = {
    searchForMatch("*ScalaIn*Package$$*")
    assertThat(matchedElements.size, equalTo(4))
  }
  
  @After
  override def tearDown : Unit = {
    super.tearDown
  }
}