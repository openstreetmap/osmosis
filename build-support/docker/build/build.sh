#!/usr/bin/env bash

set -euo pipefail

scriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "${scriptDir}"

# Build the docker image.
docker build -t="openstreetmap.org/osmosis-build-${userId}" --build-arg CURRENT_USER_ID=${userId} --build-arg CURRENT_GROUP_ID=${groupId} .
