#!/usr/bin/env bash

set -euo pipefail

scriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "${scriptDir}"

# Build docker images.
./build-support/docker/build.sh

# Run our interactive gradle enabled build environment.
set +u # Disable unbound variable check so we don't fail on run command if no args provided
./build-support/docker/run.sh "${@}"
