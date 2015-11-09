package fiji.plugin.maars.cellstateanalysis;

import java.util.HashMap;
import java.util.Map;

import org.micromanager.internal.utils.ReportingUtils;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.spot.SpotContrastAnalyzerFactory;
import fiji.plugin.trackmate.features.spot.SpotContrastAndSNRAnalyzerFactory;
import fiji.plugin.trackmate.features.spot.SpotIntensityAnalyzerFactory;
import fiji.plugin.trackmate.features.spot.SpotMorphologyAnalyzerFactory;
import fiji.plugin.trackmate.features.spot.SpotRadiusEstimatorFactory;
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
	private SpotCollection res;

	/**
	 * Constructor :
	 * 
	 * @param cell
	 *            : Cell object (the cell you want to analyse)
	 * @param spotRadius
	 *            : spot typical radius
	 */

	public CellFluoAnalysis(Cell cell, double spotRadius) {
		
		ImagePlus croppedFluoImg = cell.getCroppedFluoImage();
		Calibration cal = croppedFluoImg.getCalibration();
		Roi croppedRoi = croppedFluoImg.getRoi();
		croppedFluoImg.deleteRoi();
		
		Boolean thresholdFound = false;
		int nSpotsDetected = 0;
		double threshold = 0;
		double lowBound = 0;
		double highBound = 200;
		int maxNbSpotPerCell = cell.getMaxNbSpotPerCell();
		double stepFactor = 0.5;

		while (!thresholdFound){
			final Model model = new Model();
			Settings settings = new Settings();
			settings.setFrom(croppedFluoImg);
			settings.addSpotAnalyzerFactory(new SpotRadiusEstimatorFactory());
			settings.addSpotAnalyzerFactory(new SpotContrastAnalyzerFactory());
			settings.addSpotAnalyzerFactory(new SpotContrastAndSNRAnalyzerFactory());
			settings.addSpotAnalyzerFactory(new SpotIntensityAnalyzerFactory());
			settings.addSpotAnalyzerFactory(new SpotMorphologyAnalyzerFactory());
			
	
			settings.detectorFactory = new LogDetectorFactory();
			Map<String, Object> detectorSettings = new HashMap<String, Object>();
			detectorSettings.put("DO_SUBPIXEL_LOCALIZATION", true);
			detectorSettings.put("RADIUS", spotRadius);
			detectorSettings.put("TARGET_CHANNEL", 1);
			detectorSettings.put("THRESHOLD", threshold);
			detectorSettings.put("DO_MEDIAN_FILTERING", false);
			settings.detectorSettings = detectorSettings;
			
			TrackMate trackmate = new TrackMate(model, settings);
			ReportingUtils.logMessage("Trackmate created");

			trackmate.execDetection();
			ReportingUtils.logMessage("execDetection done");
			
			trackmate.execInitialSpotFiltering();
			ReportingUtils.logMessage("execInitialSpotFiltering done");
	
			trackmate.computeSpotFeatures(true);
			ReportingUtils.logMessage("computeSpotFeatures done");
	
			trackmate.execSpotFiltering(true);
			ReportingUtils.logMessage("execSpotFiltering done");
			nSpotsDetected = trackmate.getModel().getSpots().getNSpots(true);
			ReportingUtils.logMessage("Found " + nSpotsDetected + " spots in total");
			ReportingUtils.logMessage("Threshold = " + threshold +", LowBound = " + lowBound + ", HighBound = " + highBound);
			res = trackmate.getModel().getSpots();
			
//			res = new ArrayList<Spot>();
//			for (Spot spot : trackmate.getModel().getSpots().iterable(true)) {
//
//				Map<String, Double> features = spot.getFeatures();
//				if (croppedRoi.contains((int) Math.round(features.get("POSITION_X")/cal.pixelWidth),
//										(int) Math.round(features.get("POSITION_Y")/cal.pixelHeight))){
//					if (res.size() > maxNbSpotPerCell ){
//						break;
//					}else{
//						res.add(spot);
//					}
//				}
//			};
//			ReportingUtils.logMessage("Found " + res.size() + " spots inside the cell");
//			
//			
//			if (res.size() == 0){
//				highBound = threshold;
//				threshold = lowBound + ((highBound - lowBound) * stepFactor);
//			}else if(res.size() > maxNbSpotPerCell){
//				lowBound = threshold;
//				threshold = lowBound + ((highBound - lowBound) * stepFactor);
//				res = null;
//			}else{
//				thresholdFound = true;
//			}
		}
		ReportingUtils.logMessage("- Done.");
	}

	/**
	 * Method to get detected spots.
	 */
	public SpotCollection getSpots() {
		return res;
	}

}
