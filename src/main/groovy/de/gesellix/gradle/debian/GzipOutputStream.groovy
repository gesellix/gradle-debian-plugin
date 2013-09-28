package de.gesellix.gradle.debian

import java.util.zip.Deflater
import java.util.zip.GZIPOutputStream

class GzipOutputStream extends GZIPOutputStream {

  GzipOutputStream(OutputStream out) throws IOException {
//    super(out, new Deflater(Deflater.BEST_COMPRESSION, true),
//          size,
//          syncFlush);
//    usesDefaultDeflater = false;
//    writeHeader();
//    crc.reset();

    super(out)
    this.def.setLevel(Deflater.BEST_COMPRESSION)
//    this.usesDefaultDeflater = false
  }
}
