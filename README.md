# Gradle plugin to create Debian packages

[![Build Status](https://travis-ci.org/gesellix/gradle-debian-plugin.png)](https://travis-ci.org/gesellix/gradle-debian-plugin)
[![Coverage Status](https://coveralls.io/repos/gesellix/gradle-debian-plugin/badge.png)](https://coveralls.io/r/gesellix/gradle-debian-plugin)

## About

You can package your files or publications
 (currently only [MavenPublications](http://www.gradle.org/docs/current/userguide/publishing_maven.html) are supported)
 in Debian compatible packages with the help of this [Gradle](http://www.gradle.org/) plugin.
 Under the hood the very convenient [jdeb](https://github.com/tcurdt/jdeb) library is used, and this plugin only represents
 some kind of bridge between your Gradle build and jdeb.

There already are other plugins with the same goal, but after trying two or three of the most promising
 implementations, I came to the conclusion that I didn't want too much setup like [fpm](https://github.com/jordansissel/fpm)
 or too much hazzle with quite detailed build configurations.
 Some plugins also tried to use the same build config to create `.deb` and `.rpm` packages.

Since I don't need RPM packages (at least for now) and
 because everyone should do something new (creating a Gradle plugin) from time to time,
  there it is: Yet another Gradle Debian package plugin!

### You should know what you're doing[!](http://en.wikipedia.org/wiki/Here_be_dragons)

The plugin won't check your `.deb` package configuration for any problems, so it's up to you to use such great tools like `lintian`.
 The [Debian New Maintainers' Guide](http://www.debian.org/doc/manuals/maint-guide/index.en.html) was a good manual for
 me to understand the Debian package concepts. You should at least read chapters 4. and 5. to get some basic knowledge about
 which files and folder structures are required.

Packaging badly tested scripts might damage your and other peoples' system.


Ok, let's have some fun!

## Download

The plugin is available at [Bintray](https://bintray.com/) for [Maven compatible](http://dl.bintray.com/gesellix/gradle-plugins) repositories
 or as manual [download](https://bintray.com/gesellix/gradle-plugins/gradle-debian-plugin).

## Building on your own

This should be as easy as doing something like follows,
 provided that you have `git` installed and you have found a nice parent directory on your machine:

```
git clone https://github.com/gesellix/gradle-debian-plugin.git
cd gradle-debian-plugin
./gradlew build install
```

## Usage

Your Gradle build needs to be configured as shown below. Some properties in `debian{ ... }` are optional,
 but I still need to finish tests and other aspects before writing a complete documentation.

Looking at the tests in the source repository you can find similar examples and you'll see
 that I had the goal to package a Tomcat compatible webapp. Apart from that, it's really up to you what
 is being included in your .deb package, you just need to configure the right file paths and task dependencies.

```
buildscript {
  repositories {
    maven {
      url "http://dl.bintray.com/gesellix/gradle-plugin"
    }
    mavenCentral()
    mavenLocal()
  }
  dependencies {
    classpath "de.gesellix:gradle-debian-plugin:10"
  }
}

publishing {
  publications {
    webapp(MavenPublication) {
      from components.web  // .war
    }
  }
}

apply plugin: 'pkg-debian'

debian {
  packagename = "packagename"
  publications = ['webapp']
  controlDirectory = "/path/to/control"
  changelogFile = "/path/to/changelog"

  data {
    def baseDir = "/path/to/data"
    dir {
      name = baseDir
      exclusions = [
          "etc/init.d/packagename"]
    }
    file {
      name = "${baseDir}/etc/init.d/packagename"
      target = "etc/init.d/packagename"
      mapper {
        fileMode = "755"
      }
    }
    link {
      path = "/etc/packagename/"
      name = "var/lib/packagename/conf"
    }
  }
}
```

## Versioning

I currently don't support [Semantic Versioning](http://semver.org/) or similar concepts,
 just because I don't expect too many breaking changes to come.
 My preference will be to not break any interface. So, I will just count the version numbers up, one after the other.
 Yep, stupid me... but who knows, I could probably make up my mind in the future.

## License

Currently licensed under Apache License, Version 2.0. See the LICENSE file in the source repository root for details.

I would be glad if you try to create pull requests for missing features or when finding bugs!
