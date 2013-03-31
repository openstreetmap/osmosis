This is a customised version of Scott Crosby's PBF library.  The original
version is here:
https://github.com/scrosby/OSM-binary

The original library is not available on Maven Central, but Osmosis has a
dependency on it.  Therefore the original library has been modified to be built
as part of Osmosis and the code is renamed to live under an Osmosis package
name.  This re-packaging avoids any conflicts if other versions of the library
exist on a target classpath.

This codebase is maintained at the following location on the osmosis branch:
https://github.com/brettch/OSM-binary

The osmosis branch contains a number of customisations including:
* Change to use an Osmosis package name.
* Remove all C code, and any other unnecessary files.
* Add Osmosis compatible Gradle build script.

The Osmosis repository contains a copy of this codebase, but it is not the
master location.  It is manually updated to remain in sync with the above
location whenever necessary.  It would have been possible to do a true merge
into the Osmosis repository, but this would have resulted in full history
of both repositories being included which may have been confusing.
