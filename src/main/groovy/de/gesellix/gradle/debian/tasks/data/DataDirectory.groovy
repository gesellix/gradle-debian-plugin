package de.gesellix.gradle.debian.tasks.data

class DataDirectory implements Serializable {

  String name
  String[] inclusions
  String[] exclusions
}
