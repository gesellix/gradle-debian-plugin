package de.gesellix.gradle.debian

import de.gesellix.gradle.debian.tasks.BuildDebianPackageTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

import static de.gesellix.gradle.debian.DebianPackagePluginExtension.DEBPKGPLUGIN_EXTENSION_NAME
import static de.gesellix.gradle.debian.tasks.BuildDebianPackageTask.DEBPKGTASK_NAME
import static org.apache.commons.lang.StringUtils.capitalize

class DebianPackagePlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    def extension = project.extensions.create(DEBPKGPLUGIN_EXTENSION_NAME, DebianPackagePluginExtension)
    extension.with {
      packagename = project.name
    }
    addTasks(project)
  }

  def addTasks(Project project) {
    project.afterEvaluate {
      project.tasks.withType(BuildDebianPackageTask).whenTaskAdded { task ->
        def extension = project.extensions.findByName(DEBPKGPLUGIN_EXTENSION_NAME)
        task.conventionMapping.copyrightFile = { extension.copyrightFile }
        task.conventionMapping.changelogFile = { extension.changelogFile }
        task.conventionMapping.controlDirectory = { extension.controlDirectory }
        task.conventionMapping.packagename = { extension.packagename }
        task.conventionMapping.outputFile = {
          extension.outputFile ? extension.outputFile : new File("${project.buildDir}/${extension.packagename}.deb")
        }

        task.conventionMapping.publications = { extension.publications }
        if (extension.publications?.length) {
          def publicationExt = project.extensions.findByType(PublishingExtension)
          if (!publicationExt) {
            project.logger.warn "The publication extension point does not exist in project."
          }
          else {
            extension.publications.each { publicationName ->
              Publication publication = publicationExt?.publications?.findByName(publicationName)
              if (!publication) {
                project.logger.warn "Publication {} not found in project.", publication.name
              }
              else if (publication instanceof MavenPublication) {
                def taskName = "generatePomFileFor${capitalize(publicationName)}Publication"
                Task publishToLocalTask = project.tasks.findByName(taskName)
                task.dependsOn(publishToLocalTask)
              }
              else {
                project.logger.warn "{} can only use maven publications - skipping {}.", task.path, publication.name
              }
            }
          }
        }
      }
      project.task(DEBPKGTASK_NAME, type: BuildDebianPackageTask)
    }
  }
}
