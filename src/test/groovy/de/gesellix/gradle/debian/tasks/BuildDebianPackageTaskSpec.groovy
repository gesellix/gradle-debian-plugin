package de.gesellix.gradle.debian.tasks

import de.gesellix.gradle.debian.tasks.data.Data
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.vafer.jdeb.PackagingException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class BuildDebianPackageTaskSpec extends Specification {

  @Shared
  def task

  def setup() {
    Project project = ProjectBuilder.builder().build()
    task = project.task('buildDeb', type: BuildDebianPackageTask)
    assert task instanceof BuildDebianPackageTask
  }

  def "description and group are set"() {
    expect:
    task.description == "Build debian package"
    task.group == "Build"
  }

  @Unroll("inadequately configured task with should throw #expectedException and message #exceptionMessagePattern")
  def "buildPackage with invalid configuration"(taskConfig, expectedException, exceptionMessagePattern) {
    when: "packagename is configured"
    taskConfig(task)
    task.buildPackage();
    then:
    def e = thrown(expectedException)
    e.message =~ exceptionMessagePattern
    where:
    taskConfig || expectedException | exceptionMessagePattern
        { task ->
          /* do nothing */
        } || AssertionError | /(?m)assert getPackagename.*/
        { task ->
          task.packagename = "packagename"
        } || AssertionError | /(?m)assert getChangelogFile.*/
        { task ->
          task.packagename = "packagename"
          task.changelogFile = File.createTempFile("tst", "tmp")
        } || AssertionError | /(?m)assert getControlDirectory.*/
        { task ->
          task.packagename = "packagename"
          task.changelogFile = File.createTempFile("tst", "tmp")
          task.controlDirectory = File.createTempFile("tst2", "tmp")
          task.outputFile = File.createTempFile("tst3", "tmp")
          task.data = new Data()
        } || PackagingException | /Could not create deb package/
  }
}
