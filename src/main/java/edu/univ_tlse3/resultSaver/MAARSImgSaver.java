package edu.univ_tlse3.resultSaver;

import edu.univ_tlse3.utils.FileUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import loci.plugins.LociExporter;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

public class MAARSImgSaver {
   private String pathToFluoDir;
   private String croppedImgDir;
   private ImagePlus mergedFullFieldImg;

   public MAARSImgSaver(String pathToFluoDir, ImagePlus mergedFullFieldImg) {
      this.pathToFluoDir = pathToFluoDir;
      this.croppedImgDir = pathToFluoDir + File.separator + "croppedImgs" + File.separator;
      this.mergedFullFieldImg = mergedFullFieldImg;
      FileUtils.createFolder(croppedImgDir);
   }

   /**
    * @param croppedImgSet set of image
    * @param cellNb        number of current cell
    */
   public void saveCroppedImgs(HashMap<String, ImagePlus> croppedImgSet, int cellNb) {
      for (String s : croppedImgSet.keySet()) {
         String pathToCroppedImg = croppedImgDir + String.valueOf(cellNb) + "_" + s;
         ImagePlus imp = croppedImgSet.get(s);
         IJ.run(imp, "Enhance Contrast", "saturated=0.35");
         IJ.run(imp, "16-bit", "");
         IJ.saveAsTiff(imp, pathToCroppedImg);
      }
   }

   public void exportChannelBtf(Boolean splitChannel, Set<String> arrayChannels) {
      LociExporter lociExporter = new LociExporter();
      if (splitChannel) {
         for (String channel : arrayChannels) {
            final String btfPath = pathToFluoDir + File.separator + channel + ".ome.btf";
            if (!FileUtils.exists(btfPath)) {
               ImageStack currentStack = new ImageStack(mergedFullFieldImg.getWidth(),
                       mergedFullFieldImg.getHeight());
               for (int j = 1; j <= mergedFullFieldImg.getImageStack().getSize(); j++) {
                  if (mergedFullFieldImg.getStack().getSliceLabel(j).equals(channel)) {
                     currentStack.addSlice(mergedFullFieldImg.getStack().getProcessor(j));
                  }
               }
               final String macroOpts = "outfile=[" + btfPath
                       + "] splitz=[0] splitc=[0] splitt=[0] compression=[Uncompressed]";
               ImagePlus currentChImp = new ImagePlus(channel, currentStack);
               currentChImp.setCalibration(mergedFullFieldImg.getCalibration());
               lociExporter.setup(macroOpts, currentChImp);
               lociExporter.run(null);
            }
         }
      } else {
         String btfPath = pathToFluoDir + "merged.ome.btf";
         if (!FileUtils.exists(btfPath)) {
            final String macroOpts = "outfile=[" + btfPath
                    + "] splitz=[0] splitc=[0] splitt=[0] compression=[Uncompressed]";
            lociExporter.setup(macroOpts, mergedFullFieldImg);
            lociExporter.run(null);
         }
      }
   }
}
