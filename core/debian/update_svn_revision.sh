#!/bin/bash
# replace the revision number in debian/changelog to the last time this 
# Subtree was modified

svnrevision=`svn info . | grep "Last Changed Rev" | sed 's/Last Changed Rev: //'`
svnrevision=${svnrevision/M/}

if [ -n "${svnrevision}" ] ; then
    perl -p -i -e "s/\(\S+\)/\(${svnrevision}\)/;" debian/changelog
fi

