package de.gesellix.gradle.debian.tasks.data

import spock.lang.Specification

class DataLinkTest extends Specification {

  def "name should default to null"() {
    expect:
    new DataLink().name == null
  }

  def "path should default to null"() {
    expect:
    new DataLink().path == null
  }

  def "symbolic should default to true"() {
    expect:
    new DataLink().symbolic
  }
}
