package de.gesellix.gradle.debian.tasks.jdeb

import org.vafer.jdeb.DataConsumer
import org.vafer.jdeb.mapping.Mapper
import spock.lang.Specification

import static org.vafer.jdeb.shaded.commons.compress.archivers.tar.TarArchiveEntry.DEFAULT_FILE_MODE

class DataProducerChangelogSpec extends Specification {

  DataProducerChangelog dataProducerChangelog
  DataConsumer dataConsumer

  def setup() {
    dataProducerChangelog = new DataProducerChangelog(File.createTempFile("gradle-plugin", "tmp"), "destinationFile", [] as String[], [] as String[], [] as Mapper[])
    dataConsumer = Mock(DataConsumer)
  }

  def "produces entry as File"() {
    when:
    dataProducerChangelog.produce(dataConsumer)
    then:
    1 * dataConsumer.onEachFile(_ as InputStream, {
      it.name == "destinationFile" &&
      it.userName == "root" &&
      it.groupName == "root" &&
      it.mode == DEFAULT_FILE_MODE &&
      it.size == 20
    })
    0 * dataConsumer.onEachDir(_, _, _, _, _, _, _, _)
    0 * dataConsumer.onEachLink(_, _, _, _, _, _, _, _)
  }
}
