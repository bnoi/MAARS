package fiji.plugin.maars.cellstateanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.micromanager.utils.ReportingUtils;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.TrackMate;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.ZProjector;

/**
 * This class is to find fluorescent spots in an image using LogDetector
 * 
 * @author marie
 *
 */
public class CellFluoAnalysis {
	private Cell cell;
	private java.util.List<Spot> res;
	private double factorForThreshold;

	/**
	 * Constructor :
	 * 
	 * @param cell
	 *            : Cell object (the cell you want to analyse)
	 * @param spotRadius
	 *            : spot typical radius
	 */

	public CellFluoAnalysis(Cell cell, double spotRadius) throws InterruptedException {
		this.cell = cell;
		
		ZProjector projector = new ZProjector();
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.setImage(cell.getFluoImage());
		projector.doProjection();
		ImagePlus zProjectField = projector.getProjection();

		zProjectField.setCalibration(cell.getFluoImage().getCalibration());
		
		Model model = new Model();
		Settings settings = new Settings();
		settings.setFrom(zProjectField);

		settings.detectorFactory = new LogDetectorFactory();
		Map<String, Object> detectorSettings = new HashMap<String, Object>();
		detectorSettings.put("DO_SUBPIXEL_LOCALIZATION", true);
		detectorSettings.put("RADIUS", spotRadius);
		detectorSettings.put("TARGET_CHANNEL", 1);
		detectorSettings.put("THRESHOLD", 0.);
		detectorSettings.put("DO_MEDIAN_FILTERING", false);
		settings.detectorSettings = detectorSettings;

		FeatureFilter filter1 = new FeatureFilter("QUALITY", 1, true);
		settings.addSpotFilter(filter1);

		TrackMate trackmate = new TrackMate(model, settings);
		ReportingUtils.logMessage("Trackmate created");
		//TODO
		trackmate.execDetection();
		ReportingUtils.logMessage("execDetection done");
		
		trackmate.execInitialSpotFiltering();
		ReportingUtils.logMessage("execInitialSpotFiltering done");

		trackmate.computeSpotFeatures(false);
		ReportingUtils.logMessage("computeSpotFeatures done");

		trackmate.execSpotFiltering(false);
		ReportingUtils.logMessage("execSpotFiltering done");
		int nSpots = trackmate.getModel().getSpots().getNSpots(true);
		ReportingUtils.logMessage("Found " + nSpots + " spots");

		res = new ArrayList<Spot>();
		for (Spot spot : trackmate.getModel().getSpots().iterable(true)) {
			res.add(spot);
		}
		ReportingUtils.logMessage("- Done.");
		projector = null;
		settings= null;
		detectorSettings= null;
		trackmate= null;
		filter1 = null;
		model = null;
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

		Calibration cal = cell.getFluoImage().getCalibration();
		ArrayList<Spot> spotsToKeep = new ArrayList<Spot>();

//		ReportingUtils.logMessage("Res : " + res);
		java.util.Iterator<Spot> itr1 = res.iterator();

		double[] quality = new double[res.toArray().length];
		int nb = 0;
		while (itr1.hasNext()) {
			Spot spot = itr1.next();
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

//		ReportingUtils.logMessage("initial number of spots : " + nb);

		Statistics stat = new Statistics(quality);
		double threshold = stat.getMean() + factorForThreshold
				* stat.getStdDev();

//		ReportingUtils.logMessage("threshold : " + threshold);

		java.util.Iterator<Spot> itr2 = res.iterator();
		while (itr2.hasNext()) {
			Spot spot = itr2.next();
			Map<String, Double> features = spot.getFeatures();

			if (features.get("QUALITY") > threshold
					&& cell.getCellShapeRoi().contains(
							(int) Math.round(features.get("POSITION_X")/cal.pixelWidth),
							(int) Math.round(features.get("POSITION_Y")/cal.pixelHeight))) {
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
		res = null;
		return spotsToKeep;
		// rt.show("Measures");
	}

}
