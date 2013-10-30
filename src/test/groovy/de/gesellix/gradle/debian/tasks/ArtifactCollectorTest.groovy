package de.gesellix.gradle.debian.tasks

import org.gradle.api.logging.Logger
import org.gradle.api.publish.ivy.IvyPublication
import spock.lang.Specification
import spock.lang.Unroll

class ArtifactCollectorTest extends Specification {

  ArtifactCollector artifactCollector
  Logger logger

  def setup() {
    logger = Mock(Logger)
    artifactCollector = new ArtifactCollector("path", logger)
  }

  @Unroll("returns no artifacts for #publicationClass")
  def "accepts only MavenPublications"(publicationClass) {
    def artifacts
    def publicationMock = Mock(publicationClass)
    given:
    publicationMock.name >> "test"
    when:
    artifacts = artifactCollector.collectArtifacts(publicationMock)
    then:
    artifacts == []
    1 * logger.info("{} can only use maven publications - skipping {}.", "path", "test")
    where:
    publicationClass << [IvyPublication]
  }
}
