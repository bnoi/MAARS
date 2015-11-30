#!/usr/bin/env sh
set -e

mvn install
mvn dependency:copy-dependencies

rm -f jars/*.jar
rm -fr jars/maars_dependencies
mkdir -p jars/maars_dependencies
cp target/dependency/*.jar jars/maars_dependencies

# Remove some duplicated plugins
rm -f jars/maars_dependencies/3D_Viewer-*.jar
rm -f jars/maars_dependencies/MMJ_*.jar
rm -f jars/maars_dependencies/MMCoreJ*.jar
rm -f jars/maars_dependencies/MMAcqEngine*.jar
rm -f jars/maars_dependencies/miglayout*.jar

cp target/MAARS_-1.0-SNAPSHOT.jar jars/
