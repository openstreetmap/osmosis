#!/bin/sh

find . -iname "*.java" -exec dos2unix -U '{}' \;
find . -iname "*.xml" -exec dos2unix -U '{}' \;
find . -iname "*.txt" -exec dos2unix -U '{}' \;

find . -iname "*.java" -exec svn propset svn:eol-style native '{}' \;
find . -iname "*.xml" -exec svn propset svn:eol-style native '{}' \;
find . -iname "*.txt" -exec svn propset svn:eol-style native '{}' \;

