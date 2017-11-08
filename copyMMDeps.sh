#!/usr/bin/env bash
mvn dependency:copy-dependencies -DoutputDirectory=../jars/MAARS_deps -DexcludeGroupIds=org.micromanager -DexcludeArtifactIds=commons-math3,eventbus,gentyref,jcommon,jfreechart,3D_Viewer,scijava-common
