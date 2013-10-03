package de.gesellix.gradle.debian

class DebianPackagePluginExtension {

  public static final String DEBPKGPLUGIN_EXTENSION_NAME = "debPkgPlugin"

  String packagename
  String[] publications
  File controlDirectory
  File copyrightFile
  File changelogFile
  File outputFile
}
