package fiji.plugin.maars.cellstateanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.micromanager.utils.ReportingUtils;

import fiji.plugin.trackmate.FeatureModel;
import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.spot.SpotContrastAndSNRAnalyzerFactory;
import fiji.plugin.trackmate.features.spot.SpotIntensityAnalyzerFactory;
import fiji.plugin.trackmate.features.track.TrackSpeedStatisticsAnalyzer;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.oldlap.SimpleFastLAPTrackerFactory;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;

//import net.imglib2.Iterator;
//import net.imglib2.img.ImagePlusAdapter;
//import net.imglib2.img.basictypeaccess.array.ShortArray;
//import net.imglib2.img.display.imagej.ImageJFunctions;
//import net.imglib2.iterator.IntervalIterator;
//import ij.IJ;
//import ij.ImagePlus;
//import ij.gui.OvalRoi;
//import ij.gui.Roi;
//import ij.measure.*;
//import ij.ImageStack;
//import ij.plugin.RoiScaler;
//import ij.plugin.filter.Analyzer;

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
	private Model model;
	private Settings settings;
	private TrackMate trackMate;

	/**
	 * Constructor :
	 * 
	 * @param cell
	 *            : Cell object (the cell you want to analyse)
	 * @param spotRadius
	 *            : spot typical radius
	 */

	public CellFluoAnalysis(Cell cell, double spotRadius) {

		ReportingUtils.logMessage("Instantiate model object ...");
		model = new Model();
		// this.cellShapeRoi = cellShapeRoi;
		// this.scaleFactorForRoiFromBfToFluo = scaleFactorForRoiFromBfToFluo;
		this.cell = cell;
		// ResultsTable rt = new ResultsTable();

		// RoiScaler.scale(cellShapeRoi, scaleFactorForRoiFromBfToFluo[0],
		// scaleFactorForRoiFromBfToFluo[1], false);
//		ReportingUtils.logMessage("Set logger ...");
//		model.setLogger(Logger.IJ_LOGGER);

		settings = new Settings();
		ReportingUtils.logMessage("Prepare settings object ...");

		settings.setFrom(cell.getFluoImage());

		ReportingUtils.logMessage("Configure detector ...");
		settings.detectorFactory = new LogDetectorFactory<UnsignedShortType>();
		HashMap<String, Object> mapToSetting = new HashMap<String, Object>();
		mapToSetting.put(DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, true);
		mapToSetting.put(DetectorKeys.KEY_RADIUS, spotRadius);
		mapToSetting.put(DetectorKeys.KEY_TARGET_CHANNEL, 1);
		mapToSetting.put(DetectorKeys.KEY_THRESHOLD, 5.);
		mapToSetting.put(DetectorKeys.KEY_DO_MEDIAN_FILTERING, false);
		settings.detectorSettings = mapToSetting;

		ReportingUtils.logMessage("Configure tracker ... ");
		settings.trackerFactory = new SimpleFastLAPTrackerFactory();
		settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
		settings.trackerSettings.put("LINKING_MAX_DISTANCE", 10.0);
		settings.trackerSettings.put("GAP_CLOSING_MAX_DISTANCE", 10.0);
		settings.trackerSettings.put("MAX_FRAME_GAP", 3);

		settings.addSpotAnalyzerFactory(new SpotIntensityAnalyzerFactory<UnsignedShortType>());
		settings.addSpotAnalyzerFactory(new SpotContrastAndSNRAnalyzerFactory<UnsignedShortType>());
		settings.addTrackAnalyzer(new TrackSpeedStatisticsAnalyzer());

		settings.initialSpotFilterValue = 1.;

		ReportingUtils.logMessage(settings.toString());

		trackMate = new TrackMate(model, settings);

		trackMate.process();

		ReportingUtils.logMessage(
				"Found " + model.getTrackModel().nTracks(true) + " tracks.");

		SelectionModel selectionModel = new SelectionModel(model);
		HyperStackDisplayer displayer = new HyperStackDisplayer(model,
				selectionModel, cell.getFluoImage());
		displayer.render();
		displayer.refresh();

		factorForThreshold = 4;
		FeatureModel fm = model.getFeatureModel();
		Iterator<Integer> trackIterator = model.getTrackModel().trackIDs(true)
				.iterator();
		while (trackIterator.hasNext()) {
			Integer id = trackIterator.next();
			Double v = fm.getTrackFeature(id, "TRACK_MEAN_SPEED");
			model.getLogger().log("");
			model.getLogger().log(
					"Track " + id + ": mean velocity = " + v + " "
							+ model.getSpaceUnits() + '/'
							+ model.getTimeUnits());
			Iterator<Spot> spotIterator = model.getTrackModel().trackSpots(id)
					.iterator();
			while (spotIterator.hasNext()) {
				res.add(spotIterator.next());
			}
		}

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
		Iterator<Spot> itr1 = res.iterator();

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

		Iterator<Spot> itr2 = res.iterator();
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
