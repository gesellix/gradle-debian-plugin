package de.gesellix.gradle.debian.tasks.jdeb

import org.apache.commons.io.IOUtils
import org.vafer.jdeb.DataConsumer
import org.vafer.jdeb.DataProducer
import org.vafer.jdeb.mapping.Mapper
import org.vafer.jdeb.producers.AbstractDataProducer
import org.vafer.jdeb.shaded.compress.compress.archivers.tar.TarArchiveEntry

import static java.util.zip.Deflater.BEST_COMPRESSION
import static org.apache.commons.compress.archivers.tar.TarArchiveEntry.DEFAULT_FILE_MODE
import static org.apache.commons.io.IOUtils.closeQuietly

class DataProducerChangelog extends AbstractDataProducer implements DataProducer {

  private File file
  private String destinationName

  DataProducerChangelog(File file, String destinationName, String[] includes, String[] excludes, Mapper[] mapper) {
    super(includes, excludes, mapper)
    this.destinationName = destinationName
    this.file = file
  }

  @Override
  void produce(DataConsumer receiver) throws IOException {

    String fileName
    if (destinationName != null && destinationName.trim().length() > 0) {
      fileName = destinationName.trim()
    }
    else {
      fileName = file.getName()
    }

    TarArchiveEntry entry = new TarArchiveEntry(fileName, true)
    entry.setUserId(0)
    entry.setUserName("root")
    entry.setGroupId(0)
    entry.setGroupName("root")
    entry.setMode(DEFAULT_FILE_MODE)

    entry = map(entry)

    def compressedAsBytes = getCompressed(new FileInputStream(file))
    entry.setSize(compressedAsBytes.size())

    final InputStream inputStream = new ByteArrayInputStream(compressedAsBytes)
    try {
      receiver.onEachFile(inputStream, entry)
    }
    catch (Throwable t) {
      println t
      throw t
    }
    finally {
      closeQuietly(inputStream)
    }
  }

  byte[] getCompressed(InputStream input) {
    def streamAsBytes = new ByteArrayOutputStream()
    OutputStream output = new GzipOutputStream(streamAsBytes)
    output.level = BEST_COMPRESSION
    IOUtils.copy(input, output)
    closeQuietly(input)
    closeQuietly(output)
    return streamAsBytes.toByteArray()
  }
}
