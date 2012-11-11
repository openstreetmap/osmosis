Osmosis is a command line Java application for processing Open Street Map
(http://www.openstreetmap.org) data.

The tool consists of a series of pluggable components that can be chained
together to perform a larger operation. For example, it has components for
reading from database and from file, components for writing to database and to
file, components for deriving and applying change sets to data sources,
components for sorting data, etc. It has been written so that it is easy to add
new features without re-writing common tasks such as file or database handling.

Some brief build, running and installation notes are provided below, however
most documentation may be found on the project wiki page.
http://wiki.openstreetmap.org/wiki/Osmosis

**** BUILD ****
Osmosis is built using the Gradle (http://gradle.org) built tool, however
Gradle does not need to be installed.  The only requirements are a 1.6 JDK, and
an Internet connection.

Below are several commands useful to build the software.  All commands must be
run from the root of the source tree.
 
Build the software without running unit tests:
./gradlew assemble

Perform a complete build including unit tests:
./gradlew build

Clean the build tree:
./gradlew clean

Verify checkstyle compliance:
./gradlew checkstyleMain checkstyleTest

**** RUNNING ****
After completing the build process, a working Osmosis installation is contained
in the package sub-directory.  The Osmosis launcher scripts reside in the bin
sub-directory of package.  On a UNIX-like environment use the "osmosis" script,
on a Windows environment use the "osmosis.bat" script.

However, for installing the software it is recommended to use a distribution
archive described below.

**** INSTALLATION ****
After completing the build process, distribution archives in zip and tar gzipped
formats are contained in the package/build/distribution directory.  These
archives may be extracted to a location of your choice.  The bin sub-directory
should either be added to your PATH, or in the case of UNIX-like environments
the "osmosis" script may be symlinked into an existing directory already on the
PATH.
