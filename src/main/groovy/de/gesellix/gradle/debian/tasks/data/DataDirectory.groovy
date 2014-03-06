package de.gesellix.gradle.debian.tasks.data

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.util.ConfigureUtil

class DataDirectory {

  @Input
  String name
  @Input
  String[] inclusions = null
  @Input
  String[] exclusions = null
  @Nested
  DataDirectoryMapper mapper = new DataDirectoryMapper()

  def mapper(Closure closure) {
    ConfigureUtil.configure(closure, mapper)
  }
}
