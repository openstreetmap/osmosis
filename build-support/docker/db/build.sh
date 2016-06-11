#!/bin/sh

# Build the docker image.
# The use of tar is a workaround for the docker restriction of not following symlinks.  This
# tar command dereferences symlinks and then passes the resultant archive to the docker build. 
tar -czh . | docker build -t="openstreetmap.org/osmosis-db" -
