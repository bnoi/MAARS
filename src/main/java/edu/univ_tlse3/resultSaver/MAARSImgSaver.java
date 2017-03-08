package edu.univ_tlse3.resultSaver;

import edu.univ_tlse3.utils.FileUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import ij.plugin.HyperStackConverter;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class MAARSImgSaver {
   public static final String croppedImgs = "croppedImgs";
   private String croppedImgDir;

   public MAARSImgSaver(String pathToFluoDir) {
      this.croppedImgDir = pathToFluoDir + croppedImgs + File.separator;
      FileUtils.createFolder(croppedImgDir);
   }

   /**
    * @param croppedImg set of image
    * @param cellNb        number of current cell
    */
   public void saveSplitImgs(ImagePlus croppedImg, int cellNb, ArrayList<String> arrayChannels, int frameNb) {
      ImagePlus hyperImg = reshapeStack(croppedImg, arrayChannels.size(), frameNb);
      ImagePlus[] channels = ChannelSplitter.split(hyperImg);
      for (int i =0; i<arrayChannels.size();i++){
         String pathToCroppedImg = croppedImgDir + String.valueOf(cellNb) + "_" + arrayChannels.get(i) + ".tif";
         ImagePlus img = channels[i];
         img.setRoi(croppedImg.getRoi());
         IJ.run(img, "Enhance Contrast", "saturated=0.35");
         IJ.saveAsTiff(img, pathToCroppedImg);
      }
   }

   /**
    *
    * @param croppedImgStack
    * @param totalChNb
    * @param totalFrameNb
    * @return
    */
   private ImagePlus reshapeStack(ImagePlus croppedImgStack, int totalChNb, int totalFrameNb){
      return HyperStackConverter.toHyperStack(croppedImgStack, totalChNb,
              croppedImgStack.getStack().getSize()/totalChNb/totalFrameNb,totalFrameNb,"xyzct", "Grayscale");
   }

//   public void exportChannelBtf(Boolean splitChannel, Set<String> arrayChannels) {
//      if (splitChannel) {
//         for (String channel : arrayChannels) {
//            final String btfPath = pathToFluoDir + File.separator + channel + ".ome.btf";
//            if (!FileUtils.exists(btfPath)) {
//               ImageStack currentStack = new ImageStack(mergedFullFieldImg.getWidth(),
//                       mergedFullFieldImg.getHeight());
//               for (int j = 1; j <= mergedFullFieldImg.getImageStack().getSize(); j++) {
//                  if (mergedFullFieldImg.getStack().getSliceLabel(j).equals(channel)) {
//                     currentStack.addSlice(mergedFullFieldImg.getStack().getProcessor(j));
//                  }
//               }
//               final String macroOpts = "outfile=[" + btfPath
//                       + "] splitz=[0] splitc=[0] splitt=[0] compression=[Uncompressed]";
//               ImagePlus currentChImp = new ImagePlus(channel, currentStack);
//               currentChImp.setCalibration(mergedFullFieldImg.getCalibration());
//               lociExporter.setup(macroOpts, currentChImp);
//               lociExporter.run(null);
//            }
//         }
//      } else {
//         String btfPath = pathToFluoDir + "merged.ome.btf";
//         if (!FileUtils.exists(btfPath)) {
//            final String macroOpts = "outfile=[" + btfPath
//                    + "] splitz=[0] splitc=[0] splitt=[0] compression=[Uncompressed]";
//            lociExporter.setup(macroOpts, mergedFullFieldImg);
//            lociExporter.run(null);
//         }
//      }
//   }
}
