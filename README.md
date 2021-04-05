# Gradle plugin to create Debian packages

[![Publish](https://github.com/gesellix/gradle-debian-plugin/actions/workflows/cd.yml/badge.svg)](https://github.com/gesellix/gradle-debian-plugin/actions/workflows/cd.yml)
[Latest version](https://plugins.gradle.org/plugin/de.gesellix.debian)

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

## Building on your own

This should be as easy as doing something like follows,
 provided that you have `git` installed, and you have found a nice parent directory on your machine:

```
git clone https://github.com/gesellix/gradle-debian-plugin.git
cd gradle-debian-plugin
./gradlew build publishToMavenLocal
```

Please create pull requests for missing features or when finding bugs!

## Usage

Your Gradle build needs to be configured as shown below. Some properties in `debian{ ... }` are optional,
 but I still need to finish tests and other aspects before writing a complete documentation.

Looking at the tests in the source repository you can find similar examples and you'll see
 that I had the goal to package a Tomcat compatible webapp. Apart from that, it's really up to you what
 is being included in your .deb package, you just need to configure the right file paths and task dependencies.

```groovy
plugins {
  id "de.gesellix.debian" version "2021-04-05T21-19-00"
}

publishing {
  publications {
    webapp(MavenPublication) {
      from components.web  // .war
    }
  }
}

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
      mapper {
        filename = { path -> "opt/" + path }
      }
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
 My preference will be to not break any interface.

## Release Workflow

There are multiple GitHub Action Workflows for the different steps in the package's lifecycle:

- CI: Builds and checks incoming changes on a pull request
  - triggered on every push to a non-default branch
- CD: Publishes the Gradle artifacts to GitHub Package Registry
  - triggered only on pushes to the default branch
- Release: Publishes Gradle artifacts to Sonatype and releases them to Maven Central
  - triggered on a published GitHub release using the underlying tag as artifact version, e.g. via `git tag -m "$MESSAGE" v$(date +"%Y-%m-%dT%H-%M-%S")`

## License

MIT License

Copyright 2015-2021 [Tobias Gesellchen](https://www.gesellix.net/) ([@gesellix](https://twitter.com/gesellix))

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
