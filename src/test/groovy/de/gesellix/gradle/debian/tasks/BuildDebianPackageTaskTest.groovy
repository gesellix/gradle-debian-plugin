package de.gesellix.gradle.debian.tasks

import com.google.common.base.Predicate
import de.gesellix.gradle.debian.tasks.BuildDebianPackageTask
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test
import org.vafer.jdeb.mapping.Mapper
import org.vafer.jdeb.producers.DataProducerFile

import java.util.zip.GZIPInputStream

import static org.apache.commons.io.IOUtils.toByteArray

class BuildDebianPackageTaskTest {

  @Test
  public void "can add task to project"() {
    Project project = ProjectBuilder.builder().build()
    def task = project.task('buildDeb', type: BuildDebianPackageTask)
    assert task instanceof BuildDebianPackageTask
  }

  @Test(dependsOnMethods = ["can add task to project"])
  public void "asserts mandatory fields"() {
    // TODO
  }

  @Test(dependsOnMethods = ["can add task to project"])
  public void "can create output file"() {
    def outputFile = new File("./build/output.deb")
    if (outputFile.exists()) {
      outputFile.delete()
    }
    assert !outputFile.exists()

    Project project = ProjectBuilder.builder()
        .withName("packagename")
        .build()
    project.version = "42"
    def task = project.task('buildDeb', type: BuildDebianPackageTask)
    task.controlDirectory = new File("./src/test/resources/inputfiles/debian/control")
    task.changelogFile = new File("./src/test/resources/packagename/debian/changelog")
    task.copyrightFile = new File("./src/test/resources/inputfiles/debian/copyright")
    task.dataProducers = [
        new DataProducerFile(new File("./src/test/resources/inputfiles/debian/input.txt"), "/usr/test/input.txt", [] as String[], [] as String[], [] as Mapper[]),
        new DataProducerFile(new File("./src/test/resources/inputfiles/debian/binary.jpg"), "/usr/test/2/binary.jpg", [] as String[], [] as String[], [] as Mapper[])
    ]
    task.outputFile = outputFile
    task.packagename = "packagename"

    task.buildPackage()

    assert outputFile.exists()
    assertDebianArchiveContents(outputFile, [
        "debian-binary": "2.0\n",
        "control.tar.gz": [
            "./control": new TarEntryFileMatcher("./src/test/resources/expected/control"),
            "./md5sums": new TarEntryFileMatcher("./src/test/resources/expected/md5sums")],
        "data.tar.gz": [
            "./usr/": null,
            "./usr/share/": null,
            "./usr/share/doc/": null,
            "./usr/share/doc/packagename/": null,
            "./usr/share/doc/packagename/changelog.gz": new TarEntryGzipMatcher("./src/test/resources/expected/changelog.gz"),
            "./usr/share/doc/packagename/copyright": new TarEntryFileMatcher("./src/test/resources/expected/copyright"),
            "./usr/test/": null,
            "./usr/test/input.txt": new TarEntryFileMatcher("./src/test/resources/inputfiles/debian/input.txt"),
            "./usr/test/2/": null,
            "./usr/test/2/binary.jpg": new TarEntryFileMatcher("./src/test/resources/inputfiles/debian/binary.jpg")
        ]])
  }

  static def assertDebianArchiveContents(File file, Map<String, Object> reference) {
    def arArchive = new ArArchiveInputStream(new FileInputStream(file))
    def arEntry
    while ((arEntry = arArchive.nextEntry) != null) {
      assert !arEntry.directory
      assert arEntry.name in reference.keySet()

      if (!arEntry.name.endsWith(".tar.gz")) {
        assert toByteArray(arArchive) == reference[arEntry.name].bytes
      }
      else {
        def tarArchive = new TarArchiveInputStream(new GzipCompressorInputStream(arArchive))
        def tarEntry
        while ((tarEntry = tarArchive.nextEntry) != null) {
          println tarEntry.name
          assert tarEntry.name in reference[arEntry.name].keySet()
          if (!tarEntry.directory) {
            def entryMatcher = reference[arEntry.name][tarEntry.name] as TarEntryFileMatcher
            def actualBytes = toByteArray(tarArchive)
            assert entryMatcher.apply(actualBytes)
          }
        }
      }
    }
  }

  static class TarEntryFileMatcher implements Predicate<byte[]> {

    def File file

    TarEntryFileMatcher(String file) {
      this.file = new File(file)
    }

    @Override
    boolean apply(byte[] actualBytes) {
      def expectedBytes = readExpectedBytesFromFile()

      assert actualBytes.length == expectedBytes.length
      assert actualBytes == expectedBytes

      return true
    }

    def byte[] readExpectedBytesFromFile() {
      return toByteArray(new FileInputStream(file))
    }
  }

  static class TarEntryGzipMatcher extends TarEntryFileMatcher {

    TarEntryGzipMatcher(String file) {
      super(file)
    }

    @Override
    boolean apply(byte[] actualBytes) {
      def actualUnzippedBytes = toByteArray(new GZIPInputStream(new ByteArrayInputStream(actualBytes)))
      return super.apply(actualUnzippedBytes)
    }

    def byte[] readExpectedBytesFromFile() {
      return toByteArray(new GZIPInputStream(new FileInputStream(file)))
    }
  }
}
