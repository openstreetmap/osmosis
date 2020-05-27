# Osmosis
[![Build Status](https://travis-ci.org/openstreetmap/osmosis.svg?branch=master)](https://travis-ci.org/openstreetmap/osmosis)

## Overview

Osmosis is a command line Java application for processing
[Open Street Map](http://www.openstreetmap.org) data.

The tool consists of a series of pluggable components that can be chained
together to perform a larger operation. For example, it has components for
reading from database and from file, components for writing to database and to
file, components for deriving and applying change sets to data sources,
components for sorting data, etc. It has been written so that it is easy to add
new features without re-writing common tasks such as file or database handling.

Some brief build, running and installation notes are provided below, however
most documentation may be found on
[the project wiki page](http://wiki.openstreetmap.org/wiki/Osmosis).

## Status

Osmosis is in light-maintenance mode.
[As of 2018 we’ve stopped active development](https://lists.openstreetmap.org/pipermail/osmosis-dev/2018-October/001847.html)
and transitioned to periodic acceptance of pull requests with tests and minor version releases.
Keep an eye on [osmosis-dev list](https://lists.openstreetmap.org/listinfo/osmosis-dev)
for any updates.

## Usage

* [Detailed Usage](./doc/usage.adoc) - Full details of all tasks and their options.

## Installation

It is recommended to use a pre-built distribution archive rather than compile
from source.  The location of the [latest builds are specified on the project
wiki](https://wiki.openstreetmap.org/wiki/Osmosis#Latest_stable_version).
These archives may be extracted to a location of your choice.  The bin
sub-directory should either be added to your `PATH`, or in the case of UNIX-like
environments the "osmosis" script may be symlinked into an existing directory
already on the `PATH`.

## Development

The easiest way to perform a full Osmosis build is to use the docker-based
development environment.  If you have docker and docker-compose installed,
simply run the following command to build and launch a shell with everything
required to run the full build and test suite.

    ./docker.sh

Osmosis is built using the [Gradle build tool](http://gradle.org).  Gradle itself
does not need to be installed because the `gradlew` script will install Gradle on
first usage.  The only requirements are a 1.7 JDK, and an Internet connection.
Note that in the docker environment all downloads will still occur and be cached
in your home directory.

Below are several commands useful to build the software.  All commands must be
run from the root of the source tree.

Perform a complete build including unit tests:

    ./docker.sh ./gradlew build

Build the software without running unit tests:

    ./docker.sh ./gradlew assemble

Clean the build tree:
    
    ./docker.sh ./gradlew clean

Generate project files to allow the project to be imported into IntelliJ.

    ./docker.sh ./gradlew idea

Generate project files to allow the project to be imported into Eclipse.

    ./docker.sh ./gradlew eclipse

Verify checkstyle compliance:
    
    ./docker.sh ./gradlew checkstyleMain checkstyleTest

After completing the build process, a working Osmosis installation is contained
in the `package` sub-directory.  The Osmosis launcher scripts reside in the `bin`
sub-directory of package.  On a UNIX-like environment use the "osmosis" script,
on a Windows environment use the "osmosis.bat" script.

Distribution archives in zip and tar gzipped formats are contained in the
`package/build/distribution` directory.

## Issue Tracking

See https://trac.openstreetmap.org/query?status=!closed&component=osmosis
