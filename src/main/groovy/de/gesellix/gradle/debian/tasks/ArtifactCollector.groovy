package de.gesellix.gradle.debian.tasks

import org.gradle.api.publish.Publication
import org.gradle.api.publish.maven.MavenPublication

class ArtifactCollector {

  def logger
  def path

  ArtifactCollector(path, logger) {
    this.path = path
    this.logger = logger
  }

  def collectArtifacts(Publication publication) {
    if (!(publication in MavenPublication)) {
      logger.info "{} can only use maven publications - skipping {}.", path, publication.name
      return []
    }
    def identity = publication.mavenProjectIdentity
    def artifacts = publication.artifacts.findResults {
      new Artifact(
          name: identity.artifactId,
          groupId: identity.groupId,
          version: identity.version,
          extension: it.extension,
          type: it.extension,
          classifier: it.classifier,
          file: it.file)
    }

    //Add the pom
//    artifacts << new Artifact(
//        name: identity.artifactId, groupId: identity.groupId, version: identity.version,
//        extension: 'pom', type: 'pom', file: publication.asNormalisedPublication().pomFile)
    artifacts
  }
}
