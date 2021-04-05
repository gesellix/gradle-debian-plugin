package de.gesellix.gradle.debian.tasks

import com.google.common.base.Predicate
import de.gesellix.gradle.debian.tasks.data.Data
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import java.util.zip.GZIPInputStream

import static org.apache.commons.io.IOUtils.toByteArray

class BuildDebianPackageTaskTest extends Specification {

  void "can add task to project"() {
    when:
    Project project = ProjectBuilder.builder().build()
    def task = project.task('buildDeb', type: BuildDebianPackageTask)

    then:
    task instanceof BuildDebianPackageTask
  }

  void "can create output file"() {
    given:
    def outputFile = new File("./build/output.deb")
    if (outputFile.exists()) {
      outputFile.delete()
    }
    assert !outputFile.exists()

    Project project = ProjectBuilder.builder()
        .withName("projectname")
        .build()
    project.version = "42"

    def task = project.task('buildDeb', type: BuildDebianPackageTask)
    task.controlDirectory = new File("./src/test/resources/packagename/control")
    task.changelogFile = new File("./src/test/resources/packagename/debian/changelog")
    task.data = new Data()
    task.data.with {
      def baseDir = new File(".").absolutePath
      dir {
        name = "${baseDir}/src/test/resources/inputfiles/subdirectory"
        exclusions = ["excludedFile.txt"]
        mapper {
          filename = { x -> "./opt/$x" }
        }
      }
      file {
        name = "${baseDir}/src/test/resources/inputfiles/input.txt"
        target = "usr/test/input.txt"
      }
      file {
        name = "${baseDir}/src/test/resources/inputfiles/binary.jpg"
        target = "usr/test/2/binary.jpg"
      }
    }
    task.outputFile = outputFile
    task.packagename = "packagename"

    when:
    task.buildPackage()

    then:
    outputFile.exists()
    and:
    assertDebianArchiveContents(outputFile, [
        "debian-binary" : "2.0\n",
        "control.tar.gz": [
            "./conffiles": new TarEntryFileMatcher("./src/test/resources/expected/conffiles"),
            "./prerm"    : new TarEntryFileMatcher("./src/test/resources/expected/prerm"),
            "./postinst" : new TarEntryFileMatcher("./src/test/resources/expected/postinst"),
            "./postrm"   : new TarEntryFileMatcher("./src/test/resources/expected/postrm"),
            "./control"  : new TarEntryFileMatcher("./src/test/resources/expected/control"),
            "./md5sums"  : new TarEntryFileMatcher("./src/test/resources/expected/md5sums")],
        "data.tar.gz"   : [
            "./opt/"                                  : null,
            "./opt/includedFile.txt"                  : new TarEntryFileMatcher("./src/test/resources/inputfiles/subdirectory/includedFile.txt"),
            "./opt/subsub/"                           : null,
            "./opt/subsub/anotherIncludedFile.txt"    : new TarEntryFileMatcher("./src/test/resources/inputfiles/subdirectory/subsub/anotherIncludedFile.txt"),
            "./usr/"                                  : null,
            "./usr/share/"                            : null,
            "./usr/share/doc/"                        : null,
            "./usr/share/doc/packagename/"            : null,
            "./usr/share/doc/packagename/changelog.gz": new TarEntryGzipMatcher("./src/test/resources/expected/changelog.gz"),
            "./usr/test/"                             : null,
            "./usr/test/input.txt"                    : new TarEntryFileMatcher("./src/test/resources/inputfiles/input.txt"),
            "./usr/test/2/"                           : null,
            "./usr/test/2/binary.jpg"                 : new TarEntryFileMatcher("./src/test/resources/inputfiles/binary.jpg")
        ]
    ])
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
          assert tarEntry.name in reference[arEntry.name].keySet()
          if (!tarEntry.directory) {
            def entryMatcher = reference[arEntry.name][tarEntry.name] as TarEntryFileMatcher
            def actualBytes = toByteArray(tarArchive)
            def matches = entryMatcher.apply(actualBytes)
            if (!matches) {
              println "${arEntry.name} -- ${tarEntry.name}"
            }
            assert matches
          }
        }
      }
    }
    return true
  }

  static class TarEntryFileMatcher implements Predicate<byte[]> {

    File file

    TarEntryFileMatcher(String file) {
      this.file = new File(file)
    }

    @Override
    boolean apply(byte[] actualBytes) {
      def expectedBytes = readExpectedBytesFromFile()

      if (actualBytes.length != expectedBytes.length) {
        println "${actualBytes.length}/${expectedBytes.length}"
      }
      if (actualBytes != expectedBytes) {
        println "\n** actualBytes:\n${new String(actualBytes)}\n** expectedBytes:\n${new String(expectedBytes)}"
      }
      assert actualBytes.length == expectedBytes.length
      assert actualBytes == expectedBytes

      return true
    }

    byte[] readExpectedBytesFromFile() {
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

    byte[] readExpectedBytesFromFile() {
      return toByteArray(new GZIPInputStream(new FileInputStream(file)))
    }
  }
}
