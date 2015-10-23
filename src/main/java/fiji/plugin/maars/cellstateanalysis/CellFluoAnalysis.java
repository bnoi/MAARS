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
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.gui.Roi;

/**
 * This class is to find fluorescent spots in an image using LogDetector
 * 
 * @author marie
 *
 */
public class CellFluoAnalysis {
	private ArrayList<Spot> res;

	/**
	 * Constructor :
	 * 
	 * @param cell
	 *            : Cell object (the cell you want to analyse)
	 * @param spotRadius
	 *            : spot typical radius
	 */

	public CellFluoAnalysis(Cell cell, double spotRadius) throws InterruptedException {
		
		ImagePlus fluoCellImg = cell.getFluoImage();
		Calibration cal = fluoCellImg.getCalibration();
		Roi cellShape = fluoCellImg.getRoi();
		fluoCellImg.deleteRoi();
		Boolean thresholdFound = false;
		int nSpotsDetected = 0;
		double threshold = 0;
		double lowBound = 0;
		double highBound = 200;
		int maxNbSpotPerCell = cell.getMaxNbSpotPerCell();
		double stepFactor = 0.5;

		while (!thresholdFound){
			Model model = new Model();
			Settings settings = new Settings();
			settings.setFrom(fluoCellImg);
	
			settings.detectorFactory = new LogDetectorFactory();
			Map<String, Object> detectorSettings = new HashMap<String, Object>();
			detectorSettings.put("DO_SUBPIXEL_LOCALIZATION", true);
			detectorSettings.put("RADIUS", spotRadius);
			detectorSettings.put("TARGET_CHANNEL", 1);
			detectorSettings.put("THRESHOLD", threshold);
			detectorSettings.put("DO_MEDIAN_FILTERING", false);
			settings.detectorSettings = detectorSettings;
	
			FeatureFilter filter1 = new FeatureFilter("QUALITY", 1, true);
			settings.addSpotFilter(filter1);
	
			TrackMate trackmate = new TrackMate(model, settings);
			ReportingUtils.logMessage("Trackmate created");

			trackmate.execDetection();
			ReportingUtils.logMessage("execDetection done");
			
			trackmate.execInitialSpotFiltering();
			ReportingUtils.logMessage("execInitialSpotFiltering done");
	
			trackmate.computeSpotFeatures(false);
			ReportingUtils.logMessage("computeSpotFeatures done");
	
			trackmate.execSpotFiltering(true);
			ReportingUtils.logMessage("execSpotFiltering done");
			nSpotsDetected = trackmate.getModel().getSpots().getNSpots(true);
			ReportingUtils.logMessage("Found " + nSpotsDetected + " spots in total");
			ReportingUtils.logMessage("Threshold = " + threshold +", LowBound = " + lowBound + ", HighBound = " + highBound);
			
			res = new ArrayList<Spot>();
			for (Spot spot : trackmate.getModel().getSpots().iterable(true)) {
				Map<String, Double> features = spot.getFeatures();
				if (cellShape.contains(	(int) Math.round(features.get("POSITION_X")/cal.pixelWidth),
										(int) Math.round(features.get("POSITION_Y")/cal.pixelHeight)))
				{
					if (res.size() > maxNbSpotPerCell ){
						break;
					}else{
						res.add(spot);
					}
				}
			};
			ReportingUtils.logMessage("Found " + res.size() + " spots inside the cell");
			if (res.size() == 0){
				highBound = threshold;
				threshold = lowBound + ((highBound - lowBound) * stepFactor);
			}else if(res.size() > maxNbSpotPerCell){
				lowBound = threshold;
				threshold = lowBound + ((highBound - lowBound) * stepFactor);
				res = new ArrayList<Spot>();
			}else{
				thresholdFound = true;
			}
		}
		ReportingUtils.logMessage("- Done.");
		//TODO filter factor (between 3 and 4)
//		factorForThreshold = 3.5;
	}

	/**
	 * Method to change threshold if necessary
	 * 
	 * @param fact
	 */
	public ArrayList<Spot> getSpots() {
		return res;
	}
	
//	/**
//	 * Method to find spots
//	 * 
//	 * @return ArrayList<Spot>
//	 */
//	public ArrayList<Spot> findSpots() {
//
//		Calibration cal = cell.getFluoImage().getCalibration();
//		ArrayList<Spot> spotsToKeep = new ArrayList<Spot>();
//
//		java.util.Iterator<Spot> itr1 = res.iterator();
//
//		double[] quality = new double[res.toArray().length];
//		int nb = 0;
//		while (itr1.hasNext()) {
//			Spot spot = itr1.next();
//			Map<String, Double> features = spot.getFeatures();
//			quality[nb] = features.get("QUALITY");
//			nb++;
//			/*
//			 * OvalRoi roi = new OvalRoi(features.get("POSITION_X"),
//			 * features.get("POSITION_Y"), 2* features.get("RADIUS"), 2*
//			 * features.get("RADIUS")); fluoImage.setSlice((int)
//			 * Math.round(features.get("POSITION_Z"))); fluoImage.setRoi(roi);
//			 * Analyzer a = new
//			 * Analyzer(fluoImage,Measurements.CENTROID+Measurements.MEAN ,rt);
//			 * a.measure();
//			 */
//		}
//
//		Statistics stat = new Statistics(quality);
//		double threshold = stat.getMean() + factorForThreshold
//				* stat.getStdDev();
//
//		ReportingUtils.logMessage("threshold for spot filter: " + threshold);
//
//		java.util.Iterator<Spot> itr2 = res.iterator();
//		while (itr2.hasNext()) {
//			Spot spot = itr2.next();
//			Map<String, Double> features = spot.getFeatures();
//
//			if (features.get("QUALITY") > threshold
//					&& cell.getCellShapeRoi().contains(
//							(int) Math.round(features.get("POSITION_X")/cal.pixelWidth),
//							(int) Math.round(features.get("POSITION_Y")/cal.pixelHeight))) {
//				spotsToKeep.add(spot);
//				// ReportingUtils.logMessage(features);
//				/*
//				 * OvalRoi roi = new OvalRoi(features.get("POSITION_X"),
//				 * features.get("POSITION_Y"), features.get("RADIUS"),
//				 * features.get("RADIUS")); cell.getFluoImage().setSlice((int)
//				 * Math.round(features.get("POSITION_Z"))+1);
//				 * cell.getFluoImage().setRoi(roi); IJ.wait(5000);
//				 */
//			}
//
//		}
//		res = null;
//		return spotsToKeep;
//		// rt.show("Measures");
//	}

}
