package de.gesellix.gradle.debian

import de.gesellix.gradle.debian.tasks.data.Data
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class DebianPackagePluginExtension {

  public static final String DEBPKGPLUGIN_EXTENSION_NAME = "debPkgPlugin"

  Project project

  String packagename
  String controlDirectory
  String copyrightFile
  String changelogFile
  String[] publications
  Data data = new Data()
  String outputFile

  DebianPackagePluginExtension(Project project) {
    this.project = project
  }

  def data(Closure closure) {
    ConfigureUtil.configure(closure, data)
  }
}
