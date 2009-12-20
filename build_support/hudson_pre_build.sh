#! /bin/sh -ex

mkdir ${WORKSPACE}/trunk/changes

${WORKSPACE}/trunk/build_support/create_changes_xml.pl < ${WORKSPACE}/trunk/changes.txt > ${WORKSPACE}/trunk/changes/changes.xml
