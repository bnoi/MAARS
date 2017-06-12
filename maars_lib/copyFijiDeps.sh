#!/usr/bin/env bash
mvn dependency:copy-dependencies -DoutputDirectory=Fiji_deps -DexcludeGroupIds=org.micromanager,net.imglib2 \
-DexcludeArtifactIds=commons-math3,eventbus,gentyref,jcommon,jfreechart,\
3D_Viewer,scijava-common,commons-lang3,commons-logging,ejml,fiji-lib,\
ij1-patcher,ij,imagej-common,imagej-deprecated,j3dcore,j3dutils,jama,\
javassist,jdom2,jgrapht,jgraphx,jitk-tps,log4j,mines-jtk,mpicbg,opencsv,\
pal-optimization,TrackMate,trove4j,udunits,vecmath,VIB-lib
