#!/usr/bin/env sh
set -e

# Script used to build MM from source

# Dependencies (ubuntu / debian)
# sudo apt-get install subversion build-essential autoconf automake libtool pkg-config libboost1.54-all-dev zlib1g-dev swig openjdk-7-jdk ant python-dev python-numpy-dev

IJ_ZIP="http://rsb.info.nih.gov/ij/download/zips/ij149.zip"

CURR_DIR=$(pwd)

if [ ! -d "ImageJ" ]; then
  wget $IJ_ZIP
  unzip ij*.zip
  wget https://raw.githubusercontent.com/micro-manager/micro-manager/master/bindist/any-platform/MMConfig_demo.cfg -O ImageJ/MMConfig_demo.cfg
fi

cd micro-manager/
git pull
GIT_HASH=$(git rev-parse --short HEAD)

# Build MM
./autogen.sh
./configure --enable-imagej-plugin=$CURR_DIR/ImageJ JAVA_HOME=/usr/lib/jvm/default-java/
make fetchdeps
make

make install

cd ../

# Generate zip bundle
mkdir -p bundles/
BUNDLE_NAME="$(date +"%Y.%m.%d").MicroManager-$GIT_HASH.zip"
zip -r bundles/$BUNDLE_NAME ImageJ/
