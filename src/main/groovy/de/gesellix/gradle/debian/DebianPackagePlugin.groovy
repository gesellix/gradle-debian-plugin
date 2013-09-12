package de.gesellix.gradle.debian

import org.gradle.api.Plugin
import org.gradle.api.Project

class DebianPackagePlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    project.extensions.create("debPkgPlugin", DebianPackagePluginExtension)
    project.task(BuildDebianPackageTask.NAME,
                 group: "Build",
                 type: BuildDebianPackageTask,
                 description: "Build debian package") {}
  }
}
