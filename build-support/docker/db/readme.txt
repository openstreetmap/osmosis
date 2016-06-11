This directory contains the scripts to create a docker-based database server to be used for unit testing.
In order to use this server, docker must be installed on the local workstation.  Beyond that, no additional
configuration should be required.

To build the docker image, run the following script.
    ./build.sh

To run the docker image, run the following command.  To stop the server, press Ctrl-C.
    docker run -ti --rm=true --name osmosis-build -p 5432:5432 openstreetmap.org/osmosis-db

If you wish to troubleshoot a running server, you may run the following command to get a bash prompt
inside the docker container.
    docker exec -ti osmosis-build /bin/bash
