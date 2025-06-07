package de.gesellix.gradle.debian

import de.gesellix.gradle.debian.tasks.BuildDebianPackageTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Shared
import spock.lang.Specification

import static de.gesellix.gradle.debian.tasks.BuildDebianPackageTask.DEBPKGTASK_NAME
import static org.gradle.api.publish.maven.plugins.MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME

class DebianPackagePluginSpec extends Specification {

  @Shared
  Project project
  @Shared
  File projectDir

  def setup() {
    URL resource = getClass().getResource('/gradle/build.gradle')
    projectDir = new File(resource.toURI()).getParentFile()
    project = ProjectBuilder.builder()
        .withName('test-project')
        .withProjectDir(projectDir)
        .build()
  }

  def "no DebianPackagePluginExtension is registered by default"() {
    expect:
    !project.extensions.findByType(DebianPackagePluginExtension)
  }

  def "DebianPackagePluginExtension is registered"() {
    when: "plugin applied to project"
    def result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("for-test")
        .build()
    then:
    result.output.contains("Extension packagename: packagename.")
  }

  def "no BuildDebianPackage tasks are registered by default"() {
    expect:
    !project.tasks.withType(BuildDebianPackageTask)
  }

  def "BuildDebianPackage task is registered as 'buildDeb'"() {
    when: "plugin applied to project"
    assert DEBPKGTASK_NAME == 'buildDeb'
    def result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments(DEBPKGTASK_NAME, "--no-build-cache")
        .build()
    then: "there is a BuildDebianPackage task registered"
    result.output.contains("found buildDeb task: buildDeb")
    and:
    result.output.contains("packagename: packagename")
    result.output.contains("publications: [mavenStuff]")
    result.output.contains("changelogFile: ${new File("${projectDir}/../packagename/debian/changelog").canonicalFile}")
    result.output.contains("controlDirectory: ${new File("${projectDir}/../packagename/control").canonicalFile}")
    result.output.contains("data in de.gesellix.gradle.debian.tasks.data.Data: true")
    result.output.contains("outputFile: ${new File("${projectDir}/build/packagename-42.deb").canonicalFile}")
    result.output.contains("dependsOn: ${[PUBLISH_LOCAL_LIFECYCLE_TASK_NAME, ASSEMBLE_TASK_NAME].sort()}")
//    result.task(DEBPKGTASK_NAME).outcome == TaskOutcome.SUCCESS
  }
}
