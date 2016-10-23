package edu.univ_tlse3.utils;

import edu.univ_tlse3.cellstateanalysis.Cell;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
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
    public static ImagePlus cropImgWithRoi(ImagePlus img, Roi roi) {
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
    public static Roi centerCroppedRoi(Roi roi) {
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
        ImagePlus fluoImg;
        ImagePlus zprojectImg;
        ImageStack fieldStack = null;
        Calibration fluoImgCalib = null;
        String[] listAcqNames = new File(fluoDir).list();
        String pattern = "(\\w+)(_)(\\d+)";
        Arrays.sort(listAcqNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.valueOf(o1.split("_", -1)[1]).compareTo(Integer.valueOf(o2.split("_", -1)[1]));
            }
        });
        for (String acqName : listAcqNames) {
            if (Pattern.matches(pattern, acqName)) {
                fluoImg = IJ.openImage(fluoDir + File.separator + acqName + File.separator + acqName+ "_MMStack_Pos0.ome.tif");
                zprojectImg = ImgUtils.zProject(fluoImg);
                if (fluoImgCalib == null) {
                    fluoImgCalib = fluoImg.getCalibration();
                }
                if (fieldStack == null) {
                    fieldStack = new ImageStack(zprojectImg.getWidth(), zprojectImg.getHeight());
                }
                fieldStack.addSlice(acqName.split("_", -1)[0], zprojectImg.getStack().getProcessor(1));
            }
        }
        ImagePlus fieldImg = new ImagePlus("merged", fieldStack);
        fieldImg.setCalibration(fluoImgCalib);
        return fieldImg;
    }

    /**
     * crop ROIs from merged field-wide image
     *
     * @param mergedImg merged full field fluo image
     * @return HashMap<cellNB,corresponding cropped img>
     */
    public static HashMap<String, ImagePlus> cropMergedImpWithRois(Cell cell, ImagePlus mergedImg,
                                                                   Boolean splitChannel) {
        HashMap<String, ImagePlus> croppedImgInChannel = new HashMap<String, ImagePlus>();
        if (splitChannel) {
            ImagePlus croppedImg = ImgUtils.cropImgWithRoi(mergedImg, cell.getCellShapeRoi());
            HashMap<String, ImageStack> channelStacks = new HashMap<String, ImageStack>();
            for (int j = 1; j <= croppedImg.getImageStack().size(); j++) {
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
     * @param listImg list of Images
     * @param channelName
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
     * @param pathToImage
     * @return ImagePlus
     */
    public static void loadBigTiff(String pathToImage){
        LociImporter importer = new LociImporter();
        importer.run(pathToImage);
    }

    /**
     * @param image from micro-manager
     * @return ImageProcessor
     */
    public static ImageProcessor image2imp(Image image){
        MMStudio mm = MMStudio.getInstance();
        ImageProcessor imgProcessor = mm.getDataManager().getImageJConverter().createProcessor(image);
        return imgProcessor.convertToByteProcessor();
    }
}
