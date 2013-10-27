package de.gesellix.gradle.debian.tasks

import de.gesellix.gradle.debian.tasks.data.Data
import de.gesellix.gradle.debian.tasks.jdeb.DataProducerCreator
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.testfixtures.ProjectBuilder
import org.vafer.jdeb.PackagingException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class BuildDebianPackageTaskSpec extends Specification {

  @Shared
  def task
  @Shared
  def projectDir
  @Shared
  def project
  @Shared
  def dataProducerCreatorMock

  def setup() {
    project = ProjectBuilder.builder().build()

    task = project.task('buildDeb', type: BuildDebianPackageTask)
    assert task instanceof BuildDebianPackageTask

    URL resource = getClass().getResource('/gradle/build.gradle')
    projectDir = new File(resource.toURI()).getParentFile()

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
        } || AssertionError | /(?m)assert getOutputFile.*/
        { task ->
          task.packagename = "packagename"
          task.changelogFile = File.createTempFile("tst", "tmp")
          task.controlDirectory = File.createTempFile("tst2", "tmp")
          task.outputFile = File.createTempFile("tst3", "tmp")
        } || AssertionError | /(?m)assert getData.*/
        { task ->
          task.packagename = "packagename"
          task.changelogFile = File.createTempFile("tst", "tmp")
          task.controlDirectory = File.createTempFile("tst2", "tmp")
          task.outputFile = File.createTempFile("tst3", "tmp")
          task.data = new Data()
        } || PackagingException | /Could not create deb package/
  }

  def "buildPackage adds PublicationArtifacts"(taskConfig, expectedPublicationArtifact) {
    given:
    dataProducerCreatorMock.createDataProducers(_, project) >> []
    when:
    taskConfig(task)
    task.buildPackage()
    then:
    task.data.files.name == ["/tmp/inputfiles/artifact.war"]
    task.data.files.target == ["usr/share/packagename/publications"]
    where:
    taskConfig || expectedPublicationArtifact
        { task ->
          task.packagename = "packagename"
          task.changelogFile = new File("${projectDir}/../packagename/debian/changelog").canonicalFile
          task.controlDirectory = new File("${projectDir}/../packagename/control").canonicalFile
          task.publications = ['mavenStuff']
          task.outputFile = File.createTempFile("tst3", "tmp")
          task.data = new Data()

          task.dataProducerCreator = dataProducerCreatorMock

          project.apply plugin: 'maven-publish'
          project.with {
            publishing {
              publications {
                mavenStuff(MavenPublication) {
                  artifact new File("${projectDir}/../inputfiles/artifact.war")
                }
              }
            }
          }
        } || ""
  }
}
