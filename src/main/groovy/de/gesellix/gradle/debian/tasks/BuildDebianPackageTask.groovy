package de.gesellix.gradle.debian.tasks

import de.gesellix.gradle.debian.tasks.jdeb.DataProducerChangelog
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.vafer.jdeb.Console
import org.vafer.jdeb.DataProducer
import org.vafer.jdeb.Processor
import org.vafer.jdeb.mapping.Mapper
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
  DataProducer[] dataProducers
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
    assert getCopyrightFile()?.exists()
    assert getCopyrightFile()?.exists()
    assert getChangelogFile()?.exists()
    assert getControlDirectory()?.exists()
    assert getOutputFile()

    def processor = new Processor([
                                      info: { msg -> logger.info(msg) },
                                      warn: { msg -> logger.warn(msg) }] as Console,
                                  new MapVariableResolver([
                                      name: getPackagename(),
                                      version: project.version]))

    dataProducers = dataProducers.toList() << new DataProducerChangelog(getChangelogFile(), "/usr/share/doc/${getPackagename()}/changelog.gz", [] as String[], [] as String[], [] as Mapper[])
    dataProducers = dataProducers.toList() << new DataProducerFile(getCopyrightFile(), "/usr/share/doc/${getPackagename()}/copyright", [] as String[], [] as String[], [] as Mapper[])
    def packageDescriptor = processor.createDeb(getControlDirectory().listFiles(), dataProducers, getOutputFile(), GZIP)
  }
}
