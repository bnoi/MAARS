package fiji.plugin.maars.cellstateanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.micromanager.utils.ReportingUtils;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.detection.DetectionUtils;
import fiji.plugin.trackmate.detection.DogDetector;
import fiji.plugin.trackmate.detection.LogDetector;
import fiji.plugin.trackmate.util.TMUtils;
import net.imglib2.Interval;
import net.imglib2.Iterator;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.img.Img;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.meta.ImgPlus;
import net.imglib2.FinalInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.RoiScaler;
import ij.plugin.filter.Analyzer;

/**
 * This class is to find fluorescent spots in an image using LogDetector
 * 
 * @author marie
 *
 */
public class CellFluoAnalysis {
	// private double[] scaleFactorForRoiFromBfToFluo;
	private Cell cell;
	private java.util.List<Spot> res;
	private double factorForThreshold;

	// TODO

	/**
	 * Constructor :
	 * 
	 * @param cell
	 *            : Cell object (the cell you want to analyse)
	 * @param spotRadius
	 *            : spot typical radius
	 */

	public CellFluoAnalysis(Cell cell, double spotRadius) {

		ReportingUtils.logMessage("Creating CellFluoAnalysis object ...");

		// this.cellShapeRoi = cellShapeRoi;
		// this.scaleFactorForRoiFromBfToFluo = scaleFactorForRoiFromBfToFluo;
		this.cell = cell;
		// ResultsTable rt = new ResultsTable();

		// RoiScaler.scale(cellShapeRoi, scaleFactorForRoiFromBfToFluo[0],
		// scaleFactorForRoiFromBfToFluo[1], false);

		ReportingUtils
				.logMessage("- change image type so it can be used by spot analyzer");

		final Img<UnsignedShortType> img = ImageJFunctions.wrap(cell.getFluoImage());
		ReportingUtils.logMessage("- Done.");
		
		ReportingUtils.logMessage("- Get fluo image calibration");
		Calibration cal = cell.getFluoImage().getCalibration();
		ReportingUtils.logMessage("- Initiate interval");
				
		 long[] min = new long[img.numDimensions()];
		 long[] max = new long[img.numDimensions()];

		final net.imagej.ImgPlus imgPlus = TMUtils.rawWraps(cell.getFluoImage());
		 
		final int xindex = TMUtils.findXAxisIndex(imgPlus);
		min[xindex] = 0;
		max[xindex] = (long) (2000 * 0.0645);
		
		final int yindex = TMUtils.findYAxisIndex(imgPlus);
		min[yindex] = 0;
		max[yindex] = (long) (2000 * 0.0645);
		
		final int zindex = TMUtils.findZAxisIndex(imgPlus);
		min[zindex] = 0;
		max[zindex] = 23;

		FinalInterval interval = new FinalInterval(min, max);
		
		ReportingUtils.logMessage(xindex + "");
		ReportingUtils.logMessage(zindex + "");
		
		ReportingUtils.logMessage(img.numDimensions() + "");
		// TODO
		double[] calib = { 0.0645, 0.0645, 0.3 };
		ReportingUtils.logMessage("- create detector");
		final LogDetector<UnsignedShortType> detector = new LogDetector<UnsignedShortType>(
				img, interval, calib, spotRadius / cal.pixelWidth, 0.0, false,
				true);
		// final DogDetector<UnsignedShortType> detector = new
		// DogDetector<UnsignedShortType>(imgPlus, 0.15/cal.pixelWidth,
		// 0.05/cal.pixelWidth, false, true);
		ReportingUtils.logMessage("- done");

		if (!detector.checkInput()) {
			ReportingUtils.logMessage("- Wrong input for detector");
		} else {
			ReportingUtils.logMessage("- input ok");
		}

		final long start = System.currentTimeMillis();
		/*
		 * Copy to float for convolution.
		 */
		final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(
				interval, new FloatType());
		ReportingUtils.logMessage("salutddd");

		Img<FloatType> floatImg = DetectionUtils.copyToFloatImg(img, interval,
				factory);
		ReportingUtils.logMessage("salut");
		// /*
		// * Do median filtering (or not).
		// */
		// if (doMedianFilter) {
		// floatImg = DetectionUtils.applyMedianFilter(floatImg);
		// if (null == floatImg) {
		// errorMessage = BASE_ERROR_MESSAGE
		// + "Failed to apply median filter.";
		// return false;
		// }
		// }
		// int ndims = interval.numDimensions();
		// for (int d = 0; d < interval.numDimensions(); d++) {
		// // Squeeze singleton dimensions
		// if (interval.dimension(d) <= 1) {
		// ndims--;
		// }
		// }

		if (!detector.process()) {
			ReportingUtils.logMessage("- Detector not processing");
		} else {
			ReportingUtils.logMessage("- process ok");
		}
		ReportingUtils.logMessage("- compute results");
		res = detector.getResult();
		ReportingUtils.logMessage("- Done.");

		factorForThreshold = 4;
	}

	/**
	 * Method to change threshold if necessary
	 * 
	 * @param fact
	 */
	public void setFactorForThreshold(double fact) {
		factorForThreshold = fact;
	}

	/**
	 * Method to find spots
	 * 
	 * @return ArrayList<Spot>
	 */
	public ArrayList<Spot> findSpots() {

		ArrayList<Spot> spotsToKeep = new ArrayList<Spot>();

		// ReportingUtils.logMessage("Res : "+res);
		java.util.Iterator<Spot> itr1 = res.iterator();

		double[] quality = new double[res.toArray().length];
		int nb = 0;
		while (itr1.hasNext()) {
			Spot spot = itr1.next();
			/*
			 * ReportingUtils.logMessage("\n___\n");
			 * ReportingUtils.logMessage("spot : "+spot.getName());
			 * ReportingUtils.logMessage(spot.getFeatures());
			 */
			Map<String, Double> features = spot.getFeatures();
			quality[nb] = features.get("QUALITY");
			nb++;
			/*
			 * OvalRoi roi = new OvalRoi(features.get("POSITION_X"),
			 * features.get("POSITION_Y"), 2* features.get("RADIUS"), 2*
			 * features.get("RADIUS")); fluoImage.setSlice((int)
			 * Math.round(features.get("POSITION_Z"))); fluoImage.setRoi(roi);
			 * Analyzer a = new
			 * Analyzer(fluoImage,Measurements.CENTROID+Measurements.MEAN ,rt);
			 * a.measure();
			 */
		}

		ReportingUtils.logMessage("initial number of spots : " + nb);

		Statistics stat = new Statistics(quality);
		double threshold = stat.getMean() + factorForThreshold
				* stat.getStdDev();

		ReportingUtils.logMessage("threshold : " + threshold);

		java.util.Iterator<Spot> itr2 = res.iterator();
		while (itr2.hasNext()) {
			Spot spot = itr2.next();
			Map<String, Double> features = spot.getFeatures();

			if (features.get("QUALITY") > threshold
					&& cell.getCellShapeRoi().contains(
							(int) Math.round(cell.getCellShapeRoi().getXBase()
									+ features.get("POSITION_X")),
							(int) Math.round(cell.getCellShapeRoi().getYBase()
									+ features.get("POSITION_Y")))) {
				spotsToKeep.add(spot);
				// ReportingUtils.logMessage(features);
				/*
				 * OvalRoi roi = new OvalRoi(features.get("POSITION_X"),
				 * features.get("POSITION_Y"), features.get("RADIUS"),
				 * features.get("RADIUS")); cell.getFluoImage().setSlice((int)
				 * Math.round(features.get("POSITION_Z"))+1);
				 * cell.getFluoImage().setRoi(roi); IJ.wait(5000);
				 */
			}
		}
		return spotsToKeep;
		// rt.show("Measures");
	}

}
