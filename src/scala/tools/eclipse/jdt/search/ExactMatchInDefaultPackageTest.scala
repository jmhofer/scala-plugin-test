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

class ExactMatchInDefaultPackageTest extends BaseSearchEngineTest(SearchPattern.R_EXACT_MATCH) {

  @Before
  override def setUp : Unit = {
    super.setUp
  }

  override protected def setUpScalaProject(workspace: IWorkspace) = {
    scalaProject = ScalaProject.createIn(workspace).named("SearchEngineTest")
    scalaProject
      .addSourceFile("SearchEngineTest", "ScalaInDefaultPackage.scala")
      .addSourceFile("SearchEngineTest", "JavaInDefaultPackage.java")
      .waitUntilAutomaticBuildCompletes
  }

  @Test
  def testExactMatchOfJavaClassInDefaultPackage = {
    searchForMatch("JavaInDefaultPackage")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  @Test
  def testExactMatchOfScalaClassInDefaultPackage = {
    searchForMatch("ScalaInDefaultPackage")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  @Test
  def testExactMatchOfScalaObjectInDefaultPackage = {
    searchForMatch("ScalaInDefaultPackage$")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  @Test
  def testExactMatchOfScalaAnonymousFunctionInDefaultPackage = {
    searchForMatch("ScalaInDefaultPackage$$anonfun$1")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  @After
  override def tearDown : Unit = {
    super.tearDown
  }
}