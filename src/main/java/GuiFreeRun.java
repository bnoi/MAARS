import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import edu.univ_tlse3.display.SOCVisualizer;
import edu.univ_tlse3.maars.MAARSNoAcq;
import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.utils.IOUtils;
import fiji.plugin.trackmate.*;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.features.edges.EdgeTargetAnalyzer;
import fiji.plugin.trackmate.features.edges.EdgeTimeLocationAnalyzer;
import fiji.plugin.trackmate.features.edges.EdgeVelocityAnalyzer;
import fiji.plugin.trackmate.features.track.*;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.IJ;
import ij.ImagePlus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tongli on 28/04/2017.
 */
public class GuiFreeRun {
    public static void main(String[] args) {
        String configFileName = "/Volumes/Macintosh/curioData/102/60x/26-10-1/maars_config.xml";
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(configFileName);
        } catch (FileNotFoundException e) {
            IOUtils.printErrorToIJLog(e);
        }
        MaarsParameters parameters = new MaarsParameters(inStream);
        SetOfCells soc = new SetOfCells();
        SOCVisualizer socVisualizer = new SOCVisualizer();
        socVisualizer.createGUI(soc);
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(new MAARSNoAcq(parameters, socVisualizer, soc));
        es.shutdown();


//        TmXmlReader reader = new TmXmlReader(new File(
//                "/Volumes/Macintosh/curioData/102/60x/26-10-1/X0_Y0_FLUO/spots/1_GFP.xml"));
//        Model model = reader.getModel();
//        System.out.println(reader.getModel().set);





//        ImagePlus imp = IJ.openImage("/Volumes/Macintosh/curioData/MAARSdata/102/16-06-1/X0_Y0_FLUO/CFP_2/CFP_2_MMStack_Pos0.ome.tif");
//        imp.show();
//        Model model = new Model();
//        model.setLogger(Logger.IJ_LOGGER);
//        Settings settings = new Settings();
//        settings.setFrom(imp);
//        settings.detectorFactory = new LogDetectorFactory<>();
//        settings.detectorSettings = new HashMap<>();
//        settings.detectorSettings.put("DO_SUBPIXEL_LOCALIZATION", true);
//        settings.detectorSettings.put("RADIUS", 0.25);
//        settings.detectorSettings.put("TARGET_CHANNEL", 1);
//        settings.detectorSettings.put("THRESHOLD", 0.);
//        settings.detectorSettings.put("DO_MEDIAN_FILTERING", true);
//
//        FeatureFilter filter1 = new FeatureFilter("QUALITY", 5, true);
//        settings.addSpotFilter(filter1);
//
//        settings.trackerFactory = new SparseLAPTrackerFactory();
//        settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
//        settings.trackerSettings.put("ALLOW_TRACK_SPLITTING", true);
//        settings.trackerSettings.put("ALLOW_TRACK_MERGING", true);
//
//        settings.addTrackAnalyzer(new TrackDurationAnalyzer());
//
//        FeatureFilter filter2 = new FeatureFilter("TRACK_DISPLACEMENT", 2, true);
//        settings.addTrackFilter(filter2);
//
//        TrackMate trackmate = new TrackMate(model, settings);
//
//        Boolean ok = trackmate.checkInput();
//        if (!ok) {
//            IJ.log(trackmate.getErrorMessage());
//        }
//
//        ok = trackmate.process();
//        if (!ok) {
//            IJ.log(trackmate.getErrorMessage());
//        }
//
//        SelectionModel selectionModel = new SelectionModel(model);
//        HyperStackDisplayer displayer = new  HyperStackDisplayer(model, selectionModel, imp);
//        displayer.render();
//        displayer.refresh();

    }
}
