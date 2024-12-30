#!/bin/sh

SOURCE_DIR="$(dirname "$0")"
SOURCE_FILE="${SOURCE_DIR}/src/main/resources/org/openstreetmap/osmosis/core/plugin/plugin.xml"
TEMPLATE_FILE="${SOURCE_FILE}.template"

if [ -z "${VERSION}" ]; then
    echo "Error: VERSION not set in environment"
    exit 1
fi

if [ ! -r "${TEMPLATE_FILE}" ]; then
    echo "Error: Cannot read file: ${TEMPLATE_FILE}"
    exit 1
fi

sed "s/no-version-specified/${VERSION}/g" "${TEMPLATE_FILE}" > "${SOURCE_FILE}"
