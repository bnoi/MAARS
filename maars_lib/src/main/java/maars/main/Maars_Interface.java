package maars.main;

import ij.IJ;
import ij.ImagePlus;
import maars.agents.Cell;
import maars.agents.SetOfCells;
import maars.cellAnalysis.PythonPipeline;
import maars.io.IOUtils;
import maars.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Created by tongli on 09/06/2017.
 */
public class Maars_Interface {
   public static final String SEG = "SegImgStack";
   public static final String FLUO = "FluoImgStack";
   public static final String MITODIRNAME = "Mitosis";
   public final static String SEGANALYSISDIR = "SegAnalysis" + File.separator + "pos";
   public final static String FLUOANALYSISDIR = "FluoAnalysis" + File.separator + "pos";
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
                                         SetOfCells soc,
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

   public static void analyzeMitosisDynamic(SetOfCells soc, MaarsParameters parameters, String pathToRoot, int posNb) {
      // TODO need to find a place for the metadata, maybe in images
      IJ.log("Start python analysis");
      String mitoDir = pathToRoot + MITODIRNAME + File.separator + "pos"+posNb + File.separator;
      FileUtils.createFolder(mitoDir);
      String[] mitosis_cmd = new String[]{PythonPipeline.getPythonDefaultPathInConda(), MaarsParameters.DEPS_DIR +
            PythonPipeline.ANALYSING_SCRIPT_NAME, pathToRoot, parameters.getDetectionChForMitosis(),
            parameters.getCalibration(), String.valueOf((Math.round(Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL)) / 1000))),
            "-minimumPeriod", parameters.getMinimumMitosisDuration()};
      PythonPipeline.runPythonScript(mitosis_cmd, mitoDir + "mitosisDetection_log.txt");
      HashMap map = getMitoticCellNbs(mitoDir);
      String[] colocalisation_cmd = new String[]{PythonPipeline.getPythonDefaultPathInConda(), MaarsParameters.DEPS_DIR +
            PythonPipeline.COLOCAL_SCRIPT_NAME, mitoDir + "spots" + File.separator,
            parameters.getDetectionChForMitosis(), "GFP", mitoDir + "phases" + File.separator};
      colocalisation_cmd = Stream.of(colocalisation_cmd, map.keySet().toArray(new String[map.keySet().size()])).
            flatMap(Stream::of).toArray(String[]::new);
      FileUtils.createFolder(mitoDir + "phases");
      PythonPipeline.runPythonScript(colocalisation_cmd, mitoDir + "colocalisation_log.txt");
      ArrayList<String> cmds = new ArrayList<>();
      cmds.add(String.join(" ", mitosis_cmd));
      cmds.add(String.join(" ", colocalisation_cmd));
      String bashPath = mitoDir + "pythonAnalysis.sh";
      FileUtils.writeScript(bashPath,cmds);
      IJ.log("Script saved");
      findAbnormalCells(mitoDir, soc, map);
   }
   public static void copyDeps(){
      if (!FileUtils.exists(MaarsParameters.DEPS_DIR)) {
         FileUtils.createFolder(MaarsParameters.DEPS_DIR);
         FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.TRACKMATE_LOADER_NAME);
         FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.ANALYSING_SCRIPT_NAME);
         FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.COLOCAL_SCRIPT_NAME);
      }
   }

   public static String[] post_segmentation(String segImgsDir, MaarsParameters parameters){
      segImgsDir = FileUtils.convertPath(segImgsDir);
      ArrayList<String> names = FileUtils.getTiffWithPattern(segImgsDir, ".*_MMStack_Pos.*");
      String[] posNbs = new String[names.size()];
      // --------------------------segmentation-----------------------------//
      ExecutorService es = Executors.newSingleThreadExecutor();
      ImagePlus segImg = null;
      ArrayList<MaarsSegmentation> msList = new ArrayList<>();
      for (int i=0; i< names.size();i++){
         posNbs[i] = String.valueOf(i);
         try {
            segImg = IJ.openImage(FileUtils.convertPath(segImgsDir + File.separator + names.get(i)));
            parameters.setCalibration(String.valueOf(segImg.getCalibration().pixelWidth));
         } catch (Exception e) {
            IOUtils.printErrorToIJLog(e);
         }
         msList.add(new MaarsSegmentation(parameters, segImg, i));
      }
      for (MaarsSegmentation ms: msList) {
         try {
            es.submit(ms).get();
         } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
         }
      }
      es.shutdown();
      try {
         es.awaitTermination(2, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      return posNbs;
   }
}
