#!/usr/bin/env bash

vercomp () {
    # Compate the version of two string
    # Example : `vercomp 2.0.0 2.0.0.1` return 2 because 2.0.0 < 2.0.0.1
    if [[ $1 == $2 ]]
    then
        return 0
    fi
    local IFS=.
    local i ver1=($1) ver2=($2)
    # fill empty fields in ver1 with zeros
    for ((i=${#ver1[@]}; i<${#ver2[@]}; i++))
    do
        ver1[i]=0
    done
    for ((i=0; i<${#ver1[@]}; i++))
    do
        if [[ -z ${ver2[i]} ]]
        then
            # fill empty fields in ver2 with zeros
            ver2[i]=0
        fi
        if ((10#${ver1[i]} > 10#${ver2[i]}))
        then
            return 1
        fi
        if ((10#${ver1[i]} < 10#${ver2[i]}))
        then
            return 2
        fi
    done
    return 0
}

if [ -z "$1" ]
then
    echo "Usage : ./mavenized-mm.sh /path/to/mm/folder"
    exit 1
fi

MM_FOLDER=$1

# Need to be manually updated
BASE_MM_VERSION="2.0.0.2"

# Specify which file you want to mavenized (only MM jars)
declare -a mm_jars=("MMJ_" "MMCoreJ" "MMAcqEngine")
for JAR_NAME in "${mm_jars[@]}"
do

    JAR_FILE="$MM_FOLDER/plugins/Micro-Manager/$JAR_NAME.jar"

    if [ ! -f "$JAR_FILE" ]
    then
        echo "$JAR_FILE does not exist."
        echo "Does $MM_FOLDER really contain Micro-Manager ?"
        exit 1
    fi

    if [ -d "repo/org/micromanager/$JAR_NAME" ]
    then
        dirs=( $(find repo/org/micromanager/$JAR_NAME -maxdepth 1 -type d -printf '%P\n') )

        let i=1

        VERSION=$BASE_MM_VERSION
        for dir in "${dirs[@]}"; do
            vercomp $VERSION $dir
            if [[ $? == 2 ]]
            then
                VERSION=$dir
            fi
        done

        # Increment version number
        VERSION=$(echo $VERSION | sed 's/[0-9]$/'"$((LAST_CHAR+1))"'/')
    else
        VERSION=$BASE_MM_VERSION
    fi

    mvn install:install-file -Dfile=$JAR_FILE \
                             -DgroupId=org.micromanager \
                             -DartifactId=$JAR_NAME \
                             -Dversion=$VERSION \
                             -Dpackaging=jar \
                             -DlocalRepositoryPath=repo
done

echo
echo "**************************************"
echo "* Don't forget to update the MM jars *"
echo "* version number in the pom.xml.     *"
echo "**************************************"
