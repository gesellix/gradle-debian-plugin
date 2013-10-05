package de.gesellix.gradle.debian

import de.gesellix.gradle.debian.tasks.BuildDebianPackageTask
import de.gesellix.gradle.debian.tasks.data.Data
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test

import static de.gesellix.gradle.debian.DebianPackagePluginExtension.DEBPKGPLUGIN_EXTENSION_NAME
import static de.gesellix.gradle.debian.tasks.BuildDebianPackageTask.DEBPKGTASK_NAME

class DebianPackagePluginTest {

  @Test
  public void "plugin adds buildDeb task to project"() {
    Project project = ProjectBuilder.builder().build()
    project.apply plugin: 'pkg-debian'
    project.evaluate()

    assert project.tasks.findByName(DEBPKGTASK_NAME)
  }

  @Test
  public void "buildDeb task is a BuildDebianPackageTask"() {
    Project project = ProjectBuilder.builder().build()
    project.apply plugin: 'pkg-debian'
    project.evaluate()

    def buildDebTask = project.tasks.findByName(DEBPKGTASK_NAME)
    assert buildDebTask in BuildDebianPackageTask
  }

  @Test
  public void "can handle a debPkgPlugin configuration"() {
    def projectDir = new File(getClass().getResource('/gradle/build.gradle').toURI()).getParentFile()

    Project project = ProjectBuilder.builder()
        .withName('projectname')
        .withProjectDir(projectDir).build()
    project.evaluate()

    assert project.extensions.findByName(DEBPKGPLUGIN_EXTENSION_NAME) != null

    Task buildDebTask = project.tasks.findByName(DEBPKGTASK_NAME)
    assert buildDebTask != null
    assert buildDebTask.description == 'Build debian package'
    assert buildDebTask.group == 'Build'
    assert buildDebTask.packagename == "packagename"
    assert buildDebTask.changelogFile == new File("${projectDir}/../packagename/debian/changelog").canonicalFile
    assert buildDebTask.controlDirectory == new File("${projectDir}/../packagename/control").canonicalFile
    assert buildDebTask.data in Data
    assert buildDebTask.outputFile == new File("${projectDir}/build/packagename-42.deb").canonicalFile

    Task publicationTask = project.tasks.findByName("generatePomFileForMavenStuffPublication")
    assert buildDebTask.taskDependencies.getDependencies(buildDebTask).contains(publicationTask)
    assert buildDebTask.taskDependencies.getDependencies(buildDebTask).contains(project.tasks.findByName("build"))
  }
}
