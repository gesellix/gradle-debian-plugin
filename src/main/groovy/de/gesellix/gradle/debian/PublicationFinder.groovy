package de.gesellix.gradle.debian

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

class PublicationFinder {

  def findPublicationsInProject(Project project, String[] publicationNames) {
    def publicationsByProject = []

    project.rootProject.allprojects { Project p ->
      def publicationByProject = new MavenPublicationsByProject(p)
      publicationsByProject << publicationByProject

      def publishingExtension = p.extensions.findByType(PublishingExtension)
      if (publishingExtension) {
        publicationNames.each { publicationName ->
          def publication = publishingExtension.publications.findByName(publicationName)
          if (publication in MavenPublication) {
            publicationByProject.publications = publicationByProject.publications.toList() << publication
          }
        }
      }
    }

    return publicationsByProject
  }
}
