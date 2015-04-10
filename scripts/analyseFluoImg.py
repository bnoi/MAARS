

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
from fiji.plugin.trackmate import Model
from fiji.plugin.trackmate import Settings
from fiji.plugin.trackmate import TrackMate
from fiji.plugin.trackmate import SelectionModel
from fiji.plugin.trackmate import Logger
from fiji.plugin.trackmate.detection import LogDetectorFactory
from fiji.plugin.trackmate.tracking.sparselap import SparseLAPTrackerFactory
from fiji.plugin.trackmate.tracking import LAPUtils
from ij import IJ
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer as HyperStackDisplayer
import fiji.plugin.trackmate.features.FeatureFilter as FeatureFilter
import sys
import fiji.plugin.trackmate.features.track.TrackDurationAnalyzer as TrackDurationAnalyzer
from ij.plugin import ZProjector;
from ij.measure import Calibration

imp = IJ.openImage('/Users/theoli89/Desktop/curioData/single_field/102_3/movie_X0_Y0_FLUO/0/MMStack_Pos0.ome.tif')
#imp.show()

projector = ZProjector();
projector.setMethod(ZProjector.MAX_METHOD);
projector.setImage(imp);
projector.doProjection();
zProjectField = projector.getProjection();
zProjectField.show()

#print("- Get fluo image calibration");
cal = imp.getCalibration();
#print(cal)
model = Model();

settings = Settings();
settings.setFrom(imp);

settings.detectorFactory = LogDetectorFactory();
settings.detectorFactory = LogDetectorFactory()
settings.detectorSettings = { 
    'DO_SUBPIXEL_LOCALIZATION' : True,
    'RADIUS' : 2.5,
    'TARGET_CHANNEL' : 1,
    'THRESHOLD' : 0.,
    'DO_MEDIAN_FILTERING' : False,
}  
filter1 = FeatureFilter('QUALITY', 1, True)
settings.addSpotFilter(filter1)

trackmate = TrackMate(model, settings)
print("Trackmate created");

trackmate.execDetection();
print("execDetection done");

trackmate.execInitialSpotFiltering();
print("execInitialSpotFiltering done");

trackmate.computeSpotFeatures(True);
print("computeSpotFeatures done");

trackmate.execSpotFiltering(True);
print("execSpotFiltering done");

print("- get results");

nSpots = trackmate.getModel().getSpots().getNSpots(False);
print("Found " + nSpots + " spots");
imp.close();