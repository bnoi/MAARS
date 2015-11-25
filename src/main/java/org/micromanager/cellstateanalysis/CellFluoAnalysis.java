package org.micromanager.cellstateanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.type.numeric.real.FloatType;

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
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_DO_MEDIAN_FILTERING;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_RADIUS;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_THRESHOLD;
import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_TARGET_CHANNEL;
import fiji.plugin.trackmate.TrackMate;
import ij.ImagePlus;

/**
 * This class is to find fluorescent spots in an image using LogDetector
 * 
 * @author Tong LI
 *
 */
public class CellFluoAnalysis {

	private Cell cell;
	private Model model;
	private Settings settings;
	private CellChannelFactory factory;
	private SpotCollection tmpCollection;

	/**
	 * Constructor : prepare parameters for trackmate : model and setting
	 * 
	 * @param cell
	 * @param factory
	 */

	public CellFluoAnalysis(Cell cell, CellChannelFactory factory) {

		this.cell = cell;
		this.factory = factory;
		ImagePlus fluoImg = cell.getFluoImage();
		fluoImg.deleteRoi();
		model = new Model();

		settings = new Settings();
		settings.setFrom(fluoImg);

		// Computer different features (in order)

		settings.addSpotAnalyzerFactory(new SpotRadiusEstimatorFactory<FloatType>());
		settings.addSpotAnalyzerFactory(new SpotContrastAnalyzerFactory<FloatType>());
		settings.addSpotAnalyzerFactory(new SpotIntensityAnalyzerFactory<FloatType>());
		settings.addSpotAnalyzerFactory(new SpotMorphologyAnalyzerFactory<FloatType>());
		settings.addSpotAnalyzerFactory(new SpotContrastAndSNRAnalyzerFactory<FloatType>());

		// Set up detectino parameters.

		settings.detectorFactory = new LogDetectorFactory<FloatType>();
		Map<String, Object> detectorSettings = new HashMap<String, Object>();
		detectorSettings.put(KEY_DO_SUBPIXEL_LOCALIZATION, true);
		detectorSettings.put(KEY_RADIUS, factory.getSpotRadius());
		detectorSettings.put(KEY_TARGET_CHANNEL, DEFAULT_TARGET_CHANNEL);
		// TODO to figure out what value to use, 2 seems ok for now.
		detectorSettings.put(KEY_THRESHOLD, (double) 2);
		detectorSettings.put(KEY_DO_MEDIAN_FILTERING, true);
		settings.detectorSettings = detectorSettings;

	}

	/**
	 * Take parameters in the constructor then initalize trakemate object to get
	 * unfiltered spots.
	 */
	public void doDetection() {
		int nSpotsDetected = 0;
		TrackMate trackmate = new TrackMate(model, settings);

		trackmate.execDetection();

		trackmate.execInitialSpotFiltering();

		trackmate.computeSpotFeatures(true);

		trackmate.execSpotFiltering(true);

		nSpotsDetected = trackmate.getModel().getSpots().getNSpots(true);
		ReportingUtils
				.logMessage("Found " + nSpotsDetected + " spots in total");
		tmpCollection = trackmate.getModel().getSpots();
		trackmate = null;
		ReportingUtils.logMessage("- Done.");
	}

	/**
	 * discard all the spots out of cell shape roi
	 * 
	 * @param currentFrame
	 * @param visibleOnly
	 *            : iterate only on visible cells or not (see @SpotCollection)
	 */
	public void filterOnlyInCell(Boolean visibleOnly) {
		SpotCollection newCollection = new SpotCollection();
		for (Spot s : tmpCollection.iterable(visibleOnly)) {
			if (cell.croppedRoiContains(s)) {
				newCollection.add(s, 0);
			}
		}
		tmpCollection = newCollection;
	}

	/**
	 * if number of spot still higher than expected number of spot in the
	 * channel get the n highest quality spot(s)
	 * 
	 * @param visibleOnly
	 */
	public void findBestNSpotInCell(Boolean visibleOnly) {
		Spot tmpSpot = null;
		ArrayList<Integer> idToSkip = new ArrayList<Integer>();
		SpotCollection newCollection = new SpotCollection();
		if (tmpCollection.getNSpots(visibleOnly) > factory.getMaxNbSpot()) {
			ReportingUtils.logMessage("Found more spot than waiting number");
			for (int i = 0; i < factory.getMaxNbSpot(); i++) {
				for (Spot s : tmpCollection.iterable(visibleOnly)) {
					if (tmpSpot == null) {
						tmpSpot = s;
					} else {
						if (Spot.featureComparator("QUALITY").compare(s,
								tmpSpot) == 1
								&& !idToSkip.contains(s.ID())) {
							tmpSpot = s;
						}
					}
				}
				newCollection.add(tmpSpot, 0);
				if (tmpSpot != null) {
					idToSkip.add(tmpSpot.ID());
				}
				tmpSpot = null;
			}
			tmpCollection = newCollection;
			idToSkip = null;
			newCollection = null;
		} else {
			// do nothing
		}
	}

	public Model getModel() {
		model.setSpots(tmpCollection, true);
		return model;
	}

}
