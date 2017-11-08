#!/usr/bin/env sh
set -e

mvn clean
mvn install

rm -f jars/*.jar
mkdir -p jars/MAARS_deps

maars_folders=(maars_bfSeg maars_lib maars_otf maars_post)
for f in ${maars_folders[*]}
do
  cp $f/target/$f*-SNAPSHOT.jar jars/
done
echo libs updated!