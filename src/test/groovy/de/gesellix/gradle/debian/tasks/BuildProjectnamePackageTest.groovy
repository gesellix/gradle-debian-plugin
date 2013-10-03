package de.gesellix.gradle.debian.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test
import org.vafer.jdeb.mapping.Mapper
import org.vafer.jdeb.mapping.PermMapper
import org.vafer.jdeb.producers.DataProducerDirectory
import org.vafer.jdeb.producers.DataProducerFile

class BuildProjectnamePackageTest {

  @Test
  public void "can create output file"() {
    def outputFile = new File("./build/packagename.deb")
    if (outputFile.exists()) {
      outputFile.delete()
    }
    assert !outputFile.exists()

    Project project = ProjectBuilder.builder()
        .withName("packagename")
        .build()
    project.version = 42
    def task = project.task('buildDeb', type: BuildDebianPackageTask)
    task.controlDirectory = new File("./src/test/resources/packagename/control")
    task.copyrightFile = new File("./src/test/resources/packagename/data/usr/share/doc/packagename/copyright")
    task.changelogFile = new File("./src/test/resources/packagename/debian/changelog")
    def executable = [].toList() << new PermMapper(-1, -1, null, null, "755", null, -1, null) as Mapper[]
    def exclusions = [].toList()
    exclusions += "etc/init.d/packagename"
    exclusions += "./src/test/resources/packagename/data/usr/share/doc/packagename/copyright"
    task.dataProducers = [
        new DataProducerDirectory(new File("./src/test/resources/packagename/data"), null, exclusions as String[], null),
        new DataProducerFile(new File("./src/test/resources/packagename/data/etc/init.d/packagename"), "etc/init.d/packagename", null, null, executable),
        new DataProducerFile(new File("./src/test/resources/packagename/data/etc/cron.daily/packagename"), "etc/cron.daily/packagename", null, null, executable),
        new DataProducerFile(new File("./src/test/resources/packagename/data/usr/share/packagename/bin/setenv.sh"), "usr/share/packagename/bin/setenv.sh", null, null, executable),
        new DataProducerFile(new File("./src/test/resources/packagename/data/usr/share/packagename/bin/shutdown.sh"), "usr/share/packagename/bin/shutdown.sh", null, null, executable),
        new DataProducerFile(new File("./src/test/resources/packagename/data/usr/share/packagename/bin/startup.sh"), "usr/share/packagename/bin/startup.sh", null, null, executable),
    ]
    task.outputFile = outputFile
    task.packagename = "packagename"

    task.buildPackage()

    assert outputFile.exists()
  }
}
