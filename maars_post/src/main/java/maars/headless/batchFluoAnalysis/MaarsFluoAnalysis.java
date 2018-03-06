package maars.headless.batchFluoAnalysis;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;

import loci.formats.FormatException;
import maars.agents.Cell;
import maars.agents.DefaultSetOfCells;
import maars.cellAnalysis.FluoAnalyzer;
import maars.cellAnalysis.PythonPipeline;
import maars.display.SOCVisualizer;
import maars.io.IOUtils;
import maars.main.MaarsParameters;
import maars.main.Maars_Interface;
import maars.utils.FileUtils;
import maars.utils.ImgUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tong on 30/06/17.
 */
public class MaarsFluoAnalysis implements Runnable{
   public static final String MITODIRNAME = "Mitosis";
   private static String FLUOIMGPATH = null;
   private static Logger logger = LoggerFactory.getLogger(MaarsFluoAnalysis.class);


   private Map<Integer, String> serieNbPos;
   private MaarsParameters parameters_;
   private SOCVisualizer visualizer;
   private String[] usingChannels;
   public MaarsFluoAnalysis(MaarsParameters parameters, String suffix){
      String fluoDir = FileUtils.convertPath(parameters.getSavingPath()) + File.separator +
            parameters.getFluoParameter(MaarsParameters.FLUO_PREFIX);
      FLUOIMGPATH = Objects.requireNonNull(new File(fluoDir).listFiles(
            (FilenameFilter) new WildcardFileFilter("*." + suffix)))[0].getAbsolutePath();
      serieNbPos = ImgUtils.populateSeriesImgNames(FLUOIMGPATH);
      parameters_ = parameters;
      usingChannels = parameters_.getUsingChannels().split(",", -1);
      IJ.log(usingChannels[0]);
      visualizer = new SOCVisualizer(usingChannels);
      visualizer.setVisible(true);
   }
   @Override
   public void run() {
      AtomicBoolean stop = new AtomicBoolean(false);
      PrintStream curr_err = null;
      PrintStream curr_out = null;
      DefaultSetOfCells soc;
      String segAnaDir = FileUtils.convertPath(parameters_.getSavingPath()) + File.separator +
            parameters_.getSegmentationParameter(MaarsParameters.SEG_PREFIX) + Maars_Interface.SEGANALYSIS_SUFFIX;
      for (int serie : serieNbPos.keySet()) {
         String posName = serieNbPos.get(serie);
         ImagePlus concatenatedFluoImgs = null;
         soc = new DefaultSetOfCells(posName);
         String currentPosPrefix = segAnaDir + posName + File.separator;
         String currentZipPath = currentPosPrefix + "ROI.zip";
         if (FileUtils.exists(currentZipPath)) {
            // from Roi.zip initialize a set of cell
            soc.loadCells(currentZipPath);
            IJ.open(currentPosPrefix + "Results.csv");
            ResultsTable rt = ResultsTable.getResultsTable();
            ResultsTable.getResultsWindow().close(false);
            soc.addRoiMeasurementIntoCells(rt);
            // ----------------start acquisition and analysis --------//
            try {
               PrintStream ps = new PrintStream(parameters_.getSavingPath() + File.separator + "FluoAnalysis.LOG");
               curr_err = System.err;
               curr_out = System.out;
               System.setOut(ps);
               System.setErr(ps);
            } catch (FileNotFoundException e) {
               IOUtils.printErrorToIJLog(e);
            }
            CopyOnWriteArrayList<Map<String, Future>> tasksSet = new CopyOnWriteArrayList<>();
            try {
               concatenatedFluoImgs = processStackedImg(FLUOIMGPATH, serie,
                           parameters_, soc, visualizer, tasksSet, stop);
            } catch (IOException | FormatException e) {
               e.printStackTrace();
            }
            concatenatedFluoImgs.getCalibration().frameInterval =
                  Double.parseDouble(parameters_.getFluoParameter(MaarsParameters.TIME_INTERVAL)) / 1000;
            Maars_Interface.waitAllTaskToFinish(tasksSet);
            if (!stop.get() && soc.size() != 0) {
               long startWriting = System.currentTimeMillis();
               ArrayList<String> arrayChannels = new ArrayList<>();
               Collections.addAll(arrayChannels, parameters_.getUsingChannels().split(",", -1));
               FileUtils.createFolder(parameters_.getSavingPath() + File.separator + parameters_.getFluoParameter(MaarsParameters.FLUO_PREFIX)
                     +Maars_Interface.FLUOANALYSIS_SUFFIX);
               IOUtils.saveAll(soc, concatenatedFluoImgs, parameters_.getSavingPath() + File.separator, parameters_.useDynamic(),
                     arrayChannels, posName, parameters_.getFluoParameter(MaarsParameters.FLUO_PREFIX));
               IJ.log("It took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                     + " sec for writing results");
               if (parameters_.useDynamic()) {
                  analyzeMitosisDynamic(soc, parameters_, concatenatedFluoImgs.getCalibration().pixelWidth);
               }
            }
         }
         visualizer.clear();
         visualizer.setVisible(true);
         soc.reset();
         System.gc();
      }
      System.setErr(curr_err);
      System.setOut(curr_out);
   }

   private ImagePlus processStackedImg(String imgPath, int serie, MaarsParameters parameters, DefaultSetOfCells soc,
                                       SOCVisualizer socVisualizer, CopyOnWriteArrayList<Map<String, Future>> tasksSet,
                                       AtomicBoolean stop) throws IOException, FormatException {
      ImagePlus concatenatedFluoImgs = ImgUtils.lociImport(imgPath, serie);

      String[] arrayChannels = parameters.getUsingChannels().split(",");

      int totalChannel = Integer.parseInt(concatenatedFluoImgs.getStringProperty("SizeC"));
      int totalSlice = Integer.parseInt(concatenatedFluoImgs.getStringProperty("SizeZ"));
      int totalFrame = Integer.parseInt(concatenatedFluoImgs.getStringProperty("SizeT"));

      ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      Duplicator duplicator = new Duplicator();
      for (int i = 1; i <= totalFrame; i++) {
         Map<String, Future> chAnalysisTasks = new HashMap<>();
         for (int j = 1; j <= totalChannel; j++) {
            String channel = arrayChannels[j - 1];
            IJ.log("Processing channel " + channel + "_" + i);
            ImagePlus zProjectedFluoImg = ImgUtils.zProject(
                  duplicator.run(concatenatedFluoImgs, j, j, 1, totalSlice, i, i)
                  , concatenatedFluoImgs.getCalibration());
            Future future = es.submit(new FluoAnalyzer(zProjectedFluoImg, zProjectedFluoImg.getCalibration(),
                  soc, channel, Integer.parseInt(parameters.getChMaxNbSpot(channel)),
                  Double.parseDouble(parameters.getChSpotRaius(channel)),
                  Double.parseDouble(parameters.getChQuality(channel)), i, socVisualizer,
                  parameters.useDynamic()));
            chAnalysisTasks.put(channel, future);
         }
         tasksSet.add(chAnalysisTasks);
         if (stop.get()) {
            break;
         }
      }
      System.gc();
      es.shutdown();
      if (Boolean.parseBoolean(parameters.getProjected())) {
         IJ.run(concatenatedFluoImgs, "Z Project...", "projection=[Max Intensity] all");
         return (IJ.getImage());
      }
      return concatenatedFluoImgs.duplicate();
   }

   private static void findAbnormalCells(String mitoDir,
                                         DefaultSetOfCells soc,
                                         HashMap slopeChanges) {
      assert FileUtils.exists(mitoDir);
      PrintWriter out = null;
      try {
         out = new PrintWriter(mitoDir + File.separator + "abnormalCells.txt");
      } catch (FileNotFoundException e) {
         IOUtils.printErrorToIJLog(e);
      }
      assert out != null;
      for (Object cellNb : slopeChanges.keySet()) {
         int cellNbInt = Integer.parseInt(String.valueOf(cellNb));
         int anaBOnsetFrame = Integer.valueOf(((String[]) slopeChanges.get(cellNb))[1]);
         int lastAnaphaseFrame = Integer.valueOf(((String[]) slopeChanges.get(cellNb))[2]);
         Cell cell = soc.getCell(cellNbInt);
         cell.setAnaBOnsetFrame(anaBOnsetFrame);
         ArrayList<Integer> spotInBtwnFrames = cell.getSpotInBtwnFrames();
         if (spotInBtwnFrames.size() > 0) {
            Collections.sort(spotInBtwnFrames);
            int laggingTimePoint = spotInBtwnFrames.get(spotInBtwnFrames.size() - 1);
            if (laggingTimePoint > anaBOnsetFrame && laggingTimePoint < lastAnaphaseFrame) {
               String laggingMessage = "Lagging :" + cellNb + "_lastLaggingTimePoint_" + laggingTimePoint + "_anaBonset_" + anaBOnsetFrame;
               out.println(laggingMessage);
               logger.info(laggingMessage);
               IJ.openImage(mitoDir + File.separator + "croppedImgs"
                     + File.separator + cellNb + "_GFP.tif").show();
            }
         }
         //TODO to show unaligned cell
         if (cell.unalignedSpotFrames().size() > 0) {
            String unalignKtMessage = "Unaligned : Cell " + cellNb + " detected with unaligned kinetochore(s)";
            logger.info(unalignKtMessage);
            out.println(unalignKtMessage);
         }
      }
      assert out != null;
      out.close();
      IJ.log("lagging detection finished");
   }

   static HashMap getMitoticCellNbs(String mitoDir) {
      return FileUtils.readTable(mitoDir + File.separator + "slopeChanges.csv");
   }

   public static void analyzeMitosisDynamic(DefaultSetOfCells soc, MaarsParameters parameters, double calib) {
      IJ.log("Start python analysis");
      String pos = soc.getPosLabel();
      String pathToRoot = parameters.getSavingPath() + File.separator;
      String mitoDir = pathToRoot + MITODIRNAME + File.separator + pos + File.separator;
      FileUtils.createFolder(mitoDir);
      String[] mitosis_cmd = new String[]{PythonPipeline.getPythonDefaultPathInConda(), MaarsParameters.DEPS_DIR +
            PythonPipeline.ANALYSING_SCRIPT_NAME, pathToRoot, parameters.getDetectionChForMitosis(),
            Double.toString(calib), String.valueOf((Math.round(Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL)) / 1000))),
            pos, parameters.getSegmentationParameter(MaarsParameters.SEG_PREFIX),
            parameters.getFluoParameter(MaarsParameters.FLUO_PREFIX), "-minimumPeriod", parameters.getMinimumMitosisDuration()};
      ArrayList<String> cmds = new ArrayList<>();
      cmds.add(String.join(" ", mitosis_cmd));
      String bashPath = mitoDir + "pythonAnalysis.sh";
      FileUtils.writeScript(bashPath, cmds);
      IJ.log("Script saved. If it fails, you can still run it manually afterward.");
      PythonPipeline.runPythonScript(mitosis_cmd, mitoDir + "mitosisDetection_log.txt");
      findAbnormalCells(mitoDir, soc, getMitoticCellNbs(mitoDir));
   }
}
