package edu.univ_tlse3.cellstateanalysis;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.spot.*;
import ij.ImagePlus;
import net.imglib2.type.numeric.real.FloatType;

import java.util.HashMap;
import java.util.Map;

import static fiji.plugin.trackmate.detection.DetectorKeys.*;

public class MaarsTrackmate {

   private Settings settings;

   public MaarsTrackmate(ImagePlus img, double radius, double quality) {
      settings = new Settings();
      settings.setFrom(img);

      // Computer different features (in order)

      settings.addSpotAnalyzerFactory(new SpotIntensityAnalyzerFactory<FloatType>());
      settings.addSpotAnalyzerFactory(new SpotContrastAndSNRAnalyzerFactory<FloatType>());
      settings.addSpotAnalyzerFactory(new SpotMorphologyAnalyzerFactory<FloatType>());
      settings.addSpotAnalyzerFactory(new SpotRadiusEstimatorFactory<FloatType>());
      settings.addSpotAnalyzerFactory(new SpotContrastAnalyzerFactory<FloatType>());

      // Set up detection parameters.

      settings.detectorFactory = new LogDetectorFactory<FloatType>();
      Map<String, Object> detectorSettings = new HashMap<>();
      detectorSettings.put(KEY_DO_SUBPIXEL_LOCALIZATION, true);
      detectorSettings.put(KEY_RADIUS, radius);
      detectorSettings.put(KEY_TARGET_CHANNEL, DEFAULT_TARGET_CHANNEL);
      detectorSettings.put(KEY_THRESHOLD, quality);
      detectorSettings.put(KEY_DO_MEDIAN_FILTERING, true);
      settings.detectorSettings = detectorSettings;
   }

   /**
    * Take parameters in the constructor then initalize trakemate object to get
    * unfiltered spots.
    *
    * @param computeFeatures to compute or not features of spots
    * @return Model a Trackmate style data structure
    */
   public Model doDetection(boolean computeFeatures) {
      TrackMate trackmate = new TrackMate(settings);

      trackmate.execDetection();

      trackmate.execInitialSpotFiltering();

      if (computeFeatures) {
         trackmate.computeSpotFeatures(false);
      }

      return trackmate.getModel();
   }
}
