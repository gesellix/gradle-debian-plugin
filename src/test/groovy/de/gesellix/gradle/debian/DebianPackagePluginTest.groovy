package de.gesellix.gradle.debian

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test

class DebianPackagePluginTest {

  @Test
  public void "plugin adds buildDeb task to project"() {
    Project project = ProjectBuilder.builder().build()
    project.apply plugin: 'pkg-debian'

    assert project.tasks.findByName('buildDeb')
  }

  @Test
  public void "buildDeb task is a BuildDebianPackageTask"() {
    Project project = ProjectBuilder.builder().build()
    project.apply plugin: 'pkg-debian'

    def buildDebTask = project.tasks.findByName('buildDeb')
    assert buildDebTask in BuildDebianPackageTask
  }
}
