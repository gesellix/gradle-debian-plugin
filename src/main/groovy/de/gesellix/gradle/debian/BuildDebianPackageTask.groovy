package de.gesellix.gradle.debian

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

  @InputFile
  File copyrightFile
  @InputFile
  File changelogFile
  @InputFiles
  File[] controlFiles
  @Input
  DataProducer[] dataProducers
  @OutputFile
  File outputFile

  BuildDebianPackageTask() {
  }

  @TaskAction
  def buildPackage() {
    def processor = new Processor([
                                      info: { msg -> logger.info(msg) },
                                      warn: { msg -> logger.warn(msg) }] as Console,
                                  new MapVariableResolver([
                                      name: "test-name",
                                      version: "42"]))

    dataProducers = dataProducers.toList() << new DataProducerFile(copyrightFile, "/usr/share/doc/test-name/copyright", [] as String[], [] as String[], [] as Mapper[])
    def packageDescriptor = processor.createDeb(controlFiles, dataProducers, outputFile, GZIP)
//    dataProducers = dataProducers.toList() << new DataProducerFile(changelogFile, "/usr/share/doc/test-name/changelog.gz", [] as String[], [] as String[], [] as Mapper[])
//    processor.createChanges(packageDescriptor, null, null, null, null, )
  }
}
