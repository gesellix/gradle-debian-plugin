package de.gesellix.gradle.debian.tasks.jdeb

import org.vafer.jdeb.mapping.Mapper
import org.vafer.jdeb.shaded.compress.compress.archivers.tar.TarArchiveEntry

class ClosureFilenameMapper implements Mapper {

  private Closure mapping

  ClosureFilenameMapper(Closure mapping) {
    this.mapping = mapping
  }

  @Override
  TarArchiveEntry map(TarArchiveEntry e) {
    if (mapping != null) {
      e.setName(mapping(e.getName()))
    }
    return e
  }
}
