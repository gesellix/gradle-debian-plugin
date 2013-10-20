package de.gesellix.gradle.debian.tasks

import de.gesellix.gradle.debian.MavenPublicationsByProject
import de.gesellix.gradle.debian.PublicationFinder
import de.gesellix.gradle.debian.tasks.data.Data
import de.gesellix.gradle.debian.tasks.jdeb.DataProducerChangelog
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.vafer.jdeb.Console
import org.vafer.jdeb.DataProducer
import org.vafer.jdeb.Processor
import org.vafer.jdeb.mapping.Mapper
import org.vafer.jdeb.mapping.PermMapper
import org.vafer.jdeb.producers.DataProducerDirectory
import org.vafer.jdeb.producers.DataProducerFile
import org.vafer.jdeb.producers.DataProducerLink
import org.vafer.jdeb.utils.MapVariableResolver

import static org.vafer.jdeb.Compression.GZIP

class BuildDebianPackageTask extends DefaultTask {

  public static final String DEBPKGTASK_NAME = 'buildDeb'

  private PublicationFinder publicationFinder = new PublicationFinder()
  private ArtifactCollector artifactCollector = new ArtifactCollector()

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
  }

  @TaskAction
  def buildPackage() {
    assert getPackagename()
    assert getChangelogFile()?.exists()
    assert getControlDirectory()?.exists()
    assert getOutputFile()
    assert getData()

    addPublicationArtifacts(getPublications(), getData(), getPackagename())

    def console = [
        info: { msg -> logger.info(msg) },
        warn: { msg -> logger.warn(msg) }] as Console
    def resolver = new MapVariableResolver([
        name: getPackagename(),
        version: project.version])

    def dataProducers = createDataProducers(getData())
    dataProducers = dataProducers.toList() + new DataProducerChangelog(getChangelogFile(), "/usr/share/doc/${getPackagename()}/changelog.gz", [] as String[], [] as String[], [] as Mapper[])

    def processor = new Processor(console, resolver)
    def packageDescriptor = processor.createDeb(getControlDirectory().listFiles(), dataProducers as DataProducer[], getOutputFile(), GZIP)
  }

  def createDataProducers(Data data) {
    def result = [] as DataProducer[]
    data.directories.each { directory ->
      assert project.file(directory.name).exists()
      result = result.toList() + new DataProducerDirectory(project.file(directory.name), directory.inclusions, directory.exclusions, [] as Mapper[])
    }
    data.files.each { file ->
      def mapper = new PermMapper(-1, -1, null, null, file.mapper.fileMode, null, -1, null)
      assert project.file(file.name).exists()
      result = result.toList() + new DataProducerFile(project.file(file.name), file.target, null, null, mapper)
    }
    data.links.each { link ->
      result = result.toList() + new DataProducerLink(link.path, link.name, link.symbolic, null, null, null)
    }

    return result
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
