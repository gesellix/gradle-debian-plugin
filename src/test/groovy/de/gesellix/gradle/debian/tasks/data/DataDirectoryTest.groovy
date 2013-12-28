package de.gesellix.gradle.debian.tasks.data

import spock.lang.Specification

class DataDirectoryTest extends Specification {

  def "name should default to null"() {
    expect:
    new DataDirectory().name == null
  }

  def "inclusions should default to null"() {
    expect:
    new DataDirectory().inclusions == null
  }

  def "exclusions should default to null"() {
    expect:
    new DataDirectory().exclusions == null
  }
}
