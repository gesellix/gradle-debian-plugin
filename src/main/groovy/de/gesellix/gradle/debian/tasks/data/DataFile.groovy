package de.gesellix.gradle.debian.tasks.data

import org.gradle.util.ConfigureUtil

class DataFile {

  String name
  String target
  DataMapper mapper = new DataMapper()

  def mapper(Closure closure) {
    ConfigureUtil.configure(closure, mapper)
  }
}
