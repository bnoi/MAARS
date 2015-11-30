#!/usr/bin/env bash

set -e

if [ -z "$1" ]
then
    echo "Usage :  ./update-mm.sh /path/to/mm/folder"
    exit 1
fi

MM_FOLDER=$1

# Need to be manually updated
MM_VERSION="2.0.0"

# Specify which file you want to mavenized (only MM jars)
declare -a mm_jars=("MMJ_" "MMCoreJ" "MMAcqEngine")
for i in "${mm_jars[@]}"
do

    JAR_NAME=$i
    JAR_FILE="$MM_FOLDER/plugins/Micro-Manager/$JAR_NAME.jar"

    if [ ! -f "$JAR_FILE" ]
    then
        echo "$JAR_FILE does not exist."
        echo "Does $MM_FOLDER really contain Micro-Manager ?"
        exit 1
    fi

    mvn install:install-file -Dfile=$JAR_FILE \
                             -DgroupId=org.micromanager \
                             -DartifactId=$JAR_NAME \
                             -Dversion=2.0.0 \
                             -Dpackaging=jar \
                            -DlocalRepositoryPath=repo

done
