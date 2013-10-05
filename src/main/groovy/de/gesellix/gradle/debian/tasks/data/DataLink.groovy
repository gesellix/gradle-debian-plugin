package de.gesellix.gradle.debian.tasks.data

import org.gradle.api.tasks.Input

class DataLink {

  @Input
  String name
  @Input
  String path;
  @Input
  boolean symbolic = true;
}
