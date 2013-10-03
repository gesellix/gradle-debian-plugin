package de.gesellix.gradle.debian.tasks

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
import org.vafer.jdeb.utils.MapVariableResolver

import static org.vafer.jdeb.Compression.GZIP

class BuildDebianPackageTask extends DefaultTask {

  public static final String DEBPKGTASK_NAME = 'buildDeb'

  @Input
  String packagename
  @InputFile
  File copyrightFile
  @InputFile
  File changelogFile
  @InputDirectory
  File controlDirectory
  @Input
  @Optional
  String[] publications
  @OutputFile
  File outputFile
  @Input
  Data data

  BuildDebianPackageTask() {
    description = "Build debian package"
    group = "Build"
  }

  @TaskAction
  def buildPackage() {
    assert getPackagename()
    assert getCopyrightFile()?.exists()
    assert getCopyrightFile()?.exists()
    assert getChangelogFile()?.exists()
    assert getControlDirectory()?.exists()
    assert getOutputFile()
    assert getData()

    getData().with {
      file {
        name = getCopyrightFile().canonicalPath
        target = "usr/share/doc/${getPackagename()}/copyright"
      }
    }

//    publicationUploads = getPublications().collect { configuredPublication ->
//      if (configuredPublication instanceof CharSequence) {
//        Publication publication = project.extensions.getByType(PublishingExtension).publications.findByName(configuredPublication)
//        if (publication != null) {
//          return collectArtifacts(publication)
//        }
//        else {
//          logger.error("{}: Could not find publication: {}.", path, configuredPublication);
//        }
//      }
//      else if (conf instanceof MavenPublication) {
//        return collectArtifacts((Configuration) configuredPublication)
//      }
//      else {
//        logger.error("{}: Unsupported publication type: {}.", path, configuredPublication.class)
//      }
//      null
//    }.flatten() as Artifact[]

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

  DataProducer[] createDataProducers(Data data) {
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

    return result
  }
}
