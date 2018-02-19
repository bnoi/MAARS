# MAARS Installation
This manual should be applicable to all OSs (Ubuntu16.04/MacOS Sierra/Win7)

## Prepare the environment
* **Install Java >=1.8**  
	Download [Java SE Runtime Environment 8][] and Install it to its default location.  
	Make sure your Fiji/Micro-Manager is running with Java >= 1.8, by seeing it here.
	![][image2]
* **RAM** :  
Generally, the more the better. This also depends on the size of your images.
* **Prepare Anaconda(Post-acquisition) and/or Micro-Manager(on-the-fly)**:
see **Options** part below.


_**Important** :_ Don't forget to configure ImageJ's RAM allocation. In `Edit/Options/Memory & Threads...`  
![][image3]

## Installation
In the [download page][download], You have 4 `.jar` files:
 
1. [`maars_lib_[version]-SNAPSHOT.jar`][download] (Core)
2. [`maars_bfSeg_[version]-SNAPSHOT.jar`][download] (Segmentation with GUI, optional)
3. [`maars_otf_[version]-SNAPSHOT.jar`][download] (On-the-fly with Micro-Manager, optional)
4. [`maars_post_[version]-SNAPSHOT.jar`][download] (Post-acquisition analysis with Fiji, optional)

copy `maars_lib_XXX-SNAPSHOT.jar (the core)` and `the optional part(s) (otf, post, bfSeg)` you need into your `plugins folder` of ImageJ/Fiji.  

### Dependencies
Download the `Fiji/MM_deps` into the `jars` folder of `Fiji/MM`.

Example:
- I only want to use GUI segmentation (download 1 + 2 + `Fiji or MM_deps`).
- I only want to use on-the-fly anlaysis (download 1 + 3 + `MM_deps`).
- I only want to use post-acquisition anlaysis (download 1 + 4 + `Fiji_deps`).  

## Options

* **About `Adjustable_Watershed.java`**
see [this manual](BfSeg_manual.md) to decide whether or not to install it.

* **(Optional if you need <span style="color:red"> complete analysis of MAARS</span>) Install Anaconda **  
If you want more parameters than the ones listed in the image saved into [Cellh5 format](http://cellh5.org/) file. Go install Anaconda.
![](images/primary_output.png)
Otherwise, you won't need to install Anaconda, since it is a Python plugin for for advanced analysis.  
Download [Anaconda3][Anaconda] and then install to its _default (important)_ folder.
Then install the following packages:
	- Numpy (should be in Anaconda3 already)
	- Pandas (should be in Anaconda3 already)
	- matplotlib (should be in Anaconda3 already)
	- Scipy
	- cellh5
	- Bokeh  
with command-line `conda install scipy bokeh` and `conda install -c bioinfotongli cellh5`.  
Sorry if there is still missing packages, just install them with `conda install [package name]`.


* **(Optional if you need <span style="color:red"> on-the-fly analysis</span>) Install Micro-Manager 2.0** (_**not compatible**_ with version MM 1.4):  
Download the `nightly build 20161215` [Version 2.0 - Micro-Manager][]  (more recent versions are not guaranteed) and install Micro-Manager 2.0. It can be installed to anywhere you want.  
![MM download page][image1]  
Once MM2.0 is correctly installed, please follow this [tutorial][] to pilot your microscope. If you want to perform post-analysis without the microscope, please [download this demo-config file][] and put it under the root folder of MAARS.
* **(Optional if you need <span style="color:red"> on-the-fly analysis</span>) Launch MM2.0 with Java 1.8**  
_**For windows user** :_ Remove the ‘jre’ folder under the root folder of MM, or delete it.  
_Note :_ the fact of removing or deleting it from MM won’t change any functionality of MM.  
Launch MM2.0 again, you should specify Java 1.8 path `C:\Program Files\Java\jre1.8***` as its default Java path.  
_**For Ubuntu 16.04 and macOS user**_, if you have correctly install Java 1.8. MM will automatically detect and launch with it. You can confirm it by seeing the part below. If it doesn't use Java 1.8, the Linux user,should use the command line in the terminal `update-alternatives --config java` to select Java 1.8. As for mac user, please change you JAVA_HOME environment variable to `/Library/Java/JavaVirtualMachines/jdk1.8.0_***.jdk`.  
_**For all users** :_ To confirm you got the correct version of Java, you can verify it by looking at this region. You don't necessarily to have the same version. Any Java 1.8.0_*** should be OK.
![][image2]  


[Anaconda]: https://conda.io/docs/user-guide/install/download.html
[Version 2.0 - Micro-Manager]: https://micro-manager.org/wiki/Version_2.0
[Java SE Runtime Environment 8]: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
[tutorial]: https://micro-manager.org/wiki/Micro-Manager_Configuration_Guide
[download this demo-config file]: https://raw.githubusercontent.com/micro-manager/micro-manager/master/bindist/any-platform/MMConfig_demo.cfg
[download]: https://github.com/bnoi/MAARS/tree/master/jars
[image1]: images/Micro-Manager_download_page.png "Micro-Manager version 2.0 download page"
[image2]: images/imagej_java8.png
[image3]: images/configure_ram_imagej.png
[image4]: images/unzip.png
