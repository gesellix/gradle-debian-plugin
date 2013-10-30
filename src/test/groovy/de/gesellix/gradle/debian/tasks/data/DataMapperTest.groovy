package de.gesellix.gradle.debian.tasks.data

import spock.lang.Specification

class DataMapperTest extends Specification {

  def "fileMode should default to null"() {
    expect:
    new DataMapper().fileMode == null
  }
}
