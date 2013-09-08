package de.gesellix.gradle.debian

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test

class BuildDebianPackageTaskTest {

  @Test
  public void "can add task to project"() {
    Project project = ProjectBuilder.builder().build()
    def task = project.task('buildDeb', type: BuildDebianPackageTask)
    assert task instanceof BuildDebianPackageTask
  }
}
