package maars.io;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Concatenator;
import maars.utils.FileUtils;

import java.io.File;

public class MAARSImgSaver {
   public static final String croppedImgs = "croppedImgs";
   public String croppedImgDir;

   public MAARSImgSaver(String pathToFluoDir) {
      this.croppedImgDir = pathToFluoDir + File.separator + croppedImgs + File.separator;
      FileUtils.createFolder(croppedImgDir);
   }

   /**
    * @param croppedImg  set of image
    * @param cellNb      number of current cell
    * @param channelName channel name
    * @param append      append to existing image or not
    */
   public void saveImgs(ImagePlus croppedImg, int cellNb, String channelName, boolean append) {
      String pathToCroppedImg = croppedImgDir + String.valueOf(cellNb) + "_" + channelName + ".tif";
      IJ.run(croppedImg, "Enhance Contrast", "saturated=0.35");
      if (FileUtils.exists(pathToCroppedImg) && append) {
         Concatenator concatenator = new Concatenator();
         concatenator.setIm5D(true);
         ImagePlus new_croppedImg = concatenator.concatenate(IJ.openImage(pathToCroppedImg), croppedImg, false);
         new_croppedImg.setRoi(croppedImg.getRoi());
         IJ.saveAsTiff(new_croppedImg, pathToCroppedImg);
      } else {
         IJ.saveAsTiff(croppedImg, pathToCroppedImg);
      }
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
