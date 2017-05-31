package edu.univ_tlse3.maars;

import edu.univ_tlse3.acquisition.MAARS_mda;
import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.FluoAnalyzer;
import edu.univ_tlse3.cellstateanalysis.PythonPipeline;
import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import edu.univ_tlse3.display.SOCVisualizer;
import edu.univ_tlse3.gui.MaarsMainDialog;
import edu.univ_tlse3.resultSaver.MAARSGeometrySaver;
import edu.univ_tlse3.resultSaver.MAARSImgSaver;
import edu.univ_tlse3.resultSaver.MAARSSpotsSaver;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.IOUtils;
import edu.univ_tlse3.utils.ImgUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.frame.RoiManager;
import mmcorej.CMMCore;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.ReportingUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Main MAARS program
 *
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 21, 2015
 */
public class MAARS implements Runnable {
   private static boolean stop_ = false;
   private PrintStream curr_err;
   private PrintStream curr_out;
   private MMStudio mm;
   private CMMCore mmc;
   private MaarsParameters parameters;
   private ArrayList<SetOfCells> socList_;
   private ArrayList<SOCVisualizer> socVisualizerList_;
   private CopyOnWriteArrayList<Map<String, Future>> tasksSet_;

   /**
    * * Constructor
    *
    * @param mm                MMStudio object (gui)
    * @param mmc               CMMCore object (core)
    * @param parameters        MAARS parameters object
    * @param socVisualizerList list of set of cell visualizer
    * @param tasksSet          tasks to be terminated
    * @param socList           list of set of cell
    */
   public MAARS(MMStudio mm, CMMCore mmc, MaarsParameters parameters, ArrayList<SOCVisualizer> socVisualizerList,
                CopyOnWriteArrayList<Map<String, Future>> tasksSet,
                ArrayList<SetOfCells> socList) {
      this.mmc = mmc;
      this.parameters = parameters;
      socList_ = socList;
      this.mm = mm;
      tasksSet_ = tasksSet;
      socVisualizerList_ = socVisualizerList;
   }

   static void saveAll(SetOfCells soc, String pathToFluoDir, Boolean useDynamic) {
      IJ.log("Saving information of each cell");
      MAARSSpotsSaver spotSaver = new MAARSSpotsSaver(pathToFluoDir);
      MAARSGeometrySaver geoSaver = new MAARSGeometrySaver(pathToFluoDir);
      // TODO
      CopyOnWriteArrayList<Integer> cellIndex = soc.getPotentialMitosisCell();
      for (int i : cellIndex) {
         Cell c = soc.getCell(i);
         geoSaver.save(c);
         spotSaver.save(c);
      }
      if (useDynamic) {
         IOUtils.serializeSoc(pathToFluoDir, soc);
      }
   }

   static void saveAll(SetOfCells soc, ImagePlus mergedImg, String pathToFluoDir,
                       Boolean useDynamic, ArrayList<String> arrayChannels) {
      IJ.log("Saving information of each cell on disk");
      MAARSSpotsSaver spotSaver = new MAARSSpotsSaver(pathToFluoDir);
      MAARSGeometrySaver geoSaver = new MAARSGeometrySaver(pathToFluoDir);
      MAARSImgSaver imgSaver = new MAARSImgSaver(pathToFluoDir);
//        TODO
      CopyOnWriteArrayList<Integer> cellIndex = soc.getPotentialMitosisCell();
      for (int i : cellIndex) {
         Cell cell = soc.getCell(i);
//        for (Cell cell : soc){
         geoSaver.save(cell);
         spotSaver.save(cell);
         mergedImg.setRoi(cell.getCellShapeRoi());
         for (int j = 1; j <= mergedImg.getNChannels(); j++) {
            ImagePlus croppedImg = new Duplicator().run(mergedImg, j, j, 1, mergedImg.getNSlices(),
                  1, mergedImg.getNFrames());
            IJ.run(croppedImg, "Grays", "");
            croppedImg.setRoi(ImgUtils.centerCroppedRoi(cell.getCellShapeRoi()));
            imgSaver.saveImgs(croppedImg, i, arrayChannels.get(j - 1), false);
         }
      }
      if (useDynamic) {
         IOUtils.serializeSoc(pathToFluoDir, soc);
      }
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
                  out.println("Lagging :" + cellNb + "_laggingTimePoint_" + laggingTimePoint + "_anaBonset_" + anaBOnsetFrame);
                  IJ.log("Lagging :" + cellNb + "_laggingTimePoint_" + laggingTimePoint + "_anaBonset_" + anaBOnsetFrame);
                  IJ.openImage(mitoDir + File.separator + "croppedImgs"
                        + File.separator + cellNb + "_GFP.tif").show();
               }
            }
            //TODO to show unaligned cell
            if (cell.unalignedSpotFrames().size() > 0) {
               IJ.log("Unaligned : Cell " + cellNb + " detected with unaligned kinetochore(s)");
               out.println("Unaligned : Cell " + cellNb + " detected with unaligned kinetochore(s)");
            }
         }
         assert out != null;
         out.close();
         IJ.log("lagging detection finished");
      }
   }

   static HashMap getMitoticCellNbs(String mitoDir) {
      return FileUtils.readTable(mitoDir + File.separator + "mitosis_time_board.txt");
   }

   public static void analyzeMitosisDynamic(SetOfCells soc, MaarsParameters parameters, String pathToSegDir) {
      // TODO need to find a place for the metadata, maybe in images
      IJ.log("Start python analysis");
      String mitoDir = pathToSegDir + "_MITOSIS"+ File.separator;
      String[] mitosis_cmd = new String[]{PythonPipeline.getPythonDefaultPathInConda(), MaarsParameters.DEPS_DIR +
            PythonPipeline.ANALYSING_SCRIPT_NAME, pathToSegDir, parameters.getDetectionChForMitosis(),
            parameters.getCalibration(), String.valueOf((Math.round(Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL)) / 1000))),
            "-minimumPeriod", parameters.getMinimumMitosisDuration()};
      FileUtils.createFolder(mitoDir);
      FileUtils.createFolder(mitoDir + "phases");
      PythonPipeline.runPythonScript(mitosis_cmd, mitoDir + "mitosisDetection_log.txt");
      HashMap map = MAARS.getMitoticCellNbs(mitoDir);
      String mitosisCellNbs = String.join(" ", map.keySet());
      String[] colocalisation_cmd = new String[]{PythonPipeline.getPythonDefaultPathInConda(), MaarsParameters.DEPS_DIR +
            PythonPipeline.COLOCAL_SCRIPT_NAME, mitoDir + "spots" + File.separator,
            parameters.getDetectionChForMitosis(), "GFP", mitoDir + "phases" + File.separator};
      colocalisation_cmd = Stream.of(colocalisation_cmd, map.keySet().toArray(new String[map.keySet().size()])).
            flatMap(Stream::of).toArray(String[]::new);
      System.out.println(String.join(" ", colocalisation_cmd));
      ArrayList cmds = new ArrayList();
      cmds.add(String.join(" ", mitosis_cmd));
      cmds.add(String.join(" ", colocalisation_cmd));
      String bashPath = mitoDir + "pythonAnalysis.sh";
      FileUtils.writeScript(bashPath,cmds);
      IJ.log("Script saved");
      PythonPipeline.runPythonScript(colocalisation_cmd, mitoDir + "colocalisation_log.txt");
      MAARS.findAbnormalCells(mitoDir, soc, map);
   }

   public void interrupt() {
      stop_ = true;
   }

   @Override
   public void run() {
      String BF = "BF";
      String FLUO = "FLUO";
      // Start time
      long start = System.currentTimeMillis();
      parameters.setCalibration(String.valueOf(mm.getCachedPixelSizeUm()));
      ArrayList<String> arrayChannels = new ArrayList<>();
      Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));
//        for (SetOfCells soc : socList_){
//           soc.reset();
//        }
      String savingPath = FileUtils.convertPath(parameters.getSavingPath());
      //acquisition

      ExecutorService es = Executors.newSingleThreadExecutor();
      HashMap<Integer, ImagePlus[]> segImgs = null;
      try {
         segImgs = es.submit(new MAARS_mda(
               parameters.getSegmentationParameter(MaarsParameters.PATH_TO_BF_ACQ_SETTING),
               savingPath, BF)).get();
      } catch (InterruptedException | ExecutionException e) {
         IOUtils.printErrorToIJLog(e);
      }
      if (segImgs == null) {
         IJ.log("No images acquired");
         return;
      }
      //update saving path
      parameters.setSavingPath(savingPath + File.separator + BF + "_1");
      MaarsSegmentation ms = null;
      ArrayList<MaarsSegmentation> arrayMs = new ArrayList<>();
      for (Integer posNb : segImgs.keySet()) {
         ImagePlus segImg = segImgs.get(posNb)[0];
         // --------------------------segmentation-----------------------------//
         ms = new MaarsSegmentation(parameters, segImg, posNb);
         arrayMs.add(ms);
         try {
            es.submit(ms).get();
         } catch (InterruptedException | ExecutionException e) {
            IOUtils.printErrorToIJLog(e);
         }
      }
      es.shutdown();
      try {
         es.awaitTermination(3, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
         IOUtils.printErrorToIJLog(e);
      }
      parameters.setSavingPath(savingPath);
      // from Roi initialize a set of cell
      for (Integer posNb : segImgs.keySet()) {
         SetOfCells soc = socList_.get(posNb);
         soc.reset();
         soc.loadCells(savingPath + File.separator + BF + "_1", posNb);
         soc.setRoiMeasurementIntoCells(arrayMs.get(posNb).getRoiMeasurements());
      }
      // ----------------start acquisition and analysis --------//
      redirectLog(savingPath);
      int frame = 0;
      Boolean do_analysis = Boolean.parseBoolean(parameters.getFluoParameter(MaarsParameters.DO_ANALYSIS));
      double fluoTimeInterval = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL));
      ExecutorService es1 = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      if (parameters.useDynamic()) {
         // being dynamic acquisition
         double startTime = System.currentTimeMillis();
         double timeLimit = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_LIMIT)) * 60
               * 1000;
         while (System.currentTimeMillis() - startTime <= timeLimit) {
            double beginAcq = System.currentTimeMillis();
            if (stop_) {
               es1.shutdownNow();
               break;
            }
            // Section to acquire bf images between fluos, can be skipped
            if (frame != 0) {
               try {
                  segImgs = es1.submit(new MAARS_mda(parameters.getSegmentationParameter(MaarsParameters.PATH_TO_BF_ACQ_SETTING),
                        savingPath, BF)).get();
               } catch (InterruptedException | ExecutionException e) {
                  IOUtils.printErrorToIJLog(e);
               }
               parameters.setSavingPath(savingPath + File.separator + BF + "_" + String.valueOf(frame + 1));
               for (Integer posNb : segImgs.keySet()) {
                  ImagePlus segImg = segImgs.get(posNb)[0];
                  //update saving path
//                          parameters.setSavingPath(savingPath + File.separator + BF + "_"+posNb);
                  // --------------------------segmentation-----------------------------//
                  ms = new MaarsSegmentation(parameters, segImg, posNb);
                  try {
                     es1.submit(ms).get();
                  } catch (InterruptedException | ExecutionException e) {
                     IOUtils.printErrorToIJLog(e);
                  }
               }
            }

            Map<String, Future> channelsInFrame = new HashMap<>();
            HashMap<Integer, ImagePlus[]> fluos = null;
            try {
               fluos = es1.submit(new MAARS_mda(
                     parameters.getFluoParameter(MaarsParameters.PATH_TO_FLUO_ACQ_SETTING),
                     savingPath, FLUO)).get();
            } catch (InterruptedException | ExecutionException e) {
               IOUtils.printErrorToIJLog(e);
            }
            for (Integer posNb : fluos.keySet()) {
               for (ImagePlus chImp:fluos.get(posNb)){
                  //TODO
                  if (do_analysis) {
                     String channel = chImp.getTitle();
                     Future future2 = es1.submit(new FluoAnalyzer(chImp, chImp.getCalibration(), socList_.get(posNb), channel,
                           Integer.parseInt(parameters.getChMaxNbSpot(channel)),
                           Double.parseDouble(parameters.getChSpotRaius(channel)),
                           Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizerList_.get(posNb), parameters.useDynamic()));
                     channelsInFrame.put(channel, future2);
                  }
               }
            }

            tasksSet_.add(channelsInFrame);
            frame++;
            double acqTook = System.currentTimeMillis() - beginAcq;
            if (fluoTimeInterval > acqTook) {
               try {
                  Thread.sleep((long) (fluoTimeInterval - acqTook));
               } catch (InterruptedException e) {
                  IOUtils.printErrorToIJLog(e);
               }
            } else {
               IJ.log("Attention : acquisition before took longer than " + fluoTimeInterval
                     / 1000 + " s : " + acqTook);
            }
         }
         IJ.log("Acquisition Done, proceeding to post-analysis");
      } else {
         // being static acquisition
         Map<String, Future> channelsInFrame = new HashMap<>();
         HashMap<Integer, ImagePlus[]> fluos = null;
         try {
            fluos = es1.submit(new MAARS_mda(
                  parameters.getFluoParameter(MaarsParameters.PATH_TO_FLUO_ACQ_SETTING),
                  savingPath, FLUO)).get();
         } catch (InterruptedException | ExecutionException e) {
            IOUtils.printErrorToIJLog(e);
         }
         for (Integer posNb : fluos.keySet()) {
            for (ImagePlus chImp:fluos.get(posNb)){
               String channel = chImp.getTitle();
               if (do_analysis) {
                  Future future2 = es1.submit(new FluoAnalyzer(chImp, chImp.getCalibration(), socList_.get(posNb), channel,
                        Integer.parseInt(parameters.getChMaxNbSpot(channel)),
                        Double.parseDouble(parameters.getChSpotRaius(channel)),
                        Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizerList_.get(posNb), parameters.useDynamic()));
                  channelsInFrame.put(channel, future2);
               }
            }
         }
         tasksSet_.add(channelsInFrame);
      }
      parameters.setSavingPath(savingPath);
      es1.shutdown();
      try {
         es1.awaitTermination(60, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      MaarsMainDialog.waitAllTaskToFinish(tasksSet_);
      RoiManager.getInstance().reset();
      RoiManager.getInstance().close();
      for (Integer posNb : segImgs.keySet()) {
         SetOfCells soc = socList_.get(posNb);
         if (do_analysis) {
            long startWriting = System.currentTimeMillis();
            String pathToFluoDir = savingPath + File.separator + FLUO + "_1";
            ImagePlus mergedImg = ImgUtils.loadFullFluoImgs(pathToFluoDir);
            mergedImg.getCalibration().frameInterval = fluoTimeInterval / 1000;
            MAARS.saveAll(soc, mergedImg, pathToFluoDir, parameters.useDynamic(), arrayChannels);
            if (parameters.useDynamic()) {
               if (IJ.isWindows()) {
                  savingPath = FileUtils.convertPathToLinuxType(savingPath + File.separator + BF + "_1");
               }
               MAARS.analyzeMitosisDynamic(soc, parameters, savingPath);
            }
            ReportingUtils.logMessage("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                  + " sec for writing results");
         }
//                RemoteNotification.mailNotify("tongli.bioinfo@gmail.com");
         mmc.setAutoShutter(true);
         System.setErr(curr_err);
         System.setOut(curr_out);
         soc.reset();
      }
      IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing all fields");
      System.gc();
      IJ.showMessage("MAARS: Done!");
      MaarsMainDialog.okMainDialogButton.setEnabled(true);
   }

   private void redirectLog(String savingPath) {
      try {
         PrintStream ps = new PrintStream(savingPath + File.separator + "FluoAnalysis.LOG");
         curr_err = System.err;
         curr_out = System.err;
         System.setOut(ps);
         System.setErr(ps);
      } catch (FileNotFoundException e) {
         IOUtils.printErrorToIJLog(e);
      }
   }

   public static void copyDeps(){
      if (!FileUtils.exists(MaarsParameters.DEPS_DIR)) {
         FileUtils.createFolder(MaarsParameters.DEPS_DIR);
         FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.TRACKMATE_LOADER_NAME);
         FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.ANALYSING_SCRIPT_NAME);
         FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.COLOCAL_SCRIPT_NAME);
      }
   }
}
