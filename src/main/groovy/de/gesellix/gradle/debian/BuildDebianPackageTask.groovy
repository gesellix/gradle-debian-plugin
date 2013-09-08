package de.gesellix.gradle.debian

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.vafer.jdeb.Console
import org.vafer.jdeb.DataProducer
import org.vafer.jdeb.Processor
import org.vafer.jdeb.utils.MapVariableResolver

import static org.vafer.jdeb.Compression.GZIP

class BuildDebianPackageTask extends DefaultTask {

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

    def packageDescriptor = processor.createDeb(controlFiles, dataProducers, outputFile, GZIP)
//    processor.createChanges(packageDescriptor, null, null, null, null, )
  }
}
