#!/bin/bash
# This script replaces a make ; make install for creation of the debian package.
# Maybe you can also use it to install the stuff on your system.
# If you are successfull, please write how to do this here
# PS.: Any improvements/additions to this installer are welcome.

dst_path=$1

if [ ! -n "$dst_path" ] ; then
    echo "Please specify a Directory to use as Basedirectory"
    echo "Usage:"
    echo "     $0 <working-dir>"
    exit -1 
fi

# define Colors
ESC=`echo -e "\033"`
RED="${ESC}[91m"
GREEN="${ESC}[92m"
YELLOW="${ESC}[93m"
BLUE="${ESC}[94m"
MAGENTA="${ESC}[95m"
CYAN="${ESC}[96m"
WHITE="${ESC}[97m"
BG_RED="${ESC}[41m"
BG_GREEN="${ESC}[42m"
BG_YELLOW="${ESC}[43m"
BG_BLUE="${ESC}[44m"
BG_MAGENTA="${ESC}[45m"
BG_CYAN="${ESC}[46m"
BG_WHITE="${ESC}[47m"
BRIGHT="${ESC}[01m"
UNDERLINE="${ESC}[04m"
BLINK="${ESC}[05m"
REVERSE="${ESC}[07m"
NORMAL="${ESC}[0m"

echo "copying Files to '$dst_path'"
package_name=openstreetmap
dst_path=${dst_path%/}
platform=`uname -m`

perl_path="$dst_path/usr/share/perl5"
bin_path="$dst_path/usr/bin"
lib_path="$dst_path/usr/lib"
share_path="$dst_path/usr/share/$package_name"
man1_path="$dst_path/usr/man/man1"
mkdir -p "$perl_path"
mkdir -p "$bin_path"
mkdir -p "$lib_path"
mkdir -p "$share_path"
mkdir -p "$man1_path"


# #######################################################
# Osmosis
# #######################################################
echo "${BLUE}----------> applications/utils/osmosis/trunk Osmosis${NORMAL}"


#ant clean >build.log 2>build.err
#ant dist >>build.log 2>>build.err
ant clean
ant dist 
if [ "$?" -ne "0" ] ; then
    echo "${RED}!!!!!! ERROR compiling  Osmosis ${NORMAL}"
    exit -1
fi
cd ../..

osmosis_dir="$dst_path/usr/local/share/osmosis/"
mkdir -p $osmosis_dir
cp osmosis/trunk/build/binary/osmosis.jar $osmosis_dir
if [ "$?" -ne "0" ] ; then
    echo "${RED}!!!!!! ERROR cannot find resulting osmosis.jar ${NORMAL}"
    exit -1
fi

    # copy needed libs
mkdir -p $osmosis_dir/lib/
cp osmosis/trunk/lib/default/*.jar $osmosis_dir/lib/
if [ "$?" -ne "0" ] ; then
    echo "${RED}!!!!!! ERROR cannot copy needed libs for osmosis${NORMAL}"
    exit -1
fi

    # Osmosis script
src_fn="osmosis/trunk/bin/osmosis"
man1_fn="$man1_path/osmosis.1"
if grep -q -e "--help" "$src_fn"; then
    echo "Create Man Page from Help '$man1_fn'"
    perl $src_fn --help >"$man1_fn"
else
    echo "!!!! No idea how to create Man Page for $src_fn"
fi
mkdir -p $osmosis_dir/bin
cp $src_fn "$osmosis_dir/bin/osmosis"
if [ "$?" -ne "0" ] ; then
    echo "${RED}!!!!!! ERROR cannot find resulting osmosis script ${NORMAL}"
    exit -1
fi

ln -sf /usr/local/share/osmosis/bin/osmosis $bin_path/osmosis
