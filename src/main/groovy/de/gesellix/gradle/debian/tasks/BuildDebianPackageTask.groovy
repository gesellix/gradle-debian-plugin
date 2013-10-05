package de.gesellix.gradle.debian.tasks

import de.gesellix.gradle.debian.MavenPublicationsByProject
import de.gesellix.gradle.debian.PublicationFinder
import de.gesellix.gradle.debian.tasks.data.Data
import de.gesellix.gradle.debian.tasks.jdeb.DataProducerChangelog
import org.gradle.api.DefaultTask
import org.gradle.api.publish.Publication
import org.gradle.api.publish.maven.MavenPublication
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

  private PublicationFinder publicationFinder = new PublicationFinder()

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

    if (getPublications()?.length) {
      def publicationsByProject = publicationFinder.findPublicationsInProject(project, getPublications() as String[])
      publicationsByProject.each { MavenPublicationsByProject mavenPublicationByProject ->
        mavenPublicationByProject.publications.each { publication ->
          def artifacts = collectArtifacts(publication)
          artifacts.each { artifact ->
            getData().with {
              file {
                name = artifact.file
                target = "usr/share/${getPackagename()}/webapps"
              }
            }
          }
        }
      }
    }

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

  Artifact[] collectArtifacts(Publication publication) {
    if (!publication instanceof MavenPublication) {
      logger.info "{} can only use maven publications - skipping {}.", path, publication.name
      return []
    }
    def identity = publication.mavenProjectIdentity
    def artifacts = publication.artifacts.findResults {
      new Artifact(
          name: identity.artifactId,
          groupId: identity.groupId,
          version: identity.version,
          extension: it.extension,
          type: it.extension,
          classifier: it.classifier,
          file: it.file)
    }

    //Add the pom
//    artifacts << new Artifact(
//        name: identity.artifactId, groupId: identity.groupId, version: identity.version,
//        extension: 'pom', type: 'pom', file: publication.asNormalisedPublication().pomFile)
    artifacts
  }

  static class Artifact {

    String name
    String groupId
    String version
    String extension
    String type
    String classifier
    File file

    def getPath() {
      (groupId?.replaceAll('\\.', '/') ?: "") + "/$name/$version/$name-$version" + (classifier ? "-$classifier" : "") +
      (extension ? ".$extension" : "")
    }

    boolean equals(o) {
      if (this.is(o)) {
        return true
      }
      if (getClass() != o.class) {
        return false
      }

      Artifact artifact = (Artifact) o

      if (classifier != artifact.classifier) {
        return false
      }
      if (extension != artifact.extension) {
        return false
      }
      if (file != artifact.file) {
        return false
      }
      if (groupId != artifact.groupId) {
        return false
      }
      if (name != artifact.name) {
        return false
      }
      if (type != artifact.type) {
        return false
      }
      if (version != artifact.version) {
        return false
      }

      return true
    }

    int hashCode() {
      int result
      result = name.hashCode()
      result = 31 * result + groupId.hashCode()
      result = 31 * result + version.hashCode()
      result = 31 * result + (extension != null ? extension.hashCode() : 0)
      result = 31 * result + (type != null ? type.hashCode() : 0)
      result = 31 * result + (classifier != null ? classifier.hashCode() : 0)
      result = 31 * result + file.hashCode()
      return result
    }
  }
}
