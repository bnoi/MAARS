#!/usr/bin/env bash
cd maars_lib
mvn dependency:copy-dependencies -DoutputDirectory=../jars/MAARS_deps -DexcludeGroupIds=org.micromanager
cd ../maars_otf
mvn dependency:copy-dependencies -DoutputDirectory=../jars/MAARS_deps -DexcludeGroupIds=org.micromanager
cd ../maars_bfSeg
mvn dependency:copy-dependencies -DoutputDirectory=../jars/MAARS_deps -DexcludeGroupIds=org.micromanager
cd ..
