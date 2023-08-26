#!/usr/bin/env bash

set -euo pipefail

scriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "${scriptDir}"
. ./env.sh

# Create gradle directory in user home if it doesn't already exist.
if [ ! -d "${gradleUserDir}" ]; then
    mkdir "${gradleUserDir}"
fi

# Launch our docker build container interactively and destroy on exit.
runCommand="docker-compose run --rm build"
if [ $# -eq 0 ]; then
    ${runCommand} /bin/bash
else
    ${runCommand} "${@}"
fi

# Remove remaining containers (e.g. db server)
docker-compose down -v
