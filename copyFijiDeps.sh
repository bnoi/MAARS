#!/usr/bin/env bash
mvn dependency:copy-dependencies -DaddParentPoms=true \
-DoutputDirectory=../jars/MAARS_deps -DexcludeGroupIds=org.micromanager,net.imglib2 \
-DexcludeArtifactIds=commons-math3,eventbus,gentyref,jcommon,jfreechart,\
3D_Viewer,scijava-common,commons-lang3,commons-logging,ejml,fiji-lib,\
ij1-patcher,ij,imagej-common,imagej-deprecated,j3dcore,j3dutils,jama,\
javassist,jdom2,jgrapht,jgraphx,jitk-tps,log4j,mines-jtk,mpicbg,opencsv,\
pal-optimization,TrackMate,trove4j,udunits,vecmath,VIB-lib,autocomplete,\
base64,bsh,bytelist,clojure,gluegen-rt,groovy,guava,imagej-2.0.0-rc,\
imagej-ops,imagej-plugins-commands,imagej-plugins-tools,imagej-plugins-uploader-ssh,\
imagej-plugins-uploader-webdav,imagej-scripting,imagej-ui-awt,imagej-ui-swing,\
imagej-updater,invokebinder,jcodings,jdatepicker,jffi,jhotdraw,jnr-constants,\
jnr-constants,asm,asm-analysis,asm-commons,asm-tree,asm-util,commons-math,\
jnr-enxio,jnr-ffi,jnr-netdb,jnr-posix,jnr-unixsocket,jnr-x86asm,joda-time,\
joni,jruby-core,jruby-stdlib,jsch,jython-shaded,jzlib,languagesupport,mapdb,\
markdownj,miglayout,minimaven,nailgun-server,object-inspector,options,rhino,\
rsyntaxtextarea,scifio-jai-imageio,scifio,scijava-plugins-commands,\
scijava-plugins-platforms,scijava-plugins-text-markdown,scijava-plugins-text-plain,\
scijava-ui-awt,scijava-ui-swing,scripting-beanshell,scripting-clojure,\
scripting-groovy,scripting-java,scripting-javascript,scripting-jruby,scripting-jython,\
snakeyaml,swing-checkbox-tree,yecht
