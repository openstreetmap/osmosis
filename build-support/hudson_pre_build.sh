#! /bin/sh -ex

mkdir ${WORKSPACE}/trunk/src/changes

${WORKSPACE}/trunk/build_support/create_changes_xml.pl < ${WORKSPACE}/trunk/changes.txt > ${WORKSPACE}/trunk/src/changes/changes.xml
