# BfSeg Manual
This is a plugin for fission/budding yeast cell segmentation (tested). And should also work on E.coli and other bacterias with refringent cell border (to be confirmed).  
PS: you can skip the installation step described in [README.md][first_page], if you need only this BF segmentation
## Requirements
**Your bright-field image should be acquired with a Z-range of 10 micron (+-5) with a step of 0.3 micron = 34 images in total** and it looks like this:  
![][bf_seg_gif]  
**Run ImageJ with Java 1.6 +**  
**RAM** : About 400M  
**MAARS modules** : copy [`maars_lib_XXX-SNAPSHOT.jar`][download] and [`maars_bfSeg_XXX-SNAPSHOT.jar`][download] into your `plugins` folder of ImageJ.
## Optional
[Download][download] the `Adjustable_Watershed.java` for better binary segmentation,
see more details [here][ad]

## How to use
 - Open your image. `Make sure it is calibrated, as least for x,y,z`.  
 - Find the `SegmentPombe` plugin, and click on it:  
  ![][bfseg_plugin]  
 - Click `load` to load your current image :   
 ![][bfseg_interface]  

And Run!  
Several seconds later, a prompt will be displayed asking whether or not to use [adjustable watershed][ad] plugin to improve the segmentation by adding lines that split merged cells.   
before  
![][before]  
after  
![][after]  
To note,for Fiji user simply install the plugin yourself by following the [instuction][ad].  

Once the lines are added on the binary image (***can take 1-2s***). You can click on `ok`.  

PS: If you are working on fission yeast cell, you won't need to change the parameters in `Preference` panel (except the `Range for cell area min/max` which depends on your strain). As for ***budding yeast***, you will have to decrease the solidity threshold to around 0.7 or even lower ([what is solidity][solidity]). Generally, other parameters are self-explainable and you can play with them to meet your needs.

[first_page]: https://github.com/bnoi/MAARS/tree/master
[bf_seg_gif]: images/seg_bf.gif
[bfseg_interface]: images/bfseg_interface.png
[bfseg_plugin]: images/bfseg_plugin.png  
[download]: https://github.com/bnoi/MAARS/tree/master/jars
[ad]: http://imagejdocu.tudor.lu/doku.php?id=plugin:segmentation:adjustable_watershed:start
[before]: images/before.png
[after]: images/after.png
[solidity]: http://imagej.1557.x6.nabble.com/Solidity-td5003132.html
