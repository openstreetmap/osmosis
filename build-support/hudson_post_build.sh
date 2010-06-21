#! /bin/sh -ex

export TARGET=/osm/osmosis-continuous-integration/web

# remove '-maven' from the name
JOB_NAME=$(echo ${JOB_NAME} | sed -e 's|-maven||')

if [ -d ${WORKSPACE}/trunk/target/site ]
then

	if [ -d ${TARGET}/${JOB_NAME} ]
	then
		rm -rf ${TARGET}/${JOB_NAME}
	fi

	cp -ar ${WORKSPACE}/trunk/target/site ${TARGET}/${JOB_NAME}

fi

# Clean up residual data
/bin/rm /tmp/test*.osc
/bin/rm /tmp/test*.osm
/bin/rm /tmp/test*.osm.gz
