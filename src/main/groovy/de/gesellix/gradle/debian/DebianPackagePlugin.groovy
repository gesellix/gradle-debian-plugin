package de.gesellix.gradle.debian

import de.gesellix.gradle.debian.tasks.BuildDebianPackageTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

import static de.gesellix.gradle.debian.DebianPackagePluginExtension.DEBPKGPLUGIN_EXTENSION_NAME
import static de.gesellix.gradle.debian.tasks.BuildDebianPackageTask.DEBPKGTASK_NAME
import static org.gradle.api.publish.maven.plugins.MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME

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
      project.tasks.withType(BuildDebianPackageTask).configureEach { task ->
        def extension = project.extensions.findByName(DEBPKGPLUGIN_EXTENSION_NAME)
        task.conventionMapping.with {
          changelogFile = { project.file(extension.changelogFile) }
          controlDirectory = { project.file(extension.controlDirectory) }
          packagename = { extension.packagename }
          publications = { extension.publications }
          data = { extension.data }
          outputFile = {
            extension.outputFile ? project.file(extension.outputFile) : new File("${project.buildDir}/${extension.packagename}-${project.version}.deb")
          }
        }

        if (extension.publications?.length) {
          def publicationsByProject = publicationFinder.findPublicationsInProject(project, extension.publications as String[])
          publicationsByProject.each { MavenPublicationsByProject mavenPublicationByProject ->
            mavenPublicationByProject.publications.each { MavenPublication publication ->
/*
              def taskName = "generatePomFileFor${capitalize(publication.name)}Publication"
              Task publicationTask = mavenPublicationByProject.project.tasks.findByName(taskName)
              if (publicationTask) {
                task.dependsOn(publicationTask)
              } else {
//                def publishTask = mavenPublicationByProject.project.tasks.findByName('publish')
//                task.dependsOn(publishTask)
//                task.dependsOn({ mavenPublicationByProject.project.tasks.findByName(taskName) })
                task.dependsOn(publication.getArtifacts())
              }
*/
              def publishTask = mavenPublicationByProject.project.tasks.findByName(PUBLISH_LOCAL_LIFECYCLE_TASK_NAME)
              task.dependsOn(publishTask)

              def assembleTask = mavenPublicationByProject.project.tasks.findByName(ASSEMBLE_TASK_NAME)
              task.dependsOn(assembleTask)
            }
          }
        }
      }
      project.task(DEBPKGTASK_NAME, type: BuildDebianPackageTask)
    }
  }
}
