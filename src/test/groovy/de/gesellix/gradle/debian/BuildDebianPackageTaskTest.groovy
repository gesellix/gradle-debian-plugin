package de.gesellix.gradle.debian

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test
import org.vafer.jdeb.mapping.Mapper
import org.vafer.jdeb.producers.DataProducerFile

class BuildDebianPackageTaskTest {

  @Test
  public void "can add task to project"() {
    Project project = ProjectBuilder.builder().build()
    def task = project.task('buildDeb', type: BuildDebianPackageTask)
    assert task instanceof BuildDebianPackageTask
  }

  @Test(dependsOnMethods = ["can add task to project"])
  public void "can create output file"() {
    def outputFile = new File("./build/output.deb")
    if (outputFile.exists()) {
      outputFile.delete()
    }
    assert !outputFile.exists()

    Project project = ProjectBuilder.builder().build()
    def task = project.task('buildDeb', type: BuildDebianPackageTask)
    task.controlFiles = new File("./src/test/resources/debian/control").listFiles()
    task.dataProducers = [
        new DataProducerFile(new File("./src/test/resources/debian/input.txt"), "/tmp", [] as String[], [] as String[], [] as Mapper[])
    ]
    task.outputFile = outputFile
    task.buildPackage()

    assert outputFile.exists()
  }
}
