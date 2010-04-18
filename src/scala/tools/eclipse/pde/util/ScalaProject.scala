package scala.tools.eclipse.pde.util

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
  
  private var project: Option[IProject] = None
  private var scalaProject: Option[IJavaProject] = None
  
  private var outputLocation: Option[IPath] = None
  private var srcFolder: Option[IFolder] = None
  
  private var srcEntry: Option[IClasspathEntry] = None
  private var jreEntry: Option[IClasspathEntry] = None

  def named(name: String) = {
    val project = createWorkspaceProject(name)
    addScalaNatureToWorkspaceProject(project)
    val eclipseScalaProject = createAndConfigureScalaProject(project)
    
    val scalaProject = new ScalaProject(eclipseScalaProject)
    scalaProject.waitUntilAutomaticBuildCompletes
    
    scalaProject
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
    
    val outputLocation = initializeOutputPath(project)
    val sourceFolder = initializeSourceFolder(project)
    
    val sourceEntry = getClasspathEntryOfSourceFolder(sourceFolder)
    val jreEntry = retrieveJreClasspathEntry
    
    setClassPath(scalaProject, jreEntry, sourceEntry)
    
    scalaProject
  }

  private def initializeOutputPath(project: IProject) = {
    val folder = project.getFolder(OUTPUT_PATH)
    folder.create(IResource.FORCE, true, NO_MONITOR)
    val outputLocation = Some(folder.getFullPath)
    if (outputLocation == null) throw new IllegalStateException("Unable to initialize output path")
    
    outputLocation
  }

  private def initializeSourceFolder(project: IProject) = {
    val sourceFolder = project.getFolder(SOURCE_PATH)
    if (sourceFolder == null) throw new IllegalStateException("Unable to initialize source folder")
    sourceFolder.create(IResource.FORCE, true, NO_MONITOR)
    
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

  private def setClassPath(scalaProject: IJavaProject, 
      jreEntry: IClasspathEntry, sourceEntry: IClasspathEntry) = {
        
    scalaProject.setRawClasspath(Array[IClasspathEntry](jreEntry, sourceEntry), 
        outputLocation.get, NO_MONITOR)
  }
}

class ScalaProject(scalaProject: IJavaProject) {
  import ScalaProject._

  val project = scalaProject.getProject
  val workspace = project.getWorkspace
  val sourceFolder = project.getFolder(SOURCE_PATH)

  def waitUntilAutomaticBuildCompletes =
    Job.getJobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, NO_MONITOR)

  def addSampleSourceFile = {
    val srcFilePath = new Path(sourceFolder.getFullPath + "/Sample.scala")
    val srcFile = workspace.getRoot.getFile(srcFilePath)
    val url = FileLocator find (Platform getBundle ScalaPlugin.plugin.pluginId, 
        new Path("resources/Sample.scala"), Collections.EMPTY_MAP)
    srcFile create (url.openStream, IResource.FORCE, NO_MONITOR)
    JavaCore create srcFile
  }

  def defaultClasspath = JavaRuntime.newDefaultProjectClasspathEntry(scalaProject)
  def remove = project.delete(IResource.FORCE, NO_MONITOR)
}
