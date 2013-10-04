package de.gesellix.gradle.debian

import de.gesellix.gradle.debian.tasks.BuildDebianPackageTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import static de.gesellix.gradle.debian.DebianPackagePluginExtension.DEBPKGPLUGIN_EXTENSION_NAME
import static de.gesellix.gradle.debian.tasks.BuildDebianPackageTask.DEBPKGTASK_NAME
import static org.apache.commons.lang.StringUtils.capitalize

class DebianPackagePlugin implements Plugin<Project> {

  private PublicationFinder publicationFinder = new PublicationFinder()

  @Override
  void apply(Project project) {
    def extension = project.extensions.create(DEBPKGPLUGIN_EXTENSION_NAME, DebianPackagePluginExtension, project)
    extension.with {
      packagename = project.name
    }
    addTasks(project)
  }

  def addTasks(Project project) {
    project.afterEvaluate {
      project.tasks.withType(BuildDebianPackageTask).whenTaskAdded { task ->
        def extension = project.extensions.findByName(DEBPKGPLUGIN_EXTENSION_NAME)
        task.conventionMapping.with {
          copyrightFile = { project.file(extension.copyrightFile) }
          changelogFile = { project.file(extension.changelogFile) }
          controlDirectory = { project.file(extension.controlDirectory) }
          packagename = { extension.packagename }
          publications = { extension.publications }
          data = { extension.data }
          outputFile = {
            extension.outputFile ? project.file(extension.outputFile) : new File("${project.buildDir}/${extension.packagename}.deb")
          }
        }

        if (extension.publications?.length) {
          def publicationsByProject = publicationFinder.findPublicationsInProject(project, extension.publications as String[])
          publicationsByProject.each { MavenPublicationsByProject mavenPublicationByProject ->
            mavenPublicationByProject.publications.each { publication ->
              def taskName = "generatePomFileFor${capitalize(publication.name)}Publication"
              Task publicationTask = mavenPublicationByProject.project.tasks.findByName(taskName)
              task.dependsOn(publicationTask)
            }
          }
        }
      }
      project.task(DEBPKGTASK_NAME, type: BuildDebianPackageTask)
    }
  }
}
