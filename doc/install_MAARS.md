# MAARS Installation for FluoAnalysis
Since post-analysis is not pure Java, You will have to install Python with Anaconda.This manual should be applicable to all OSs (Ubuntu16.04/MacOS Sierra/Win7)

## Build the environment
* **Install Anaconda** (for post-analysis with python) :  
Download [Anaconda][] and then install to its _default (important)_ folder.
* **Install Java 1.8**  
	Download [Java SE Runtime Environment 8][] and Install it to its default location.  
* **RAM** :  
The more the better. The minimum requirement depends on what you want to do with MAARS:
	* If you want to perform a normal acquisition with or without on-the-fly analysis. **6000M** should be enough.
	* if you want to perform the post-analysis, you will need at least : `number of fluo-channel * number of timepoint * number of z-stack at each time point * single image size of your camera` megabytes of of RAM.  
Example : **2** (channels)  * **90** (3 acquisitions/min during 30 min) * **6** (z-stack) * **10M**(camera snap image size, resolution=2560*2160) = **10800M**, analyzing images also needs RAMs, so approximatively **13G** is required.
* **Install Micro-Manager 2.0** (_**not compatible**_ with version MM 1.4):  
Download the `nightly build 20161215` [Version 2.0 - Micro-Manager][]  (more recent versions are not guaranteed) and install Micro-Manager 2.0. It can be installed to anywhere you want.  
![MM download page][image1]  
Once MM2.0 is correctly installed, please follow this [tutorial][] to pilot your microscope. If you want to perform post-analysis without the microscope, please [download this demo-config file][] and put it under the root folder of MAARS.
* **Launch MM2.0 with Java 1.8**  
_**For windows user** :_ Remove the ‘jre’ folder under the root folder of MM, or delete it.  
_Note :_ the fact of removing or deleting it from MM won’t change any functionality of MM.  
Launch MM2.0 again, you should specify Java 1.8 path `C:\Program Files\Java\jre1.8***` as its default Java path.  
_**For Ubuntu 16.04 and macOS user**_, if you have correctly install Java 1.8. MM will automatically detect and launch with it. You can confirm it by seeing the part below. If it doesn't use Java 1.8, the Linux user,should use the command line in the terminal `update-alternatives --config java` to select Java 1.8. As for mac user, please change you JAVA_HOME environment variable to `/Library/Java/JavaVirtualMachines/jdk1.8.0_***.jdk`.  
_**For all users** :_ To confirm you got the correct version of Java, you can verify it by looking at this region. You don't necessarily to have the same version. Any Java 1.8.0_*** should be OK.
![][image2]  
_**Important** :_ Don't forget to configure ImageJ's RAM allocation. In `Edit/Options/Memory & Threads...`  
![][image3]


[Anaconda]: https://www.continuum.io/downloads
[Version 2.0 - Micro-Manager]: https://micro-manager.org/wiki/Version_2.0
[Java SE Runtime Environment 8]: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
[tutorial]: https://micro-manager.org/wiki/Micro-Manager_Configuration_Guide
[download this demo-config file]: https://raw.githubusercontent.com/micro-manager/micro-manager/master/bindist/any-platform/MMConfig_demo.cfg
[download_maars]: https://github.com/bnoi/MAARS/releases/tag/v1.0.0
[image1]: images/Micro-Manager_download_page.png "Micro-Manager version 2.0 download page"
[image2]: images/imagej_java8.png
[image3]: images/configure_ram_imagej.png
[image4]: images/unzip.png
