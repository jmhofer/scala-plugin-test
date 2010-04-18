package scala.tools.eclipse.pde.util

import java.io.File
import java.net.URL
import java.util.Collections

import org.eclipse.core.resources._
import org.eclipse.core.runtime._
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core._
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

import scala.tools.eclipse.ScalaPlugin

/**
 * Builder utility for creating Scala projects in an Eclipse workspace.
 */
object ScalaProject {
  val NO_MONITOR = null
  val SOURCE_PATH = "src"
  val OUTPUT_PATH = "bin"
    
  def createIn(workspace: IWorkspace) = new ScalaProjectBuilder(workspace)
}

class ScalaProjectBuilder(workspace: IWorkspace) {
  import ScalaProject._
  
  def named(name: String) = {
    val project = createWorkspaceProject(name)
    addScalaNatureToWorkspaceProject(project)
    val eclipseScalaProject = createAndConfigureScalaProject(project)
    
    new ScalaProject(eclipseScalaProject)
  }

  private def createWorkspaceProject(name: String) = {
    val project = workspace.getRoot.getProject(name)
    if (project == null) throw new IllegalStateException("Unable to create workspace project")
    
    project.create(NO_MONITOR)
    project.open(NO_MONITOR)
    
    project
  }

  private def addScalaNatureToWorkspaceProject(project: IProject) = {
    val description = project.getDescription
    description.setNatureIds(Array(JavaCore.NATURE_ID, ScalaPlugin.plugin.natureId))
    project.setDescription(description, NO_MONITOR)
  }

  private def createAndConfigureScalaProject(project: IProject) = {
    val scalaProject = JavaCore.create(project)
    if (scalaProject == null) throw new IllegalStateException("Unable to create Scala project")
    
    initializeOutputPath(project)
    val sourceFolder = initializeSourceFolder(project)
    
    val sourceEntry = getClasspathEntryOfSourceFolder(sourceFolder)
    val jreEntry = retrieveJreClasspathEntry
    val scalaEntry = retrieveScalaClasspathEntry
    
    setClassPath(scalaProject, jreEntry, scalaEntry, sourceEntry)
    
    scalaProject
  }

  private def initializeOutputPath(project: IProject) = {
    val folder = project.getFolder(OUTPUT_PATH)
    if (folder == null) throw new IllegalStateException("Unable to initialize output path")
    
    if (! folder.exists()) folder.create(IResource.FORCE, true, NO_MONITOR)
  }

  private def initializeSourceFolder(project: IProject) = {
    val sourceFolder = project.getFolder(SOURCE_PATH)
    if (sourceFolder == null) throw new IllegalStateException("Unable to initialize source folder")
    
    if (! sourceFolder.exists()) sourceFolder.create(IResource.FORCE, true, NO_MONITOR)
    
    sourceFolder
  }

  private def getClasspathEntryOfSourceFolder(sourceFolder: IFolder) = {
    val sourceEntry = JavaCore.newSourceEntry(sourceFolder.getFullPath)
    if (sourceEntry == null) throw new IllegalStateException(
        "Unable to create classpath entry of source folder")
    
    sourceEntry
  }
  
  private def retrieveJreClasspathEntry = {
    val jreEntry = JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER))
    if (jreEntry == null) throw new IllegalStateException("Unable to retrieve JRE classpath entry")
    
    jreEntry
  }

  private def retrieveScalaClasspathEntry = {
    val scalaEntry = JavaCore.newContainerEntry(new Path(ScalaPlugin.plugin.scalaLibId))
    if (scalaEntry == null) 
      throw new IllegalStateException("Unable to retrieve Scala classpath entry")
    
    scalaEntry
  }
  
  private def setClassPath(scalaProject: IJavaProject, 
      jreEntry: IClasspathEntry, scalaEntry: IClasspathEntry, sourceEntry: IClasspathEntry) = {
        
    scalaProject.setRawClasspath(Array[IClasspathEntry](jreEntry, scalaEntry, sourceEntry), 
        scalaProject.getOutputLocation, NO_MONITOR)
  }
}

class ScalaProject(scalaProject: IJavaProject) {
  import ScalaProject._

  val project = scalaProject.getProject
  val workspace = project.getWorkspace
  val sourceFolder = project.getFolder(SOURCE_PATH)

  def waitUntilAutomaticBuildCompletes = {
    try {
      Job.getJobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, NO_MONITOR)
    } catch {
      case _: InterruptedException => Thread.currentThread.interrupt
    }
    
    this
  }

  def addSourcePackageFolder(relativeFilePath: String) = {
    val packageFolder = project.getFolder(
        "%s/%s".format(sourceFolder.getFullPath, relativeFilePath))
    packageFolder.create(IResource.FORCE, true, NO_MONITOR)
    
    this
  }
  
  def addSourceFile(testName: String, relativeFilePath: String) = {
    val sourceFilePath = new Path("%s/%s".format(sourceFolder.getFullPath, relativeFilePath))
    val sourceFile = workspace.getRoot.getFile(sourceFilePath)
    val url = FileLocator.find(Platform.getBundle(ScalaPlugin.plugin.pluginId), 
        new Path("resources/%s/%s".format(testName, relativeFilePath)), Collections.emptyMap())
    sourceFile.create(url.openStream, IResource.FORCE, NO_MONITOR)
    JavaCore.create(sourceFile)
    
    this
  }    

  def defaultClasspath = JavaRuntime.newDefaultProjectClasspathEntry(scalaProject)
  def remove = project.delete(IResource.FORCE, NO_MONITOR)
}
