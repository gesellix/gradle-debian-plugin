package de.gesellix.gradle.debian

import org.apache.commons.compress.archivers.ar.ArArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test
import org.vafer.jdeb.mapping.Mapper
import org.vafer.jdeb.producers.DataProducerFile

class BuildDebianPackageTaskTest {

  @Test
  public void "can add task to project"() {
    Project project = ProjectBuilder.builder().build()
    def task = project.task('buildDeb', type: BuildDebianPackageTask)
    assert task instanceof BuildDebianPackageTask
  }

  @Test(dependsOnMethods = ["can add task to project"])
  public void "can create output file"() {
    def outputFile = new File("./build/output.deb")
    if (outputFile.exists()) {
      outputFile.delete()
    }
    assert !outputFile.exists()

    Project project = ProjectBuilder.builder().build()
    def task = project.task('buildDeb', type: BuildDebianPackageTask)
    task.controlDirectory = new File("./src/test/resources/debian/control")
    task.controlFiles = new File("./src/test/resources/debian/control").listFiles()
    task.copyrightFile = new File("./src/test/resources/debian/copyright")
//    task.changelogFile = new File("./src/test/resources/debian/changelog")
    task.dataProducers = [
        new DataProducerFile(new File("./src/test/resources/debian/input.txt"), "/usr/test/input.txt", [] as String[], [] as String[], [] as Mapper[]),
        new DataProducerFile(new File("./src/test/resources/debian/binary.jpg"), "/usr/test/2/binary.jpg", [] as String[], [] as String[], [] as Mapper[])
    ]
    task.outputFile = outputFile

    task.buildPackage()

    assert outputFile.exists()
    assertDebianArchiveContents(outputFile, [
        "debian-binary": "2.0\n",
        "control.tar.gz": [
            "./control": new File("./src/test/resources/expected/control"),
            "./md5sums": new File("./src/test/resources/expected/md5sums")],
        "data.tar.gz": [
            "./usr/": null,
            "./usr/share/": null,
            "./usr/share/doc/": null,
            "./usr/share/doc/test-name/": null,
//            "./usr/share/doc/test-name/changelog.gz": new File("./src/test/resources/expected/changelog"),
            "./usr/share/doc/test-name/copyright": new File("./src/test/resources/expected/copyright"),
            "./usr/test/": null,
            "./usr/test/input.txt": new File("./src/test/resources/debian/input.txt"),
            "./usr/test/2/": null,
            "./usr/test/2/binary.jpg": new File("./src/test/resources/debian/binary.jpg")
        ]])
  }

  static def assertDebianArchiveContents(File file, Map<String, Object> reference) {
    def arArchive = new ArArchiveInputStream(new FileInputStream(file))
    def arEntry
    while ((arEntry = arArchive.nextEntry) != null) {
      assert !arEntry.directory
      assert arEntry.name in reference.keySet()

      if (!arEntry.name.endsWith(".tar.gz")) {
        def outputStream = new ByteArrayOutputStream()
        IOUtils.copy(arArchive, outputStream)
        outputStream.close()
        assert outputStream.toByteArray() == reference[arEntry.name].bytes
      }
      else {
        def tarArchive = new TarArchiveInputStream(new GzipCompressorInputStream(arArchive))
        def tarEntry
        while ((tarEntry = tarArchive.nextEntry) != null) {
          assert tarEntry.name in reference[arEntry.name].keySet()
          if (!tarEntry.directory) {
            def outputStream = new ByteArrayOutputStream()
            IOUtils.copy(tarArchive, outputStream)
            outputStream.close()

            def actualBytes = outputStream.toByteArray()
            def expectedBytes = IOUtils.toByteArray(new FileInputStream(reference[arEntry.name][tarEntry.name] as File))

            assert actualBytes.length == expectedBytes.length
            assert actualBytes == expectedBytes
          }
        }
      }
    }
  }
}
