# Development

The easiest way to perform a full Osmosis build is to use the docker-based
development environment.  If you have docker and docker-compose installed,
simply run the following command to build and launch a shell with everything
required to run the full build and test suite.

    ./docker.sh

Osmosis is built using the [Gradle build tool](http://gradle.org).  Gradle itself
does not need to be installed because the `gradlew` script will install Gradle on
first usage.  The only requirements are a 1.17 JDK, and an Internet connection.
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
`osmosis/build/distributions` directory.
