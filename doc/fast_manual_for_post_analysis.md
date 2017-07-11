# MAARS's independent post-analysis (testing version)  
You will have to enter 3 parameters before running the analysis.  
![][interface]  
1. Perform segmentation or fluo-analysis. (Generally, you should perform segmentation before running fluo-analysis.)  

2. If segmentation is selected, select an existing maars_config.xml file as template. After preforming the analysis, a newly modifier config file will be saved in the input folder of option 3. Else if fluo-analysis option is selected. this field is kind of useless. Because MAARS will by default take the existing config file in the given folder. However, you still need to give a valid path for the validation. This will be better handled in the future.  

3. Give the full path of the folder to which contains tiff images (segmentaion / fluorescence).

4. Click "validate" then "Ok" to run.  

## During fluo-analysis  
The Bioformat Importor dialog will be displayed (once for each field)  
![][importor_params]  
Please configure it as in the images above. Then, you should specify manually where are the fluo-images (choose no matter which image in the fluo-folder, bioformat consider it as a set of images).  
Now look at the log dialog. Information as below will be displayed. 
![][log1]  
Please choose and open this and only this images in this dialog.  
![][select_images]  
**I should be able to automatize these time-consuming operations in the future if Bioformat supports it.**


### Known issus
1. There is no check to verify whether the option 3 contains fluo images or segmentation images. So if user accidentally chose to perform segmentation on fluo-images.
The program will freeze. **In this case, Please re-launch MAARS and manually change the seg_prefix in the config file ![][seg_prefix_config] to the segmentation folder name before launching next fluo-analysis**  
[seg_prefix_config]: images/seg_prefix_config.png
[select_images]: images/select_images.png
[importor_params]: images/importor_configure.png
[interface]: images/interface.png
[log1]: images/log1.png