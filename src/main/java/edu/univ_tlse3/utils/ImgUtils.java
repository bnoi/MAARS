package edu.univ_tlse3.utils;

import edu.univ_tlse3.cellstateanalysis.Cell;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.Concatenator;
import ij.plugin.RoiScaler;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;
import loci.plugins.LociImporter;
import mmcorej.CMMCore;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 25, 2015
 */
public class ImgUtils {
   /**
    * Do projection by using Max method to create a projection
    *
    * @param img image to be projected
    * @return projected image
    */
   public static ImagePlus zProject(ImagePlus img) {
      ZProjector projector = new ZProjector();
      projector.setMethod(ZProjector.MAX_METHOD);
      projector.setImage(img);
      projector.doProjection();
      return projector.getProjection();
   }

   /**
    * change unit of "cm" to "micron"
    *
    * @param img img with calibration to change
    * @return calibration changed image
    */
   public static ImagePlus unitCmToMicron(ImagePlus img) {
      img.getCalibration().setUnit("micron");
      img.getCalibration().pixelWidth = img.getCalibration().pixelWidth * 10000;
      img.getCalibration().pixelHeight = img.getCalibration().pixelHeight * 10000;
      return img;
   }

   /**
    * Crop the ROI on the given image
    *
    * @param img ImagePlus
    * @param roi region of interest to crop
    * @return cropped imagePLus
    */
   private static ImagePlus cropImgWithRoi(ImagePlus img, Roi roi) {
      ImageStack stack = img.getStack().crop((int) roi.getXBase(), (int) roi.getYBase(), 0,
              roi.getBounds().width, roi.getBounds().height, img.getStack().getSize());
      ImagePlus croppedImg = new ImagePlus("cropped_" + img.getShortTitle(), stack);
      croppedImg.setCalibration(img.getCalibration());
      Roi centeredRoi = centerCroppedRoi(roi);
      croppedImg.setRoi(centeredRoi);
      return croppedImg;
   }

   /**
    * re-calculate the position of ROI. make it adapt to the cropped image
    *
    * @param roi roi to process
    * @return processed ROI
    */
   private static Roi centerCroppedRoi(Roi roi) {
      int[] newXs = roi.getPolygon().xpoints;
      int[] newYs = roi.getPolygon().ypoints;
      int nbPoints = roi.getPolygon().npoints;
      for (int i = 0; i < nbPoints; i++) {
         newXs[i] = newXs[i] - (int) roi.getXBase();
         newYs[i] = newYs[i] - (int) roi.getYBase();
      }
      float[] newXsF = new float[nbPoints];
      float[] newYsF = new float[nbPoints];
      for (int i = 0; i < nbPoints; i++) {
         newXsF[i] = (float) newXs[i];
         newYsF[i] = (float) newYs[i];
      }
      return new PolygonRoi(newXsF, newYsF, Roi.POLYGON);
   }

   /**
    * Calculate rescale factor.
    *
    * @param cal1 calibration of imagePlus
    * @param cal2 calibration of imagePlus
    * @return factors of correction for x and y
    */
   public static double[] getRescaleFactor(Calibration cal1, Calibration cal2) {
      double[] factors = new double[2];
      if (cal1.equals(cal2)) {
         factors[0] = 1;
         factors[1] = 1;
      } else {
         factors[0] = cal1.pixelWidth / cal2.pixelWidth;
         factors[1] = cal1.pixelHeight / cal2.pixelHeight;
      }
      return factors;
   }

   /**
    * @param oldRoi  :roi to rescale
    * @param factors : double[] where first one is a factor to change width and
    *                second one is a factor to change height
    * @return rescaled ROI
    */
   public static Roi rescaleRoi(Roi oldRoi, double[] factors) {
      Roi roi = RoiScaler.scale(oldRoi, factors[0], factors[1], true);
      roi.setName("rescaledRoi");
      return roi;
   }

   /**
    * @param fluoDir fluo base dir where all full field images are stored
    * @return an ImagePlus with all channels stacked
    */
   public static ImagePlus loadFullFluoImgs(String fluoDir) {
      Concatenator concatenator = new Concatenator();
      ArrayList<String> listAcqNames = new ArrayList<>();
      String pattern = "(\\w+)(_)(\\d+)";
      assert listAcqNames != null;
      for (String acqName :  new File(fluoDir).list()) {
         if (Pattern.matches(pattern, acqName)) {
            listAcqNames.add(acqName);
         }
      }
      String[] listAcqNamesArray = listAcqNames.toArray(new String[listAcqNames.size()]);
      Arrays.sort(listAcqNamesArray,
              Comparator.comparing(o -> Integer.parseInt(o.split("_", -1)[1])));
      concatenator.setIm5D(true);
      ImagePlus concatenatedFluoImgs = null;
      for (String acqName : listAcqNamesArray) {
         System.out.println(acqName);
         ImagePlus newImg = IJ.openImage(fluoDir + File.separator + acqName + File.separator + acqName + "_MMStack_Pos0.ome.tif");
         if (concatenatedFluoImgs==null){
            concatenatedFluoImgs = newImg;
         }else{
            concatenatedFluoImgs = concatenator.concatenate(concatenatedFluoImgs, newImg,false);
         }
      }
      return concatenatedFluoImgs;
   }

   /**
    * crop ROIs from merged field-wide image
    *
    * @param cell         Cell object from MAARS
    * @param splitChannel return data in splitChannels or not
    * @param mergedImg    merged full field fluo image
    * @return HashMap of cellNB,corresponding cropped img
    */
   public static HashMap<String, ImagePlus> cropMergedImpWithRois(Cell cell, ImagePlus mergedImg,
                                                                  Boolean splitChannel) {
      HashMap<String, ImagePlus> croppedImgInChannel = new HashMap<>();
      if (splitChannel) {
         ImagePlus croppedImg = ImgUtils.cropImgWithRoi(mergedImg, cell.getCellShapeRoi());
         HashMap<String, ImageStack> channelStacks = new HashMap<>();
         for (int j = 1; j <= croppedImg.getImageStack().getSize(); j++) {
            // TODO problem here
            String currentLabel = croppedImg.getImageStack().getSliceLabel(j);
            if (!channelStacks.containsKey(currentLabel)) {
               channelStacks.put(currentLabel, new ImageStack(croppedImg.getWidth(), croppedImg.getHeight()));
            }
            channelStacks.get(currentLabel)
                    .addSlice(croppedImg.getStack().getProcessor(j).convertToFloatProcessor());
         }

         for (String channel : channelStacks.keySet()) {
            ImagePlus croppedSingleChImg = new ImagePlus(channel, channelStacks.get(channel));
            croppedSingleChImg.setCalibration(mergedImg.getCalibration());
            croppedSingleChImg.setRoi(croppedImg.getRoi());
            croppedImgInChannel.put(channel, croppedSingleChImg);
         }
      } else {
         ImagePlus croppedImg = ImgUtils.cropImgWithRoi(mergedImg, cell.getCellShapeRoi());
         croppedImgInChannel.put("merged", croppedImg);
      }
      return croppedImgInChannel;
   }

   /**
    * convert a list of Image to an ImagePlus
    *
    * @param listImg     list of Images
    * @param channelName name of the channel
    * @return imageplus
    */
   public static ImagePlus convertImages2Imp(List<Image> listImg, String channelName) {
      CMMCore mmc = MMStudio.getInstance().getCore();
      ImageStack imageStack = new ImageStack((int) mmc.getImageWidth(), (int) mmc.getImageHeight());
      for (Image img : listImg) {
         imageStack.addSlice(ImgUtils.image2imp(img));
      }
      ImagePlus imagePlus = new ImagePlus(channelName, imageStack);
      Calibration cal = new Calibration();
      cal.setUnit("micron");
      cal.pixelWidth = mmc.getPixelSizeUm();
      cal.pixelHeight = mmc.getPixelSizeUm();
      imagePlus.setCalibration(cal);
      return imagePlus;
   }

   /**
    * @param pathToImage saving path of big tiff file
    */
   public static void loadBigTiff(String pathToImage) {
      LociImporter importer = new LociImporter();
      importer.run(pathToImage);
   }

   /**
    * @param image from micro-manager
    * @return ImageProcessor
    */
   private static ImageProcessor image2imp(Image image) {
      MMStudio mm = MMStudio.getInstance();
      ImageProcessor imgProcessor = mm.getDataManager().getImageJConverter().createProcessor(image);
      return imgProcessor.convertToFloatProcessor();
   }
}
