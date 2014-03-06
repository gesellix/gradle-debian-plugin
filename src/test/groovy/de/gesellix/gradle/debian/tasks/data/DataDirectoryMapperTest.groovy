package de.gesellix.gradle.debian.tasks.data

import spock.lang.Specification

class DataDirectoryMapperTest extends Specification {

  def "filename should default to null"() {
    expect:
    new DataDirectoryMapper().filename == null
  }
}
