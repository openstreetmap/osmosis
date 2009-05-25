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
BLUE="${ESC}[94m"
NORMAL="${ESC}[0m"

echo "copying Files to '$dst_path'"

bin_path="$dst_path/usr/bin"
lib_path="$dst_path/usr/lib"
share_path="$dst_path/usr/share/openstreetmap"
man1_path="$dst_path/usr/man/man1"
mkdir -p "$bin_path"
mkdir -p "$lib_path"
mkdir -p "$share_path"
mkdir -p "$man1_path"


# #######################################################
# Osmosis
# #######################################################
echo "${BLUE}----------> Osmosis${NORMAL}"

# Strange is that it doesn't work with all java compilers ..
if ! javac -version 2>&1 | grep -q -e 1.6 ; then
    javac -version
    echo "!!!!! ERROR: Wrong JavaC Version. Need 1.6*"
    exit -1 
fi

ant clean >build.log 2>&1
if [ "$?" -ne "0" ] ; then
    cat build.log
    echo "${RED}!!!!!! ERROR compiling  Osmosis (ant clean) ${NORMAL}"
    exit -1
fi

ant resolve >>build.log 2>&1
if [ "$?" -ne "0" ] ; then
    cat build.log
    echo "${RED}!!!!!! ERROR compiling  Osmosis (ant resolve) ${NORMAL}"
    exit -1
fi

ant dist >>build.log 2>&1
if [ "$?" -ne "0" ] ; then
    cat build.log
    echo "${RED}!!!!!! ERROR compiling  Osmosis (ant build) ${NORMAL}"
    exit -1
fi

osmosis_dir="$dst_path/usr/local/share/osmosis/"
mkdir -p $osmosis_dir
cp build/binary/osmosis.jar $osmosis_dir
if [ "$?" -ne "0" ] ; then
    find . -name "*osmosis*.jar"
    echo "${RED}!!!!!! ERROR cannot find resulting osmosis.jar ${NORMAL}"
    exit -1
fi

# copy needed libs
mkdir -p $osmosis_dir/lib/
cp lib/default/*.jar $osmosis_dir/lib/
if [ "$?" -ne "0" ] ; then
    echo "${RED}!!!!!! ERROR cannot copy needed libs for osmosis${NORMAL}"
    exit -1
fi

# Osmosis script
src_fn="bin/osmosis"
man1_fn="$man1_path/osmosis.1"
if grep -q -e "--help" "$src_fn"; then
    echo "Create Man Page from Help '$man1_fn'"
    $src_fn --help >"$man1_fn"
else
    echo "!!!! No idea how to create Man Page for $src_fn"
fi

mkdir -p $osmosis_dir/bin
cp $src_fn "$osmosis_dir/bin/osmosis"
if [ "$?" -ne "0" ] ; then
    echo "${RED}!!!!!! ERROR cannot find resulting osmosis script ${NORMAL}"
    exit -1
fi

cd $dst_path
mkdir -p usr/bin
ln -sf usr/local/share/osmosis/bin/osmosis usr/bin/osmosis
