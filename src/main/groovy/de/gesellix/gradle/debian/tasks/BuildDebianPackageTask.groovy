package de.gesellix.gradle.debian.tasks

import de.gesellix.gradle.debian.MavenPublicationsByProject
import de.gesellix.gradle.debian.PublicationFinder
import de.gesellix.gradle.debian.tasks.data.Data
import de.gesellix.gradle.debian.tasks.data.DataFile
import de.gesellix.gradle.debian.tasks.jdeb.DataProducerChangelog
import de.gesellix.gradle.debian.tasks.jdeb.DataProducerCreator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.vafer.jdeb.Console
import org.vafer.jdeb.DataProducer
import org.vafer.jdeb.DebMaker
import org.vafer.jdeb.mapping.Mapper
import org.vafer.jdeb.utils.MapVariableResolver

class BuildDebianPackageTask extends DefaultTask {

  public static final String DEBPKGTASK_NAME = 'buildDeb'

  @Internal
  PublicationFinder publicationFinder
  @Internal
  ArtifactCollector artifactCollector
  @Internal
  DataProducerCreator dataProducerCreator

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

    List<DataProducer> dataProducers = dataProducerCreator.createDataProducers(getData(), project)
    DataProducer[] conffileProducers = dataProducerCreator.createConffileProducers(getData(), project)
    dataProducers = dataProducers.toList() + conffileProducers.toList()
    dataProducers = dataProducers.toList() + new DataProducerChangelog(getChangelogFile(), "/usr/share/doc/${getPackagename()}/changelog.gz", [] as String[], [] as String[], [] as Mapper[])

    DebMaker debMaker = createDebMaker(dataProducers.toList(), conffileProducers.toList())
    debMaker.makeDeb()
  }

  DebMaker createDebMaker(List<DataProducer> dataProducers, List<DataProducer> conffileProducers) {
    Console console = [
        debug: { String msg -> logger.debug(msg) },
        info : { String msg -> logger.info(msg) },
        warn : { String msg -> logger.warn(msg) }] as Console
    def resolver = new MapVariableResolver([name   : getPackagename(),
                                            version: project.version] as Map<String, String>)

    def debMaker = new DebMaker(console, dataProducers, conffileProducers)
    debMaker.setResolver(resolver)
    debMaker.setControl(getControlDirectory())
    debMaker.setDeb(getOutputFile())
    debMaker.setChangesIn(getChangelogFile())
    return debMaker
  }

  void addPublicationArtifacts(String[] publicationNames, Data data, String packagename) {
    if (publicationNames?.length) {
      def publicationsByProject = publicationFinder.findPublicationsInProject(project, publicationNames as String[])
      publicationsByProject.each { MavenPublicationsByProject mavenPublicationByProject ->
        mavenPublicationByProject.publications.each { publication ->
          Collection<Artifact> artifacts = artifactCollector.collectArtifacts(publication)
          artifacts.each { artifact ->
            data.with {
              project.logger.info "adding artifact ${artifact.file}"
              DataFile file = new DataFile()
              file.name = artifact.file
              file.target = "usr/share/${packagename}/publications"
              files = files.toList() + file
            }
          }
        }
      }
    }
  }
}
