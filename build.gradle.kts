import java.text.SimpleDateFormat
import java.util.*

plugins {
  id("groovy")
  id("java-gradle-plugin")
  id("maven-publish")
  id("signing")
  id("com.github.ben-manes.versions") version "0.38.0"
  id("net.ossindex.audit") version "0.4.11"
  id("com.gradle.plugin-publish") version "0.14.0"
  id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
  // TODO Validation fails for the java-gradle-plugin "PluginMaven" publication
  // Validation is disabled in the ci/cd workflows (`-x validatePomFileForPluginMavenPublication`)
  id("io.freefair.maven-central.validate-poms") version "5.3.0"
}

val dependencyVersions = listOf(
  "com.google.guava:guava:20.0",
  "commons-io:commons-io:2.6",
  "org.codehaus.plexus:plexus-component-annotations:1.7.1",
  "org.codehaus.plexus:plexus-utils:3.2.0"
)

repositories {
  mavenCentral()
}

configurations.all {
  resolutionStrategy {
    failOnVersionConflict()
    force(dependencyVersions)
  }
}

dependencies {
  api(gradleApi())
  api(localGroovy())

  api("org.vafer:jdeb:1.6")
  implementation("commons-lang:commons-lang:2.6")

  testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
  testImplementation("cglib:cglib-nodep:3.3.0")

  // see https://docs.gradle.org/current/userguide/test_kit.html
  //testImplementation(gradleTestKit())
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
  withType(Test::class.java) {
    useJUnit()
  }
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("javadoc")
  from(tasks.javadoc)
}

val sourcesJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
}

artifacts {
  add("archives", sourcesJar.get())
  add("archives", javadocJar.get())
}

fun findProperty(s: String) = project.findProperty(s) as String?

val isSnapshot = project.version == "unspecified"
val artifactVersion = if (!isSnapshot) project.version as String else SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date())!!
val publicationName = "gradleDebianPlugin"
publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/${property("github.package-registry.owner")}/${property("github.package-registry.repository")}")
      credentials {
        username = System.getenv("GITHUB_ACTOR") ?: findProperty("github.package-registry.username")
        password = System.getenv("GITHUB_TOKEN") ?: findProperty("github.package-registry.password")
      }
    }
  }
  publications {
    register(publicationName, MavenPublication::class) {
      pom {
        name.set("gradle-debian-plugin")
        description.set("A Debian plugin for Gradle")
        url.set("https://github.com/gesellix/gradle-debian-plugin")
        licenses {
          license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("gesellix")
            name.set("Tobias Gesellchen")
            email.set("tobias@gesellix.de")
          }
        }
        scm {
          connection.set("scm:git:github.com/gesellix/gradle-debian-plugin.git")
          developerConnection.set("scm:git:ssh://github.com/gesellix/gradle-debian-plugin.git")
          url.set("https://github.com/gesellix/gradle-debian-plugin")
        }
      }
      artifactId = "gradle-debian-plugin"
      version = artifactVersion
      from(components["java"])
      artifact(sourcesJar.get())
      artifact(javadocJar.get())
    }
  }
}
nexusPublishing {
  repositories {
    if (!isSnapshot) {
      sonatype {
        // 'sonatype' is pre-configured for Sonatype Nexus (OSSRH) which is used for The Central Repository
        stagingProfileId.set(System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: findProperty("sonatype.staging.profile.id")) //can reduce execution time by even 10 seconds
        username.set(System.getenv("SONATYPE_USERNAME") ?: findProperty("sonatype.username"))
        password.set(System.getenv("SONATYPE_PASSWORD") ?: findProperty("sonatype.password"))
      }
    }
  }
}

signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications[publicationName])
}

pluginBundle {
  website = "https://github.com/gesellix/gradle-debian-plugin"
  vcsUrl = "https://github.com/gesellix/gradle-debian-plugin.git"
  description = "Create Debian packages with Gradle"
  tags = listOf("debian", "gradle", "plugin", "jdeb")

  plugins {
    register(publicationName) {
      id = "de.gesellix.debian"
      displayName = "Gradle Debian plugin"
      version = artifactVersion
    }
  }

  mavenCoordinates {
    groupId = "de.gesellix"
    artifactId = "gradle-debian-plugin"
    version = artifactVersion
  }
}
