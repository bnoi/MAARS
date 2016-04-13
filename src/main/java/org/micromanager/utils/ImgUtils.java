package org.micromanager.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.micromanager.cellstateanalysis.Cell;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.RoiScaler;
import ij.plugin.ZProjector;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 25, 2015
 */
public class ImgUtils {
	/**
	 * Do projection by using Max method to create a projection
	 * 
	 * @param img
	 * @return
	 */
	public static ImagePlus zProject(ImagePlus img) {
		ZProjector projector = new ZProjector();
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.setImage(img);
		projector.doProjection();
		ImagePlus imgProjected = projector.getProjection();
		return imgProjected;
	}

	/**
	 * change unit of "cm" to "micron"
	 * 
	 * @param img
	 * @return
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
	 * @param img
	 * @param roi
	 * @return
	 */
	public static ImagePlus cropImgWithRoi(ImagePlus img, Roi roi) {
		ImageStack stack = img.getStack().crop((int) roi.getXBase(), (int) roi.getYBase(), 0,
				(int) roi.getBounds().width, (int) roi.getBounds().height, img.getStack().getSize());
		ImagePlus croppedImg = new ImagePlus("cropped_" + img.getShortTitle(), stack);
		croppedImg.setCalibration(img.getCalibration());
		Roi centeredRoi = centerCroppedRoi(roi);
		croppedImg.setRoi(centeredRoi);
		return croppedImg;
	}

	/**
	 * re-calculate the position of ROI. make it adapt to the cropped image
	 * 
	 * @param roi
	 * @return
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
		Roi croppedRoi = new PolygonRoi(newXsF, newYsF, Roi.POLYGON);
		return croppedRoi;
	}

	/**
	 * Calculate rescale factor.
	 * 
	 * @param cal1
	 * @param cal2
	 * @return
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
	 * 
	 * @param oldRoi
	 *            :roi to rescale
	 * @param factors
	 *            : double[] where first one is a factor to change width and
	 *            second one is a factor to change height
	 * @return
	 */
	public static Roi rescaleRoi(Roi oldRoi, double[] factors) {
		Roi roi = RoiScaler.scale(oldRoi, factors[0], factors[1], true);
		roi.setName("rescaledRoi");
		return roi;
	}

	/**
	 * 
	 * @param fluoDir
	 *            fluo base dir where all full field images are stored
	 * @return an ImagePlus with all channels stacked
	 */
	public static ImagePlus loadFullFluoImgs(String fluoDir) {
		ImagePlus fluoImg = null;
		ImagePlus zprojectImg = null;
		ImageStack fieldStack = null;
		Calibration fluoImgCalib = null;
		String[] listAcqNames = new File(fluoDir).list();
		String pattern = "(\\d+)(_)(\\w+)";
		for (String acqName : listAcqNames) {
			if (Pattern.matches(pattern, acqName)) {
				fluoImg = IJ.openImage(acqName + "/MMStack.ome.tif");
				zprojectImg = ImgUtils.zProject(fluoImg);
				if (fluoImgCalib == null) {
					fluoImgCalib = fluoImg.getCalibration();
				}
				if (fieldStack == null) {
					fieldStack = new ImageStack(zprojectImg.getWidth(), zprojectImg.getHeight());
				}
				fieldStack.addSlice(acqName.split("_", -1)[1] , zprojectImg.getStack().getProcessor(1).convertToFloatProcessor());
			}
		}
		ImagePlus fieldImg = new ImagePlus("merged", fieldStack);
		fieldImg.setCalibration(fluoImgCalib);
		return fieldImg;
	}
	
	/**
	 * crop ROIs from merged field-wide image
	 * 
	 * @param mergedImg
	 * @return HashMap<cell NB, HashMap<channel, corresponding cropped img>>
	 */
	public static HashMap<Integer, HashMap<String, ImagePlus>> cropMergedImpWithRois(ArrayList<Cell> cellArray, ImagePlus mergedImg,
			Boolean splitChannel) {
		HashMap<Integer, HashMap<String, ImagePlus>> croppedImgs = new HashMap<Integer, HashMap<String, ImagePlus>>();
		if (splitChannel) {
			for (int i = 0; i < cellArray.size(); i++) {
				ImagePlus croppedImg = ImgUtils.cropImgWithRoi(mergedImg, cellArray.get(i).getCellShapeRoi());
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
				HashMap<String, ImagePlus> croppedImgInChannel = new HashMap<String, ImagePlus>();
				for (String channel : channelStacks.keySet()) {
					ImagePlus croppedSingleChImg = new ImagePlus(channel, channelStacks.get(channel));
					croppedSingleChImg.setCalibration(mergedImg.getCalibration());
					croppedSingleChImg.setRoi(croppedImg.getRoi());
					croppedImgInChannel.put(channel, croppedSingleChImg);
				}
				croppedImgs.put(i, croppedImgInChannel);
			}
		} else {
			for (int i = 0; i < cellArray.size(); i++) {
				ImagePlus croppedImg = ImgUtils.cropImgWithRoi(mergedImg, cellArray.get(i).getCellShapeRoi());
				HashMap<String, ImagePlus> croppedImgInChannel = new HashMap<String, ImagePlus>();
				croppedImgInChannel.put("merged", croppedImg);
				croppedImgs.put(i, croppedImgInChannel);
			}
		}
		return croppedImgs;
	}
}
