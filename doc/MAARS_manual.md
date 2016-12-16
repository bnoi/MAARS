# MAARS Manual (v1.0.0)
This manual should be applicable to all OSs (Ubuntu16.04/MacOS Sierra/Win7) 
## Build the environment
* **Install Anaconda** :  
[Download Anaconda][] and then install to its _default (important)_ folder.
* **Install MicroManager 2.0** :  
Download the `nightly build 20161215` [Version 2.0 - Micro-Manager][]  (more recent versions are not guaranteed)
![MM download page][image1]
 and install Micro-Manager 2.0. It can be installed to anywhere you want.
* **Install Java 1.8**  
	Download [Java SE Runtime Environment 8][] and Install it to its default location.  
* **Launch MM2.0 with Java 1.8**  
_For Ubuntu 16.04 and macOS user_, if you have correctly install Java 1.8. MM will automatically detect and launch with it. You can confirm it by seeing the part below. If it doesn't use Java 1.8, the Linux user,should use the command line in the termimal `update-alternateives --config java` to select Java 1.8. As for mac user, please change you JAVA_HOME environment variable to `/Library/Java/JavaVirtualMachines/jdk1.8.0_***.jdk`.
_For windows user :_ Remove the ‘jre’ folder under the root folder of MM, or delete it.  
_Note :_ the fact of removing or deleting it from MM won’t change any functionality of MM.  
Next time when you launch MM, it will let you configure the Java path. Specify where you’ve installed Java 8. If you didn’t change the installation path, it should be in `/Library/Java/JavaVirtualMachines/` for macOS, `/usr/lib/jvm/` for Ubuntu and `C:\Program Files\Java\` for windows.  
_For all users :_ To confirm you got the correct version of Java, you can verify it by looking at this region. You don't necessarily to have the same version. Any Java 1.8.0_*** should be ok.
![][image2]

* **RAM** :  
the more the better. The minimum requirement depends on:
	* If you want to perform a normal acquisition with or without on-the-fly analysis. **4000M** should be enough.
	* if you want to perform the post-analysis you will need at least :`number of fluo-channel * number of timepoint * number of z-stack at each timepoint * single image size of your camera` megabytes of of RAM.   Example **2** (channel)  * **90** (3 acquisitions/min during 30 min) * **6** (z-stack) * **10M** = **10800M**, analysing images also needs RAMs, so at least **13G** is required.

_Important :_ Don't forget to configure ImageJ's RAM allocation. In `Edit/Options/Memory & Threads...` ![][image3]

## Installation
Find the .jar file *MAARS_1.0.0.jar* and the *MAARS_deps* folder in `jars` folder of MAARS's root folder. Then copy them into your `plugins` folder under MM’s root folder.  
![][image4]

## Developers
 `build-mm.sh` : use this script to build Micro-Manager under Linux.
 `mavenized-mm.sh` : use this script to update local Maven repository inside `repo/`. MM jars used are `MMJ_`, `MMCoreJ` and `MMAcqEngine`.

`update-libs.sh` : use Maven to build MAARS and copy all dependencies to `jars/`. It allows "basic users" to copy the required .jar file to a standard Micro-Manager installation.

`install.sh` : Use to install MAARS plugin and its dependencies to Micro-Manager installation.
### Eclipse/Netbean/IntelliJ
# Configuration
# TroubleShooting

[Download Anaconda]: (https://www.continuum.io/downloads)
[Version 2.0 - Micro-Manager]: (https://micro-manager.org/wiki/Version_2.0)
[Java SE Runtime Environment 8]: (http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

[image1]: images/Micro-Manager_download_page.png "Micro-Manager version 2.0 download page"
[image2]: images/imagej_java8.png 
[image3]: images/configure_ram_imagej.png
[image4]: images/copy_dependencies.png