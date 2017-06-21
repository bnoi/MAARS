# BfSeg Installation
## Build the environment  
**Install Java 1.6 +**  
**RAM** : About 400M  

## Installation
[Download][download_bfseg] the `Adjustable_Watershed.java`, `maars_lib_1.0-SNAPSHOT.jar` and `maars_bfSeg_1.0-SNAPSHOT.jar` into your `plugins` folder of ImageJ.  

## How to use
 - Open your image. `Make sure it is calibrated, as least for x,y,z`.  
 - Click the plugin, like in this image :  
  ![][bfseg_plugin]  
 - Click `load` to load your current image :   
 ![][bfseg_interface]  

And Run!  
Several seconds later, a prompt will be displayed to use [adjustable watershed][ad] plugin (or not) to make the segmenation even better. To note that, this optimization will not work on Fiji. Since it doesn't support `compile and run` option. 

If you want to run it on Fiji, please install the plugin yourself by following the [instuction][ad]. The essential of this step is to close the gaps between cells,   
before  
![][before]  
after  
![][after]  
Once the lines are added on the binary image. You can click on `ok`.  
If you are working on yeast cell, you won't need to change the parameters in `Preference` panel.  


[bfseg_interface]: images/bfseg_interface.png 
[bfseg_plugin]: images/bfseg_plugin.png  
[download_bfseg]: https://github.com/bnoi/MAARS/tree/multiPosition/jars/MAARS_bfseg
[ad]: http://imagejdocu.tudor.lu/doku.php?id=plugin:segmentation:adjustable_watershed:start
[before]: images/before.png 
[after]: images/after.png 
