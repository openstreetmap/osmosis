#!/bin/sh

# This script automates the replication of changes into an offline osm file for a specific area of interest.  This allows an up-to-date local snapshot of an area to be maintained.

# The name of the replicated file.
OSM_FILE=myfile.osm.gz

# The name of the temp file to create during processing (Note: must have the same extension as OSM_FILE to ensure the same compression method is used.
TEMP_OSM_FILE=tmp.osm.gz

# The directory containing the state associated with the --read-change-interval task previously initiated with the --read-change-interval-init task.
WORKING_DIRECTORY=./

# The bounding box to maintain.
LEFT=-180
BOTTOM=-90
RIGHT=180
TOP=90

# The osmosis command.
CMD="osmosis -q"

# Launch the osmosis process.
$CMD --read-change-interval $WORKING_DIRECTORY --read-xml $OSM_FILE --apply-change --bounding-box left=$LEFT bottom=$BOTTOM right=$RIGHT top=$TOP --write-xml $TEMP_OSM_FILE

# Verify that osmosis ran successfully.
if [ "$?" -ne "0" ]; then
	
	echo "Osmosis failed, aborting."
	exit -1
	
fi

mv $TEMP_OSM_FILE $OSM_FILE

