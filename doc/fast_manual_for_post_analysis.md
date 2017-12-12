# MAARS post-analysis with command-line (alpha version)  
This is an advance manual for users who want to launch MAARS with command-line in **Fiji**.

## Requirements
1. Structure your root folder as follow:  
![][root_folder]  
The root folder should contain at least **2 folders containing BF-images and Fluo-images separately**
 (not necessarily `BF_1` and `FLUO_1`)<sup>[1](#myfootnote1)</sup>.  
 Rename your images using Micro-Manager style as follow (**recommend**):
**name_of_acquisition**\_MMStack\_**name_of_position/strain**.ome.tif  
PS: if your single Fluo-image stack is larger than 4G. Micro-Manager will create 
another tif file with the same base name and add an `_1`. This is the case in example.
`displaySettings.txt` is **not necessary**.
2. Finish the installation described in [README.md][head]
3. A file named `maars_config.xml`. See **Edit maars_config.xml** section below

### Edit `maars_config.xml`
`maars_config.xml` is the recipe for MAARS. If you don't have it in your root folder.
It will be created and saved into your root folder by the first time runing `batchSegmentation` or 
`batchFluoanalysis`. You can find all inputs inside this 
file (some should be removed). You can edit it with any text editor.
Generally, parameter names are self-explainable. This part will just show you how to
use MAARS segmentation without GUI for the first time.  
Open it with your text editor ([Atom][atom] is used in the demo).
1. Find `SEG_PREFIX` in `SEGMENTATION_PARAMETERS` section and change it to 
your BF folder name
![][segment_param]
2. If `SAVING_PATH` in `GENERAL_ACQUISITION_PARAMETERS` is not your root folder path, change it.
![][general_param]
3. If you don't want `adjustable_watershed`, change the `BATCH_MODE` tag to `true`, 
also no more confirmation dialog will be displayed.

## How to run MAARS with command-line
1. Open `script panel` of Fiji.  
![][script-panel]  
2. Load [this script][batchMAARS] and select `Python` as language.
If everything went well, you should see text with color like this  
![][python_interpreter]  
3. Click on `Run`, you will get this  
![][post_interface]  
User `Browse` button to load the root folder of the acquisition. 
  - **Important Note**: If it's your time running segmenation/fluoanalysis within the root folder and 
  your acquisitions are not named `BF_1` and `FLUO_1`. You will get an error. Please
  edit your BF acquisition name and/or Fluo-acquisition name in `maars_config.xml`
4. When you finished segmentation and fluo analysis in the root, you will get newly 
created folders and files as below:  
![][final_res]

[batchMAARS]: https://github.com/bnoi/MAARS/blob/master/batchMAAR.py
[script-panel]: images/script-panel.png
[python_interpreter]: images/python_interpreter.png
[post_interface]: images/post_interface.png
[final_res]: images/final_res.png
[head]: https://github.com/bnoi/MAARS
[atom]: https://atom.io/
[root_folder]: images/root_folder.png
[segment_param]: images/segment_param.png
[general_param]: images/general_param.png
<a name="myfootnote1">1</a>: In MAARS, by default, we follow Micro-Manager syntax and Micro-Manager
by default always add an `_1` for the first the acquisition. If you forgot to change the name
of acquisition, the number will increment in order to not override older images.


