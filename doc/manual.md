# MAARS manual
## Lauching MAARS
You can find MAARS in `Plugins` of **Micro-Manager toolbar** (not ImageJ toolbar!). Then you will see this main dialog

![main_frame_clean][]  

which is designed to change different parameters for the recording/analyzing process, and the log dialog

![log_dialog][]  
  
which displays the logs.
## Quick start
### Main frame

![main_frame][]  

1. Define width and height of the multi-position course
2. Configure segmentation parameters
3. Configure fluorescent analysis parameters for each time point
4. Static / dynamic analysis :
	- static : take a snapshot for each view field
	- dynamic : perform a N minutes recording/analyzing for each view field
5. Perform post-analysis or acquire new images
6. Save all the specify parameters for current analysis, so that the user can use the same parameters in next run
7. The folder that contains previously acquired image (post-analysis) or the saving path for images and the analyzing results (on-the-fly analysis)
8. Run MAARS
9. Show the visualizer of parameters
10. Stop

# Output of MAARS
If MAARS has successfully finished the analysis, user should at least has these folders and one config file saved.  

![output_folders][]  

the **maars_config.xml** save the configuration used in this acquisition.  
The **X(number)_Y(number)** structure means the data of position X and position Y.  
The folder **X(number)_Y(number)** contains bright-field images and result of segmentation.  

![bf_folder][]  

The bright-field image are always save in the **\_1** folder with a name of **\_1\_MMStack_Pos0.ome.tif**  
All the other files are produced by MAARS segmentation.  
_note_: When performing post-analysis without MAARS segmentation, the BF_result.csv and ROI.zip file should be both provided with the same name.  

The **X(number)_Y(number)_FLUO** folder contains all fluorescence acquisitions and unfiltered results: 

![fluo_folder][]  

For each frame, images are saved in folders respecting **(channel)_(frame)** structure and also the prefix of each image.  
_note_: it is important to respect this structure for post-analysis, including the name of the files.  

![unfiltered_output][]  

The unfiltered results of analysis are saved into three folder: **croppedImgs**, **features** and **spots**.  

**croppedImgs**: cropped images of all detected cells (.tif)  
**features**: measurements such as spindle angle and spindle length for each cell (.csv).  
**spots**:  all spots detected in each cell (.xml from Trackmate).  

The **X(number)_Y(number)_MITOSIS** folder contains all information of cells detected as cells in mitosis:  

![mitosis_folder][]

The folders croppedImgs, features and spots contains only data from cells in mitosis.

**figs** : three plots for each cell.

![elongation][]  
Spindle elongation curve (x labels are the frame numbers. To calculate time in minutes t : `t = frame_number * time interval / 60`)

![slopChangePoints][]  
normalized (to current cell major axis length) spindle elongation with maximum slope change time point detected. In example image, MAARS detected two maximum slope changes time point : frame 15 and frame 69.  

 _colors of slope change_: Blue squares and red triangles below spindle elongation is the slope change of each time point. Blue indicates `slope of time t+1` - `slope of current time point` <= 0; red means > 0
 _colors of fitted line overlapping with elongation curve_: red color indicates positive slope , blue indicated negative slope.

![SPBtracks][]  
SPB tracks in current film

### Segmentation
### Fluorescent analysis
### Visualizer




# For developers
 `build-mm.sh` : use this script to build Micro-Manager under Linux.
 (obsolete)`mavenized-mm.sh` : use this script to update local Maven repository inside `repo/`. MM jars used are `MMJ_`, `MMCoreJ` and `MMAcqEngine`.

`update-libs.sh` : use Maven to build MAARS and copy all dependencies to `jars/`. It allows "basic users" to copy the required .jar file to a standard Micro-Manager installation.

`install.sh` : Use to install MAARS plugin and its dependencies to Micro-Manager installation.

# Developing with Eclipse/Netbean/IntelliJ
# TroubleShooting

[main_frame_clean]: images/main_frame_clean.png
[main_frame]: images/main_frame.png
[log_dialog]: images/log_dialog.png
[output_folders]: images/output_folders.png
[bf_folder]:images/bf_folder.png
[fluo_folder]:images/fluo_folder.png
[unfiltered_output]: images/unfiltered_output.png
[mitosis_folder]: images/mitosis_folder.png
[elongation]: images/elongation.png
[slopChangePoints]: images/slopChangePoints_15_69.png
[SPBtracks]: images/SPBtracks.png
