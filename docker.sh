#!/usr/bin/env bash

set -e

scriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "${scriptDir}"

# Build docker images.
./build-support/docker/build.sh

# Run our interactive gradle enabled build environment.
./build-support/docker/run.sh "${@}"
