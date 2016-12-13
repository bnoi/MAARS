#!/usr/bin/env sh
set -e

mvn install
mvn dependency:copy-dependencies

rm -f jars/*.jar
rm -fr jars/MAARS_deps
mkdir -p jars/MAARS_deps
cp target/dependency/*.jar jars/MAARS_deps

# Remove duplicated dependencies
rm -f jars/MAARS_deps/3D_Viewer-*.jar
rm -f jars/MAARS_deps/MMJ_*.jar
rm -f jars/MAARS_deps/MMCoreJ*.jar
rm -f jars/MAARS_deps/MMAcqEngine*.jar
rm -f jars/MAARS_deps/miglayout*.jar
rm -f jars/MAARS_deps/ij-*.jar
rm -f jars/MAARS_deps/bsh-*.jar
rm -f jars/MAARS_deps/clojure-*.jar
rm -f jars/MAARS_deps/eventbus-*.jar
rm -f jars/MAARS_deps/gentyref-*.jar
rm -f jars/MAARS_deps/guava-*.jar
rm -f jars/MAARS_deps/jcommon-*.jar
rm -f jars/MAARS_deps/jfreechart-*.jar
rm -f jars/MAARS_deps/rsyntaxtextarea-*.jar
rm -f jars/MAARS_deps/kyro-*.jar
rm -f jars/MAARS_deps/minlog-*.jar
rm -f jars/MAARS_deps/objenesis-*.jar

cp target/MAARS_-1.0-SNAPSHOT.jar jars/
