#!/usr/bin/env sh
set -e

# Script used to build MM from source

# Dependencies (ubuntu / debian)
# sudo apt-get install subversion build-essential autoconf automake libtool pkg-config libboost1.54-all-dev zlib1g-dev swig openjdk-7-jdk ant python-dev python-numpy-dev

# The directory which contains this file should have
# - an ImageJ/ folder with a valid ij49.zip installation)
# - 3rdpartypublic : svn co https://valelab.ucsf.edu/svn/3rdpartypublic
# - micromanager/ folder : git clone https://github.com/hadim/micromanager.git

CURR_DIR=$(pwd)

cd micromanager/
git pull
GIT_HASH=$(git rev-parse --short HEAD)

# Build MM
./autogen.sh
./configure --enable-imagej-plugin=$CURR_DIR/ImageJ JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/
make fetchdeps
make
rm -fr $CURR_DIR/ImageJ
make install

cd ../

# Generate zip bundle
mkdir -p bundles/
BUNDLE_NAME="$(date +"%Y.%m.%d").MicroManager-$GIT_HASH.zip"
zip -r bundles/$BUNDLE_NAME ImageJ/
