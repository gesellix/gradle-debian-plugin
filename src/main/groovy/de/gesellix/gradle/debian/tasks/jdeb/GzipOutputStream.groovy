package de.gesellix.gradle.debian.tasks.jdeb

import java.util.zip.GZIPOutputStream

class GzipOutputStream extends GZIPOutputStream {

  GzipOutputStream(OutputStream out) throws IOException {
    super(out)
  }

  def setLevel(int compressionLevel) {
    this.def.setLevel(compressionLevel)
  }
}
