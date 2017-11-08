#!/usr/bin/env bash

set -e

if [ -z "$1" ]
then
    echo "Usage : ./install.sh /path/to/mm/folder"
    exit 1
fi

IMAGEJ_ROOT=$1
MM_ANCHOR="$IMAGEJ_ROOT/plugins/Micro-Manager/MMJ_.jar"
FIJI_ANCHOR="$IMAGEJ_ROOT/jars/fiji-2.0.0-SNAPSHOT.jar"

if [ ! -f "$MM_ANCHOR" ] && [ ! -f "$FIJI_ANCHOR" ]
then
    echo "Does $IMAGEJ_ROOT really contain Micro-Manager/Fiji ?"
    exit 1
fi

# Install MAARS plugin
if [ -f "$MM_ANCHOR" ]; then
  bash copyMMDeps.sh
  rm -f $IMAGEJ_ROOT/mmplugins/maars*.jar
  cp jars/maars*.jar $IMAGEJ_ROOT/mmplugins/
fi

if [ -f "$FIJI_ANCHOR" ]; then
  bash copyFijiDeps.sh
  rm -f $IMAGEJ_ROOT/jars/maars*.jar
  cp jars/maars*.jar $IMAGEJ_ROOT/jars/
fi

# Install dependencies
rm -fr $IMAGEJ_ROOT/plugins/MAARS_deps
mkdir -p $IMAGEJ_ROOT/plugins/MAARS_deps


function join_by { local IFS="$1"; shift; echo "$*"; }

for jarPath in jars/MAARS_deps/*
do
  jarPathAry=(${jarPath//// })
  jarName=(${jarPathAry[2]//-/ })
  nameLen=${#jarName[@]}
  artifactNameary=${jarName[*]:0:$nameLen - 1 }
  artifactName=$(join_by - $artifactNameary)
  res=$(find $IMAGEJ_ROOT -name $artifactName*.jar )
  if [ "$res" == "" ] && [ "$artifactName" != maars_lib-2.0.0 ] ; then
    cp $jarPath $IMAGEJ_ROOT/plugins/MAARS_deps
    echo "$jarPath copied"
  fi
done

echo "Installation done to $IMAGEJ_ROOT"
