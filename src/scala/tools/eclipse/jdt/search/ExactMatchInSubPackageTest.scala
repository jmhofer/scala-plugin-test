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

class ExactMatchInSubPackageTest extends BaseSearchEngineTest(SearchPattern.R_EXACT_MATCH) {

  @Before
  override def setUp : Unit = {
    super.setUp
  }

  override protected def setUpScalaProject(workspace: IWorkspace) = {
    scalaProject = ScalaProject.createIn(workspace).named("SearchEngineTest")
    scalaProject
      .addSourcePackageFolder("package1")
      .addSourcePackageFolder("package1/package2")
      .addSourceFile("SearchEngineTest", "package1/package2/ScalaInSubPackage.scala")
      .addSourceFile("SearchEngineTest", "package1/package2/JavaInSubPackage.java")
      .waitUntilAutomaticBuildCompletes
  }

  @Test
  def testExactMatchOfJavaClassInSubPackage = {
    searchForMatch("package1.package2.JavaInSubPackage")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  @Test
  def testExactMatchOfScalaClassInSubPackage = {
    searchForMatch("package1.package2.ScalaInSubPackage")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  @Test
  def testExactMatchOfScalaObjectInSubPackage = {
    searchForMatch("package1.package2.ScalaInSubPackage$")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  @Test
  def testExactMatchOfScalaAnonymousFunctionInSubPackage = {
    searchForMatch("package1.package2.ScalaInSubPackage$$anonfun$1")
    assertThat(matchedElements.size, equalTo(1))
  }
  
  @After
  override def tearDown : Unit = {
    super.tearDown
  }
}