#!/usr/bin/env sh
set -e

mvn install
mvn dependency:copy-dependencies

rm -fr jars/lib
mkdir -p jars/lib
cp target/dependency/*.jar jars/lib

# Remove some duplicated plugins
rm -f jars/lib/3D_Viewer-3.1.0.jar

cp target/MAARS_-1.0.jar jars/
