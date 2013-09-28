package de.gesellix.gradle.debian

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DebianPackagePluginSpec extends Specification {

  Project project

  def setup() {
    URL resource = getClass().getResource('/gradle/build.gradle')
    def projDir = new File(resource.toURI()).getParentFile()

    project = ProjectBuilder.builder().withName('project').withProjectDir(projDir).build()
  }

  def "no BuildDebianPackage tasks are registered by default"() {
    when: "plugin applied to project"
    project.evaluate()
    then: "there is a BuildDebianPackage tasks registered"
    project.tasks.withType(BuildDebianPackageTask)
  }
}
