package de.gesellix.gradle.debian.tasks

import de.gesellix.gradle.debian.MavenPublicationsByProject
import de.gesellix.gradle.debian.PublicationFinder
import de.gesellix.gradle.debian.tasks.data.Data
import de.gesellix.gradle.debian.tasks.jdeb.DataProducerChangelog
import de.gesellix.gradle.debian.tasks.jdeb.DataProducerCreator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.vafer.jdeb.Console
import org.vafer.jdeb.DataProducer
import org.vafer.jdeb.Processor
import org.vafer.jdeb.mapping.Mapper
import org.vafer.jdeb.utils.MapVariableResolver

import static org.vafer.jdeb.Compression.GZIP

class BuildDebianPackageTask extends DefaultTask {

  public static final String DEBPKGTASK_NAME = 'buildDeb'

  def publicationFinder
  def artifactCollector
  def dataProducerCreator

  @Input
  String packagename
  @InputFile
  File changelogFile
  @InputDirectory
  File controlDirectory
  @Nested
  Data data
  @Input
  @Optional
  String[] publications
  @OutputFile
  File outputFile

  BuildDebianPackageTask() {
    description = "Build debian package"
    group = "Build"

    publicationFinder = new PublicationFinder()
    artifactCollector = new ArtifactCollector(path, logger)
    dataProducerCreator = new DataProducerCreator()
  }

  @TaskAction
  def buildPackage() {
    assert getPackagename()
    assert getChangelogFile()?.exists()
    assert getControlDirectory()?.exists()
    assert getOutputFile()
    assert getData()

    addPublicationArtifacts(getPublications(), getData(), getPackagename())

    def dataProducers = dataProducerCreator.createDataProducers(getData(), project)
    dataProducers = dataProducers.toList() + new DataProducerChangelog(getChangelogFile(), "/usr/share/doc/${getPackagename()}/changelog.gz", [] as String[], [] as String[], [] as Mapper[])

    def processor = createProcessor()
    def packageDescriptor = processor.createDeb(getControlDirectory().listFiles(), dataProducers as DataProducer[], getOutputFile(), GZIP)
  }

  def createProcessor() {
    def console = [
        info: { msg -> logger.info(msg) },
        warn: { msg -> logger.warn(msg) }] as Console
    def resolver = new MapVariableResolver([
        name: getPackagename(),
        version: project.version])

    return new Processor(console, resolver)
  }

  def addPublicationArtifacts(String[] publicationNames, Data data, String packagename) {
    if (publicationNames?.length) {
      def publicationsByProject = publicationFinder.findPublicationsInProject(project, publicationNames as String[])
      publicationsByProject.each { MavenPublicationsByProject mavenPublicationByProject ->
        mavenPublicationByProject.publications.each { publication ->
          def artifacts = artifactCollector.collectArtifacts(publication)
          artifacts.each { artifact ->
            data.with {
              project.logger.info "adding artifact ${artifact.file}"
              file {
                name = artifact.file
                target = "usr/share/${packagename}/publications"
              }
            }
          }
        }
      }
    }
  }
}
