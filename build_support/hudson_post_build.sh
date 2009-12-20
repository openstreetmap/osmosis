#! /bin/sh

TARGET=/osm/osmosis-continuous-integration/web

if [ -d ${WORKSPACE}/target/site ]
then

	if [ -d ${TARGET}/${JOB_NAME} ]
	then
		rm -rf ${TARGET}/${JOB_NAME}
	fi

	cp -ar ${WORKSPACE}/target/site ${TARGET}/${JOB_NAME}

fi
