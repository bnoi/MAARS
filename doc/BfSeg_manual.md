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
Several seconds later, a prompt will be displayed asking whether or not to use [adjustable watershed][ad] plugin to improve the segmentation by adding lines to split merged cells.   
before  
![][before]  
after  
![][after]  
To note, this option doesn't work on Fiji for instance. But if you want to do it, simply install the plugin yourself by following the [instuction][ad]. So when the binary image is displayed. You can run the plugin on it (or even do it manually...).   

Once the lines are added on the binary image. You can click on `ok`.  

PS: If you are working on yeast cell, you won't need to change the parameters in `Preference` panel.  


[bfseg_interface]: images/bfseg_interface.png 
[bfseg_plugin]: images/bfseg_plugin.png  
[download_bfseg]: https://github.com/bnoi/MAARS/tree/master/jars/MAARS_bfseg
[ad]: http://imagejdocu.tudor.lu/doku.php?id=plugin:segmentation:adjustable_watershed:start
[before]: images/before.png 
[after]: images/after.png 
