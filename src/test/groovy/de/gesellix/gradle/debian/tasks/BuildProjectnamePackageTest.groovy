package de.gesellix.gradle.debian.tasks

import de.gesellix.gradle.debian.tasks.data.Data
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test

class BuildProjectnamePackageTest {

  @Test
  public void "can create output file"() {
    def outputFile = new File("./build/packagename.deb")
    if (outputFile.exists()) {
      outputFile.delete()
    }
    assert !outputFile.exists()

    Project project = ProjectBuilder.builder()
        .withName("projectname")
        .build()
    project.version = 42

    def baseDir = new File("./src/test/resources/packagename").absolutePath
    def task = project.task('buildDeb', type: BuildDebianPackageTask)
    task.packagename = "packagename"
    task.controlDirectory = new File("${baseDir}/control")
    task.changelogFile = new File("${baseDir}/debian/changelog")
    task.outputFile = outputFile
    task.data = new Data()
    task.data.with {
      dir {
        name = "${baseDir}/data"
        exclusions = ["etc/init.d/packagename", "var/.gitkeep"]
      }
      file {
        name = "${baseDir}/data/etc/init.d/packagename"
        target = "etc/init.d/packagename"
        mapper {
          fileMode = "755"
        }
      }
      file {
        name = "${baseDir}/data/etc/cron.daily/packagename"
        target = "etc/cron.daily/packagename"
        mapper {
          fileMode = "755"
        }
      }
      file {
        name = "${baseDir}/data/usr/share/packagename/bin/setenv.sh"
        target = "usr/share/packagename/bin/setenv.sh"
        mapper {
          fileMode = "755"
        }
      }
      file {
        name = "${baseDir}/data/usr/share/packagename/bin/shutdown.sh"
        target = "usr/share/packagename/bin/shutdown.sh"
        mapper {
          fileMode = "755"
        }
      }
      file {
        name = "${baseDir}/data/usr/share/packagename/bin/startup.sh"
        target = "usr/share/packagename/bin/startup.sh"
        mapper {
          fileMode = "755"
        }
      }
      link {
        path = "/etc/packagename/"
        name = "var/lib/packagename/conf"
      }
    }

    task.buildPackage()

    assert outputFile.exists()

    def lintianProcess = "lintian -c -q ${outputFile.absolutePath}".execute()
    assert lintianProcess.waitFor() == 0
    println "${lintianProcess.in.text}"
  }
}
