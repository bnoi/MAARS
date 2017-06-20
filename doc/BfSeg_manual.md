# BfSeg Installation
## Build the environment  
**Install Java 1.6 +**  
**RAM** : About 400M  

## Installation
[Download][download_bfseg] the `Adjustable_Watershed.java`, `maars_lib_1.0-SNAPSHOT.jar` and `maars_bfSeg_1.0-SNAPSHOT.jar` into your `plugins` folder of ImageJ.  

## How to use
 - Open your image to segment  
 - Click the plugin, like in this image :  
  ![][bfseg_plugin]  
 - Click `load` to load your current image :   
 ![][bfseg_interface]  

And Run!  
Several seconds later, a prompt will be displayed to use [adjustable watershed][ad] plugin (or not) to make the segmenation even better.
If you are working on yeast cell, you won't need to change the parameters in `Preference` panel.  


[bfseg_interface]: images/bfseg_interface.png 
[bfseg_plugin]: images/bfseg_plugin.png  
[download_bfseg]: https://github.com/bnoi/MAARS/tree/multiPosition/jars/MAARS_bfseg
[ad]: http://imagejdocu.tudor.lu/doku.php?id=plugin:segmentation:adjustable_watershed:start
