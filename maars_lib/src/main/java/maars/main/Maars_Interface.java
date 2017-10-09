package maars.main;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import maars.agents.Cell;
import maars.agents.DefaultSetOfCells;
import maars.cellAnalysis.FluoAnalyzer;
import maars.cellAnalysis.PythonPipeline;
import maars.display.SOCVisualizer;
import maars.io.IOUtils;
import maars.utils.FileUtils;
import maars.utils.ImgUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by tongli on 09/06/2017.
 */
public class Maars_Interface {
   public static final String MITODIRNAME = "Mitosis";
   public final static String SEGANALYSIS_SUFFIX = "_SegAnalysis" + File.separator;
   public final static String FLUOANALYSIS_SUFFIX = "_FluoAnalysis" + File.separator;
   /**
    * @param tasksSet tasks to be terminated
    */
   public static void waitAllTaskToFinish(CopyOnWriteArrayList<Map<String, Future>> tasksSet) {
      for (Map<String, Future> aFutureSet : tasksSet) {
         for (String channel : aFutureSet.keySet()) {
            try {
               aFutureSet.get(channel).get();
            } catch (InterruptedException | ExecutionException e) {
               IOUtils.printErrorToIJLog(e);
            }
            IJ.showStatus("Terminating analysis...");
         }
      }
      IJ.log("Spot detection finished! Proceed to saving and analysis...");
   }

   private static void findAbnormalCells(String mitoDir,
                                         DefaultSetOfCells soc,
                                         HashMap map) {
      if (FileUtils.exists(mitoDir)) {
         PrintWriter out = null;
         try {
            out = new PrintWriter(mitoDir + File.separator + "abnormalCells.txt");
         } catch (FileNotFoundException e) {
            IOUtils.printErrorToIJLog(e);
         }

         for (Object cellNb : map.keySet()) {
            int cellNbInt = Integer.parseInt(String.valueOf(cellNb));
            int anaBOnsetFrame = Integer.valueOf(((String[]) map.get(cellNb))[2]);
            int lastAnaphaseFrame = Integer.valueOf(((String[]) map.get(cellNb))[3]);
            Cell cell = soc.getCell(cellNbInt);
            cell.setAnaBOnsetFrame(anaBOnsetFrame);
            ArrayList<Integer> spotInBtwnFrames = cell.getSpotInBtwnFrames();
            assert out != null;
            if (spotInBtwnFrames.size() > 0) {
               Collections.sort(spotInBtwnFrames);
               int laggingTimePoint = spotInBtwnFrames.get(spotInBtwnFrames.size() - 1);
               if (laggingTimePoint > anaBOnsetFrame && laggingTimePoint < lastAnaphaseFrame) {
                  String laggingMessage = "Lagging :" + cellNb + "_lastLaggingTimePoint_" + laggingTimePoint + "_anaBonset_" + anaBOnsetFrame;
                  out.println(laggingMessage);
                  IJ.log(laggingMessage);
                  IJ.openImage(mitoDir + File.separator + "croppedImgs"
                        + File.separator + cellNb + "_GFP.tif").show();
               }
            }
            //TODO to show unaligned cell
            if (cell.unalignedSpotFrames().size() > 0) {
               String unalignKtMessage = "Unaligned : Cell " + cellNb + " detected with unaligned kinetochore(s)";
               IJ.log(unalignKtMessage);
               out.println(unalignKtMessage);
            }
         }
         assert out != null;
         out.close();
         IJ.log("lagging detection finished");
      }
   }

   static HashMap getMitoticCellNbs(String mitoDir) {
      return FileUtils.readTable(mitoDir + File.separator + "mitosis_time_board.csv");
   }

   public static void analyzeMitosisDynamic(DefaultSetOfCells soc, MaarsParameters parameters) {
      // TODO need to find a place for the metadata, maybe in images
      IJ.log("Start python analysis");
      String pos = soc.getPosLabel();
      String pathToRoot = parameters.getSavingPath() + File.separator;
      String mitoDir = pathToRoot + MITODIRNAME + File.separator + pos + File.separator;
      FileUtils.createFolder(mitoDir);
      String[] mitosis_cmd = new String[]{PythonPipeline.getPythonDefaultPathInConda(), MaarsParameters.DEPS_DIR +
            PythonPipeline.ANALYSING_SCRIPT_NAME, pathToRoot, parameters.getDetectionChForMitosis(),
            parameters.getCalibration(), String.valueOf((Math.round(Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL)) / 1000))),
            pos, parameters.getSegmentationParameter(MaarsParameters.SEG_PREFIX),
            parameters.getFluoParameter(MaarsParameters.FLUO_PREFIX), "-minimumPeriod", parameters.getMinimumMitosisDuration()};
      PythonPipeline.runPythonScript(mitosis_cmd, mitoDir + "mitosisDetection_log.txt");
      HashMap map = getMitoticCellNbs(mitoDir);
      String[] colocalisation_cmd = new String[]{PythonPipeline.getPythonDefaultPathInConda(), MaarsParameters.DEPS_DIR +
            PythonPipeline.COLOCAL_SCRIPT_NAME, mitoDir + "spots" + File.separator,
            parameters.getDetectionChForMitosis(), "GFP", mitoDir};
      colocalisation_cmd = Stream.of(colocalisation_cmd, map.keySet().toArray(new String[map.keySet().size()])).
            flatMap(Stream::of).toArray(String[]::new);
      PythonPipeline.runPythonScript(colocalisation_cmd, mitoDir + "colocalisation.log");
      ArrayList<String> cmds = new ArrayList<>();
      cmds.add(String.join(" ", mitosis_cmd));
      cmds.add(String.join(" ", colocalisation_cmd));
      String bashPath = mitoDir + "pythonAnalysis.sh";
      FileUtils.writeScript(bashPath,cmds);
      IJ.log("Script saved");
      findAbnormalCells(mitoDir, soc, map);
   }

   public static void copyDeps(){
      FileUtils.createFolder(MaarsParameters.DEPS_DIR);
      FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.TRACKMATE_LOADER_NAME);
      FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.ANALYSING_SCRIPT_NAME);
      FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.COLOCAL_SCRIPT_NAME);
   }



   public static String[] getBfImgs(MaarsParameters parameters){
      String segImgsDir = FileUtils.convertPath(parameters.getSavingPath() + File.separator +
            parameters.getSegmentationParameter(MaarsParameters.SEG_PREFIX) + File.separator);
      ArrayList<String> names = FileUtils.getTiffWithPattern(segImgsDir, ".*_MMStack_.*.ome.tif");
      return names.toArray(new String[names.size()]);
   }

   public static String[] getPosNbs(String[] imgNames){
      String[] posNbs = new String[imgNames.length];
      Pattern pattern = Pattern.compile(".*_MMStack_(.*?).ome.tif");
      for (int i=0;i<imgNames.length;i++){
         Matcher matcher = pattern.matcher(imgNames[i]);
         while ( matcher.find() ) {
            posNbs[i] = matcher.group(1);
         }
      }
      return posNbs;
   }

   public static void post_segmentation(MaarsParameters parameters){
      String[] imgNames = getBfImgs(parameters);
      String[] posNbs = getPosNbs(imgNames);
      segExecuter(parameters, imgNames, posNbs);
   }

   private static void segExecuter(MaarsParameters parameters, String[] imgNames, String[] posNbs) {
      ExecutorService es = Executors.newSingleThreadExecutor();
      ImagePlus segImg = null;
      for (int i=0; i< imgNames.length;i++){
         try {
            segImg = IJ.openImage(FileUtils.convertPath(parameters.getSavingPath() + File.separator +
                  parameters.getSegmentationParameter(MaarsParameters.SEG_PREFIX) + File.separator) + imgNames[i]);
            parameters.setCalibration(String.valueOf(segImg.getCalibration().pixelWidth));
         } catch (Exception e) {
            IOUtils.printErrorToIJLog(e);
         }
         es.execute(new MaarsSegmentation(parameters, segImg, posNbs[i]));
      }
      es.shutdown();
      try {
         es.awaitTermination(1, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }


   public static MaarsParameters loadParameters(){
      String configFileName = "maars_config.xml";
      InputStream inStream = null;
      if (FileUtils.exists(configFileName)) {
         try {
            inStream = new FileInputStream(configFileName);
         } catch (FileNotFoundException e) {
            IOUtils.printErrorToIJLog(e);
         }
      } else {
         inStream = FileUtils.getInputStreamOfScript("maars_default_config.xml");
      }
      return new MaarsParameters(inStream);
   }
}
