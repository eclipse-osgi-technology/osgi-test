# OSGi Testing Support

Testing support for OSGi

## Purpose

This project provides a set of bundles which contain useful and helpful classes for testing of OSGi API.

### [org.osgi.test.common][1]

This artifact includes common utility classes which are useful in all testing scenarios.

### org.osgi.test.assertj.*

These artifacts provides support classes for OSGi testing with [AssertJ](https://github.com/joel-costigliola/assertj-core) including custom assertions. Currently there are artifacts for [org.osgi.framework][2], [org.osgi.util.promise][3], and [org.osgi.service.log][7].

### [org.osgi.test.junit4][4]

This artifact provides support classes for OSGi testing with [JUnit 4](https://junit.org/junit4/) including JUnit 4 Rules.

### [org.osgi.test.junit5][5]

This artifact provides support classes for OSGi testing with [JUnit 5](https://junit.org/junit5/) including JUnit 5 Extensions.

### [org.osgi.test.junit5.cm][6]

This artifact provides support classes for OSGi testing with ConfigurationAdmin and [JUnit 5](https://junit.org/junit5/) including JUnit 5 Extensions.

### [org.osgi.test.junit5.listeners.log.osgi][8]

This artifact provides a [JUnit 5](https://junit.org/junit5/) `TestExecutionListener` that logs test output via the OSGi Log Service.

## Examples

### [Maven Examples][9]

A set of example projects demonstrating how to set up OSGi testing with Maven, including a [comprehensive demo][10] showcasing all osgi-test features.

## Building

We use Maven to build and the repo includes `mvnw`.
You can use your system `mvn` but we require a recent version.

- `./mvnw clean install` - Assembles and tests the project

[![Build Status](https://github.com/osgi/osgi-test/workflows/CI%20Build/badge.svg)](https://github.com/osgi/osgi-test/actions?query=workflow%3A%22CI%20Build%22)

## Code Quality

We use [CodeQL](https://github.com/osgi/osgi-test/blob/main/.github/workflows/codeql-analysis.yml) for continuous security analysis.

## Repository

Release versions of osgi-test bundles are available on Maven Central under the `org.osgi` group ID. You can find them [here](https://search.maven.org/search?q=a:org.osgi.test.*).

For those who want the bleeding edge, snapshot artifacts are published to the Sonatype OSS repository:

[https://oss.sonatype.org/content/repositories/snapshots/](https://oss.sonatype.org/content/repositories/snapshots/)

## Future work

See the [open issues](https://github.com/osgi/osgi-test/issues) for the list of outstanding TODOs.

## License

This program and the accompanying materials are made available under the terms of the Apache License, Version 2.0 which is available at <https://www.apache.org/licenses/LICENSE-2.0>.

## Contributing

Want to hack? There are [instructions](CONTRIBUTING.md) to get you
started.

They are probably not perfect, please let us know if anything feels
wrong or incomplete.

## Acknowledgments

This project uses the [Bnd Maven Plugins](https://github.com/bndtools/bnd) to build.

[1]: org.osgi.test.common/README.md
[2]: org.osgi.test.assertj.framework/README.md
[3]: org.osgi.test.assertj.promise/README.md
[4]: org.osgi.test.junit4/README.md
[5]: org.osgi.test.junit5/README.md
[6]: org.osgi.test.junit5.cm/README.md
[7]: org.osgi.test.assertj.log/README.md
[8]: org.osgi.test.junit5.listeners.log.osgi/README.md
[9]: examples/osgi-test-example-mvn
[10]: examples/osgi-test-example-mvn/org.osgi.test.example.demo

