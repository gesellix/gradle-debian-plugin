package de.gesellix.gradle.debian.tasks

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Artifact {

  String name
  String groupId
  String version
  String extension
  String type
  String classifier
  File file

  def getPath() {
    (groupId?.replaceAll('\\.', '/') ?: "") + "/$name/$version/$name-$version" + (classifier ? "-$classifier" : "") +
    (extension ? ".$extension" : "")
  }
}
