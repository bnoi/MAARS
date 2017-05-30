
import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import edu.univ_tlse3.display.SOCVisualizer;
import edu.univ_tlse3.gui.MaarsFluoAnalysisDialog;
import edu.univ_tlse3.gui.MaarsSegmentationDialog;
import edu.univ_tlse3.maars.MAARS;
import edu.univ_tlse3.maars.MAARSNoAcq;
import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.maars.MaarsSegmentation;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.IOUtils;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The main of MAARS without GUI configuration of MAARSparameters
 * Created by tongli on 28/04/2017.
 */
public class GuiFreeRun implements PlugIn {
   private static MaarsParameters loadMaarsParameters(String configFileName, String rootDir) {
      if (rootDir == null) {
         JFileChooser chooser = new JFileChooser();
         chooser.setCurrentDirectory(new File("."));
         chooser.setDialogTitle("Directory of MAARS folder");
         chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            rootDir = String.valueOf(chooser.getSelectedFile());
         } else {
            IJ.error("No folder Selected");
         }
      }
      MaarsParameters parameters;
      InputStream inStream;
      if (FileUtils.exists(rootDir + File.separator + configFileName)) {
         try {
            inStream = new FileInputStream(rootDir + File.separator + configFileName);
            parameters = new MaarsParameters(inStream);
            return parameters;
         } catch (FileNotFoundException e) {
            IOUtils.printErrorToIJLog(e);
         }
         return null;

      } else {
         inStream = FileUtils.getInputStreamOfScript("maars_default_config.xml");
         parameters = new MaarsParameters(inStream);
         parameters.setSavingPath(rootDir);
         new MaarsSegmentationDialog(parameters, null);
         runSegmentation(parameters);
         MaarsFluoAnalysisDialog fluoAnalysisDialog = new MaarsFluoAnalysisDialog(parameters);
         return fluoAnalysisDialog.getParameters();
      }
   }

   private static MaarsParameters loadMaarsParameters(String configFileName) {
      return loadMaarsParameters(configFileName, null);
   }

   private static void executeAnalysis(MaarsParameters parameters) {
      SetOfCells soc = new SetOfCells(0);
      SOCVisualizer socVisualizer = new SOCVisualizer();
      socVisualizer.createGUI(soc);
      ExecutorService es = Executors.newSingleThreadExecutor();
      es.execute(new MAARSNoAcq(parameters, socVisualizer, soc));
      es.shutdown();
      try {
         es.awaitTermination(20, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }

   public static void runSegmentation(MaarsParameters parameters) {
      String defaultBfFolderName = "BF_1";
      String originalFolder = parameters.getSavingPath();
      String segDir = parameters.getSavingPath() + File.separator + defaultBfFolderName + File.separator;
      parameters.setSavingPath(segDir);
      ArrayList<String> names = FileUtils.getTiffWithPattern(segDir, ".*_MMStack_.*");
      String pathToSegMovie = FileUtils.convertPath(segDir + File.separator + names.get(0));
      MaarsSegmentation ms = new MaarsSegmentation(parameters, IJ.openImage(pathToSegMovie), 0);
      ExecutorService es = Executors.newSingleThreadExecutor();
      es.execute(ms);
      es.shutdown();
      try {
         es.awaitTermination(30, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      parameters.setSavingPath(originalFolder);
   }

   public static void main(String[] args) {
      new ImageJ();
      MAARS.copyDeps();
      String configFileName = "maars_config.xml";
      String dir = "/Volumes/Macintosh/curioData/MAARSdata/102/15-06-2";
      MaarsParameters parameters = loadMaarsParameters(configFileName, dir);
      parameters.setSavingPath(dir);
      //executeAnalysis(fluoAnalysisDialog.getParameters());
      executeAnalysis(parameters);

//        for (String s : new File(".").list()){
//            System.out.println(s);
//        }

//        //        chooser.setAcceptAllFileFilterUsed(false);
//        FileNameExtensionFilter posListFilter = new FileNameExtensionFilter(
//                "MM acquisition setting file ", "txt");
//        chooser.setFileFilter(posListFilter);

//        concatenatedImg.setProperty("Info", im.getInfoProperty());

//        JSONObject jsonObject = null;
//        try {
//            jsonObject = new JSONObject(IJ.openImage("/Volumes/Macintosh/curioData/new_format/27C_3_MMStack_102_1.ome.tif",1).getInfoProperty());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        ArrayList<String> arrayChannels = new ArrayList<>();
//        try {
//
//            for (int i=0; i<jsonObject.getJSONArray("ChNames").length(); i++){
//                arrayChannels.add(jsonObject.getJSONArray("ChNames").getString(i));
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        System.out.println(arrayChannels);
////        ArrayList<String> arrayChannels = (ArrayList) map.get("ChNames");
//        int totalChannel = MAARSNoAcq.extractFromOMEmetadata(jsonObject, "channel");
//        int totalSlice = MAARSNoAcq.extractFromOMEmetadata(jsonObject, "z");
//        int totalFrame = MAARSNoAcq.extractFromOMEmetadata(jsonObject, "time");
////               totalPosition = (int) ((Map)map.get("IntendedDimensions")).get("position");
//
//        IJ.log("Re-stack image : channel " + totalChannel +", slice " + totalSlice + ", frame " + totalFrame);

//        String jsonTxt = null;
//        try {
//            InputStream is = new FileInputStream("/Volumes/Macintosh/cloudDrives/Dropbox/AcqSettings_fluo.txt");
//            jsonTxt = org.apache.commons.io.IOUtils.toString(is);
//            System.out.println(jsonTxt);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        JSONObject jsonObject = null;
//        try {
//            jsonObject = new JSONObject(jsonTxt);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        try {
//            System.out.println(((JSONObject)((JSONArray)jsonObject.get("channels")).get(0)).get("config"));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


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

   @Override
   public void run(String s) {
      String configFileName = "maars_config.xml";
      MaarsParameters parameters = loadMaarsParameters(configFileName);
      if (!Boolean.parseBoolean(parameters.getSkipSegmentation())) {
         runSegmentation(parameters);
         parameters.setSkipSegmentation(!Boolean.parseBoolean(parameters.getSkipSegmentation()));
      }
      MaarsFluoAnalysisDialog fluoAnalysisDialog = new MaarsFluoAnalysisDialog(parameters);
      executeAnalysis(fluoAnalysisDialog.getParameters());
//        byte[] encoded = new byte[0];
//        try {
//            encoded = Files.readAllBytes(Paths.get("/home/tong/Desktop/new_mda/AcqSettings_bf.txt"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        PositionList pl = new PositionList();
//        try {
//            pl.load("/home/tong/Desktop/new_mda/PositionList.pos");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(pl.getPosition(0).getX());
   }
}
