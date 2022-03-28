# Osmosis
![Build Status](https://github.com/openstreetmap/osmosis/actions/workflows/continous-integration.yml/badge.svg)

## Overview

Osmosis is a command line Java application for processing
[Open Street Map](http://www.openstreetmap.org) data.

The tool consists of a series of pluggable components that can be chained
together to perform a larger operation. For example, it has components for
reading from database and from file, components for writing to database and to
file, components for deriving and applying change sets to data sources,
components for sorting data, etc. It has been written so that it is easy to add
new features without re-writing common tasks such as file or database handling.

The main point of entry for documentation is
[the project wiki page](http://wiki.openstreetmap.org/wiki/Osmosis), although
some information is included below.

## Status

Osmosis is in light-maintenance mode.
[As of 2018 we’ve stopped active development](https://lists.openstreetmap.org/pipermail/osmosis-dev/2018-October/001847.html)
and transitioned to periodic acceptance of pull requests with tests and minor version releases.
Keep an eye on [osmosis-dev list](https://lists.openstreetmap.org/listinfo/osmosis-dev)
for any updates.

## Usage

* [The project wiki page](http://wiki.openstreetmap.org/wiki/Osmosis) is the best
place to begin for new users.
* [Detailed Usage](./doc/detailed-usage.adoc) is the main reference for experienced users.

## Installation

It is recommended to use a pre-built distribution archive rather than compile
from source.  The location of the [latest builds are specified on the project
wiki](https://wiki.openstreetmap.org/wiki/Osmosis#Latest_stable_version).
These archives may be extracted to a location of your choice.  The bin
sub-directory should either be added to your `PATH`, or in the case of UNIX-like
environments the "osmosis" script may be symlinked into an existing directory
already on the `PATH`.

## Development

See [Development](./doc/development.md) for details.

## Issue Tracking

See https://github.com/openstreetmap/osmosis/issues
