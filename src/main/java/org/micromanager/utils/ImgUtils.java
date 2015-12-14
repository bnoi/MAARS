package org.micromanager.utils;

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
		ImagePlus imgProject = projector.getProjection();
		imgProject.setCalibration(img.getCalibration());
		imgProject.setTitle(img.getTitle());
		return imgProject;
	}

	/**
	 * change unit of "cm" to "micron"
	 * 
	 * @param img
	 * @return
	 */
	public static ImagePlus unitCmToMicron(ImagePlus img) {
		if (img.getCalibration().getUnit().equals("cm")) {
			img.getCalibration().setUnit("micron");
			img.getCalibration().pixelWidth = img.getCalibration().pixelWidth * 10000;
			img.getCalibration().pixelHeight = img.getCalibration().pixelHeight * 10000;
		}
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
		Roi roi = RoiScaler.scale(oldRoi, factors[0], factors[1], false);
		roi.setName("rescaledRoi");
		return roi;
	}
}
