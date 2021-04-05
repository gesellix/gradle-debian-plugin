package de.gesellix.gradle.debian.tasks.data

import org.gradle.util.ConfigureUtil

class DataTemplate {

  String[] paths = null

  def mapper(Closure closure) {
    ConfigureUtil.configure(closure, mapper)
  }
}
