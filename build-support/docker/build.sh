#!/usr/bin/env bash

set -euo pipefail

scriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "${scriptDir}"
. ./env.sh

# Build the docker image used for the interactive gradle enabled build environment.
./build/build.sh

# Build the docker image for the database server used for running database task integration tests.
./db/build.sh
