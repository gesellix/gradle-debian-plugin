package de.gesellix.gradle.debian.tasks

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

  boolean equals(o) {
    if (this.is(o)) {
      return true
    }
    if (getClass() != o.class) {
      return false
    }

    Artifact artifact = (Artifact) o

    if (classifier != artifact.classifier) {
      return false
    }
    if (extension != artifact.extension) {
      return false
    }
    if (file != artifact.file) {
      return false
    }
    if (groupId != artifact.groupId) {
      return false
    }
    if (name != artifact.name) {
      return false
    }
    if (type != artifact.type) {
      return false
    }
    if (version != artifact.version) {
      return false
    }

    return true
  }

  int hashCode() {
    int result
    result = name.hashCode()
    result = 31 * result + groupId.hashCode()
    result = 31 * result + version.hashCode()
    result = 31 * result + (extension != null ? extension.hashCode() : 0)
    result = 31 * result + (type != null ? type.hashCode() : 0)
    result = 31 * result + (classifier != null ? classifier.hashCode() : 0)
    result = 31 * result + file.hashCode()
    return result
  }
}
