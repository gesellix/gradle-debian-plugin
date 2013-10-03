package de.gesellix.gradle.debian.tasks.data

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.util.ConfigureUtil

class DataFile {

  @Input
  String name
  @Input
  String target
  @Nested
  DataMapper mapper = new DataMapper()

  def mapper(Closure closure) {
    ConfigureUtil.configure(closure, mapper)
  }
}
