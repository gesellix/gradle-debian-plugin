package de.gesellix.gradle.debian.tasks

import de.gesellix.gradle.debian.tasks.data.Data
import de.gesellix.gradle.debian.tasks.jdeb.DataProducerCreator
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.testfixtures.ProjectBuilder
import org.vafer.jdeb.PackagingException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class BuildDebianPackageTaskSpec extends Specification {

  @Shared
  Task task
  @Shared
  File testResourcesDir
  @Shared
  Project project
  @Shared
  DataProducerCreator dataProducerCreatorMock

  def setup() {
    project = ProjectBuilder.builder().build()

    task = project.task('buildDeb', type: BuildDebianPackageTask)
    assert task instanceof BuildDebianPackageTask

    URL resource = getClass().getResource('/gradle/build.gradle')
    testResourcesDir = new File(resource.toURI()).parentFile.parentFile

    dataProducerCreatorMock = Mock(DataProducerCreator)
  }

  def "description and group are set"() {
    expect:
    task.description == "Build debian package"
    task.group == "Build"
  }

  @Unroll("inadequately configured task should throw #expectedException with message like '#exceptionMessagePattern'")
  def "buildPackage with invalid configuration"(taskConfig, expectedException, exceptionMessagePattern) {
    when: "task is configured"
    taskConfig(task)
    task.buildPackage();
    then:
    def e = thrown(expectedException)
    e.message =~ exceptionMessagePattern
    where:
    taskConfig || expectedException | exceptionMessagePattern;
    { task ->
      /* do nothing */
    } || AssertionError | /(?m)assert getPackagename.*/;
    { task ->
      task.packagename = "packagename"
    } || AssertionError | /(?m)assert getChangelogFile.*/;
    { task ->
      task.packagename = "packagename"
      task.changelogFile = File.createTempFile("tst", "tmp")
    } || AssertionError | /(?m)assert getControlDirectory.*/;
    { task ->
      task.packagename = "packagename"
      task.changelogFile = File.createTempFile("tst", "tmp")
      task.controlDirectory = File.createTempFile("tst2", "tmp")
    } || AssertionError | /(?m)assert getOutputFile.*/;
    { task ->
      task.packagename = "packagename"
      task.changelogFile = File.createTempFile("tst", "tmp")
      task.controlDirectory = File.createTempFile("tst2", "tmp")
      task.outputFile = File.createTempFile("tst3", "tmp")
    } || AssertionError | /(?m)assert getData.*/;
    { task ->
      task.packagename = "packagename"
      task.changelogFile = File.createTempFile("tst", "tmp")
      task.controlDirectory = File.createTempFile("tst2", "tmp")
      task.outputFile = File.createTempFile("tst3", "tmp")
      task.data = new Data()
    } || PackagingException | /Failed to create debian package/;
  }

  @Unroll("correctly configured task with publications should add data files with names '#dataFileNames' and targets '#dataFileTargets'")
  def "buildPackage adds PublicationArtifacts"(taskConfig, dataFileNames, dataFileTargets) {
    given:
    dataProducerCreatorMock.createDataProducers(_ as Data, project) >> []
    dataProducerCreatorMock.createConffileProducers(_ as Data, project) >> []

    when:
    task.with {
      packagename = "packagename"
      changelogFile = new File(testResourcesDir, "packagename/debian/changelog").canonicalFile
      controlDirectory = new File(testResourcesDir, "packagename/control").canonicalFile
      outputFile = File.createTempFile("tst3", "tmp")
      data = new Data()
      dataProducerCreator = dataProducerCreatorMock
    }
    taskConfig(task)
    task.buildPackage()

    then:
    task.data.files.name == dataFileNames
    task.data.files.target == dataFileTargets

    where:
    taskConfig || dataFileNames | dataFileTargets;
    { task ->
      task.publications = ['mavenStuff']
    } || [] | [];
    { task ->
      task.publications = []
      project.apply plugin: 'maven-publish'
      project.with {
        publishing {
          publications {
            mavenStuff(MavenPublication) {
              artifact new File(testResourcesDir, "inputfiles/artifact.war")
            }
          }
        }
      }
    } || [] | [];
    { task ->
      task.publications = ['mavenStuff']
      project.apply plugin: 'maven-publish'
      project.with {
        publishing {
          publications {
            mavenStuff(MavenPublication) {
              artifact new File(testResourcesDir, "inputfiles/artifact.war")
            }
          }
        }
      }
    } || [new File(testResourcesDir, "inputfiles/artifact.war").canonicalPath] | ["usr/share/packagename/publications"];
  }

  def "creates Jdeb DebMaker with packagename and project version variable resolver"() {
    def debMaker
    when:
    task.with {
      packagename = "anotherpackagename"
    }
    project.with {
      version = "42"
    }
    debMaker = task.createDebMaker([], [])
    then:
    debMaker.variableResolver.get("name") == "anotherpackagename"
    debMaker.variableResolver.get("version") == "42"
  }

  def "creates Jdeb DebMaker with controlDirectory"() {
    def debMaker
    def expectedControlDirectory
    when:
    expectedControlDirectory = new File("./control")
    task.with {
      controlDirectory = expectedControlDirectory
    }
    debMaker = task.createDebMaker([], [])
    then:
    debMaker.control == expectedControlDirectory
  }

  def "creates Jdeb DebMaker with outputFile"() {
    def debMaker
    def expectedOutputFile
    when:
    expectedOutputFile = new File("./out.deb")
    task.with {
      outputFile = expectedOutputFile
    }
    debMaker = task.createDebMaker([], [])
    then:
    debMaker.deb == expectedOutputFile
  }

  def "creates Jdeb DebMaker with changelogFile"() {
    def debMaker
    def expectedChangelogFile
    when:
    expectedChangelogFile = new File("./changes")
    task.with {
      changelogFile = expectedChangelogFile
    }
    debMaker = task.createDebMaker([], [])
    then:
    debMaker.changesIn == expectedChangelogFile
  }
}
