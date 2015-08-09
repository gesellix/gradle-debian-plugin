package de.gesellix.gradle.debian

import de.gesellix.gradle.debian.tasks.BuildDebianPackageTask
import de.gesellix.gradle.debian.tasks.data.Data
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

import static de.gesellix.gradle.debian.DebianPackagePluginExtension.DEBPKGPLUGIN_EXTENSION_NAME
import static de.gesellix.gradle.debian.tasks.BuildDebianPackageTask.DEBPKGTASK_NAME
import static org.gradle.api.publish.maven.plugins.MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME
import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_TASK_NAME

class DebianPackagePluginSpec extends Specification {

  @Shared
  Project project
  @Shared
  def projectDir

  def setup() {
    URL resource = getClass().getResource('/gradle/build.gradle')
    projectDir = new File(resource.toURI()).getParentFile()
    project = ProjectBuilder.builder().withName('project').withProjectDir(projectDir).build()
  }

  def "no DebianPackagePluginExtension is registered by default"() {
    expect:
    !project.extensions.findByType(DebianPackagePluginExtension)
  }

  def "DebianPackagePluginExtension is registered on project evaluation"() {
    when: "plugin applied to project"
    project.evaluate()
    then:
    project.extensions.findByType(DebianPackagePluginExtension)
  }

  def "DebianPackagePluginExtension is registered as 'debian'"() {
    when: "plugin applied to project"
    project.evaluate()
    assert DEBPKGPLUGIN_EXTENSION_NAME == 'debian'
    then:
    project.extensions.findByName('debian') in DebianPackagePluginExtension
  }

  def "no BuildDebianPackage tasks are registered by default"() {
    expect:
    !project.tasks.withType(BuildDebianPackageTask)
  }

  def "BuildDebianPackage tasks are registered on project evaluation"() {
    when: "plugin applied to project"
    project.evaluate()
    then: "there is a BuildDebianPackage task registered"
    project.tasks.withType(BuildDebianPackageTask)
  }

  def "BuildDebianPackage task is registered as 'buildDeb'"() {
    when: "plugin applied to project"
    project.evaluate()
    assert DEBPKGTASK_NAME == 'buildDeb'
    then: "there is a 'buildDeb' task registered"
    project.tasks.findByName('buildDeb') in BuildDebianPackageTask
  }

  def "can handle a debian configuration"() {
    when: "project example project 'projectname' is evaluated"
    Project project = ProjectBuilder.builder().withName('projectname').withProjectDir(projectDir).build()
    project.evaluate()

    then: "extension properties are mapped to task properties"
    Task buildDebTask = project.tasks.findByName(DEBPKGTASK_NAME)
    buildDebTask != null
    buildDebTask.packagename == "packagename"
    buildDebTask.changelogFile == new File("${projectDir}/../packagename/debian/changelog").canonicalFile
    buildDebTask.controlDirectory == new File("${projectDir}/../packagename/control").canonicalFile
    buildDebTask.publications == ['mavenStuff']
    buildDebTask.data in Data
    buildDebTask.outputFile == new File("${projectDir}/build/packagename-${project.version}.deb").canonicalFile
  }

  def "buildDeb is dependent on publicationTask"() {
    when: "project example project 'projectname' is evaluated"
    Project project = ProjectBuilder.builder().withName('projectname').withProjectDir(projectDir).build()
    project.evaluate()
    then:
    Task buildDebTask = project.tasks.findByName(DEBPKGTASK_NAME)
    Task publicationTask = project.tasks.findByName(PUBLISH_LOCAL_LIFECYCLE_TASK_NAME)
    buildDebTask.taskDependencies.getDependencies(buildDebTask).contains(publicationTask)
  }

  def "buildDeb is dependent on build task"() {
    when: "project example project 'projectname' is evaluated"
    Project project = ProjectBuilder.builder().withName('projectname').withProjectDir(projectDir).build()
    project.evaluate()
    then:
    Task buildDebTask = project.tasks.findByName(DEBPKGTASK_NAME)
    buildDebTask.taskDependencies.getDependencies(buildDebTask).contains(project.tasks.findByName(BUILD_TASK_NAME))
  }
}
