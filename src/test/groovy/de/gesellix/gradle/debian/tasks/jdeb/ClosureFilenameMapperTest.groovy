package de.gesellix.gradle.debian.tasks.jdeb

import org.vafer.jdeb.shaded.compress.compress.archivers.tar.TarArchiveEntry
import spock.lang.Specification

class ClosureFilenameMapperTest extends Specification {

  TarArchiveEntry tarArchiveEntry

  def setup() {
    tarArchiveEntry = Mock(TarArchiveEntryForTest)
  }

  def "accepts filename-mapping == null"() {
    when:
    new ClosureFilenameMapper(null).map(tarArchiveEntry)
    then:
    0 * tarArchiveEntry.setName(_)
  }

  def "updates filename when filename-mapping != null"() {
    given:
    tarArchiveEntry.name >> "/initial_name"
    when:
    new ClosureFilenameMapper({ String path -> "/another/path$path" }).map(tarArchiveEntry)
    then:
    1 * tarArchiveEntry.setName("/another/path/initial_name")
  }

  static class TarArchiveEntryForTest extends TarArchiveEntry {

    TarArchiveEntryForTest() {
      super("test")
    }
  }
}
