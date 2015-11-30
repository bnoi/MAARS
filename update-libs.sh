#!/usr/bin/env sh
set -e

mvn install
mvn dependency:copy-dependencies

rm -f jars/*.jar
rm -fr jars/maars_dependencies
mkdir -p jars/maars_dependencies
cp target/dependency/*.jar jars/maars_dependencies

# Remove duplicated dependencies
rm -f jars/maars_dependencies/3D_Viewer-*.jar
rm -f jars/maars_dependencies/MMJ_*.jar
rm -f jars/maars_dependencies/MMCoreJ*.jar
rm -f jars/maars_dependencies/MMAcqEngine*.jar
rm -f jars/maars_dependencies/miglayout*.jar
rm -f jars/maars_dependencies/ij-*.jar
rm -f jars/maars_dependencies/bsh-*.jar
rm -f jars/maars_dependencies/clojure-*.jar
rm -f jars/maars_dependencies/eventbus-*.jar
rm -f jars/maars_dependencies/gentyref-*.jar
rm -f jars/maars_dependencies/guava-*.jar
rm -f jars/maars_dependencies/jcommon-*.jar
rm -f jars/maars_dependencies/jfreechart-*.jar
rm -f jars/maars_dependencies/rsyntaxtextarea-*.jar

cp target/MAARS_-1.0-SNAPSHOT.jar jars/
