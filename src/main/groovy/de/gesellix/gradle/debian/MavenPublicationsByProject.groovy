package de.gesellix.gradle.debian

import org.gradle.api.Project
import org.gradle.api.publish.Publication

class MavenPublicationsByProject {

  Project project
  Publication[] publications

  MavenPublicationsByProject(Project project) {
    this.project = project
    this.publications = []
  }
}
