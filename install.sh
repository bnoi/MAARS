#!/usr/bin/env bash

set -e

if [ -z "$1" ]
then
    echo "Usage : ./install.sh /path/to/mm/folder"
    exit 1
fi

MM_FOLDER=$1
JAR_FILE="$MM_FOLDER/plugins/Micro-Manager/MMJ_.jar"

if [ ! -f "$JAR_FILE" ]
then
    echo "Does $MM_FOLDER really contain Micro-Manager ?"
    exit 1
fi

# Install MAARS plugin
rm -f $MM_FOLDER/mmplugins/MAARS*.jar
cp jars/MAARS*.jar $MM_FOLDER/mmplugins/

# Install dependencies
rm -fr $MM_FOLDER/plugins/maars_dependencies
cp -R jars/maars_dependencies $MM_FOLDER/plugins/

echo "Installation done to $MM_FOLDER"
