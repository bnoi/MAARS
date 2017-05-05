package edu.univ_tlse3.utils;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.*;
import ij.process.ImageProcessor;
import org.micromanager.data.*;
import org.micromanager.internal.MMStudio;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 25, 2015
 */
public class ImgUtils {
    /**
     *
     * Do projection by using Max method to create a projection
     * @param cal   calibration
     * @param img   image to be projected
     * @return      projected image
     */
    public static ImagePlus zProject(ImagePlus img, Calibration cal) {
        ZProjector projector = new ZProjector();
        projector.setMethod(ZProjector.MAX_METHOD);
        projector.setImage(img);
        projector.doProjection();
        ImagePlus projected = projector.getProjection();
        projected.setCalibration(cal);
        return projected;
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
        for (String acqName : new File(fluoDir).list()) {
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
            ImagePlus newImg = IJ.openImage(fluoDir + File.separator + acqName + File.separator + acqName + "_MMStack_Pos0.ome.tif");
            ImageStack stack = newImg.getStack();
            for (int i = 1; i <= stack.getSize(); i++) {
                stack.setSliceLabel(acqName, i);
            }
            concatenatedFluoImgs = concatenatedFluoImgs == null ?
                    newImg : concatenator.concatenate(concatenatedFluoImgs, newImg, false);
        }
        return concatenatedFluoImgs;
    }

    /**
     * convert a list of Image to an ImagePlus
     *
     * @param listImg           list of Images
     * @param summaryMetadata   metadata from datastore of mm2
     * @param pixelSizeUm       image calib
     * @return imageplus
     */
    public static ImagePlus[] convertImages2Imp(List<Image> listImg, SummaryMetadata summaryMetadata,
                                              double pixelSizeUm) {
//         String[] axisOrder = summaryMetadata.getAxisOrder();

        ImageStack imageStack = new ImageStack(Integer.valueOf(summaryMetadata.getUserData().getString("Width")),
                Integer.valueOf(summaryMetadata.getUserData().getString("Height")));
        for (Image img : listImg) {
            imageStack.addSlice(img.getMetadata().getReceivedTime(), image2imp(img));
        }
        ImagePlus imagePlus = new ImagePlus("",imageStack);
        Calibration cal = new Calibration();
        cal.setUnit("micron");
        cal.pixelWidth = pixelSizeUm;
        cal.pixelHeight = pixelSizeUm;
        cal.pixelDepth = summaryMetadata.getZStepUm();
        imagePlus.setCalibration(cal);
       imagePlus = HyperStackConverter.toHyperStack(imagePlus, summaryMetadata.getIntendedDimensions().getChannel(),
                summaryMetadata.getIntendedDimensions().getZ(), summaryMetadata.getIntendedDimensions().getTime(),
                "xytzc", "Grayscale");
       ImagePlus[] channels;
        if (summaryMetadata.getChannelNames().length>1){
           channels =  ChannelSplitter.split(imagePlus);
           for (int i =0 ; i< channels.length;i++){
              channels[i].setTitle(summaryMetadata.getChannelNames()[i]);
           }
        }else{
           channels = new ImagePlus[]{imagePlus};
        }
       return channels;
    }

    /**
     * @param image from micro-manager
     * @return ImageProcessor
     */
    private static ImageProcessor image2imp(Image image) {
        MMStudio mm = MMStudio.getInstance();
        return mm.getDataManager().getImageJConverter().createProcessor(image);
    }

    public static  ArrayList<Coords> getSortedCoords(Datastore ds){
       ArrayList<Coords> coordsList = new ArrayList<>();
       for (Coords coords : ds.getUnorderedImageCoords()) {
          coordsList.add(coords);
       }
       java.util.Collections.sort(coordsList, (a, b) -> {
          int p1 = a.getStagePosition();
          int p2 = b.getStagePosition();
          if (p1 != p2) {
             return p1 < p2 ? -1 : 1;
          }
          int t1 = a.getTime();
          int t2 = b.getTime();
          if (t1 != t2) {
             return t1 < t2 ? -1 : 1;
          }
          int z1 = a.getZ();
          int z2 = b.getZ();
          if (z1 != z2) {
             return z1 < z2 ? -1 : 1;
          }
          int c1 = a.getChannel();
          int c2 = b.getChannel();
          return c1 < c2 ? -1 : 1;
       });
       return coordsList;
    }
}
