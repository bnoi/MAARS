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
   public static final String SEG = "SegImgStacks";
   public static final String FLUO = "FluoImgStacks";
   public static final String MITODIRNAME = "Mitosis";
   public final static String SEGANALYSIS_SUFFIX = "_SegAnalysis" + File.separator;
   public final static String FLUOANALYSISDIR = "FluoAnalysis" + File.separator;
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
            pos, "-minimumPeriod", parameters.getMinimumMitosisDuration()};
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
      FileUtils.createFolder(MaarsParameters.DEPS_DIR);
      FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.TRACKMATE_LOADER_NAME);
      FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.ANALYSING_SCRIPT_NAME);
      FileUtils.copy(MaarsParameters.DEPS_DIR, PythonPipeline.COLOCAL_SCRIPT_NAME);
   }

   private static ArrayList<Integer> getFluoAcqStructure(String pathToFluoDir) {
      String[] listAcqNames = new File(pathToFluoDir).list();
      String pattern = "(\\w+)(_)(\\d+)";
      ArrayList<Integer> arrayImgFrames = new ArrayList<>();
      assert listAcqNames != null;
      for (String acqName : listAcqNames) {
         if (Pattern.matches(pattern, acqName)) {
            String current_frame = acqName.split("_", -1)[1];
            if (!arrayImgFrames.contains(Integer.parseInt(current_frame))) {
               arrayImgFrames.add(Integer.parseInt(current_frame));
            }
         }
      }
      Collections.sort(arrayImgFrames);
      return arrayImgFrames;
   }

   private static ImagePlus prepareImgToSave(ImagePlus projectedImg, ImagePlus notProjected, String channel, int frame,
                                             Boolean projected) {
      ImagePlus imgToSave = projected ? projectedImg : notProjected;
      for (int i = 1; i <= imgToSave.getStack().getSize(); i++) {
         imgToSave.getStack().setSliceLabel(channel + "_" + frame, i);
      }
      return imgToSave;
   }

   private static ImagePlus processSplitImgs(String pathToFluoImgsDir, MaarsParameters parameters, DefaultSetOfCells soc,
                                             SOCVisualizer socVisualizer, CopyOnWriteArrayList<Map<String, Future>> tasksSet,
                                             AtomicBoolean stop) {
      ArrayList<Integer> arrayImgFrames = getFluoAcqStructure(pathToFluoImgsDir);
      int totalFrame = arrayImgFrames.size();

      Concatenator concatenator = new Concatenator();
      concatenator.setIm5D(true);

      ArrayList<String> arrayChannels = new ArrayList<>();
      Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));

      ImagePlus concatenatedFluoImgs = null;
      ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      for (Integer current_frame : arrayImgFrames) {
         Map<String, Future> analysisTasks = new HashMap<>();
         for (String channel : arrayChannels) {
            IJ.log("Processing channel " + channel + "_" + current_frame);
            String pathToFluoMovie = pathToFluoImgsDir + channel + "_" + current_frame + "/" + channel + "_" + current_frame + "_MMStack_Pos0.ome.tif";
            ImagePlus currentFluoImage = IJ.openImage(pathToFluoMovie);
            ImagePlus zProjectedFluoImg = ImgUtils.zProject(currentFluoImage, currentFluoImage.getCalibration());
            Future future = es.submit(new FluoAnalyzer(zProjectedFluoImg.duplicate(), zProjectedFluoImg.getCalibration(),
                  soc, channel, Integer.parseInt(parameters.getChMaxNbSpot(channel)),
                  Double.parseDouble(parameters.getChSpotRaius(channel)),
                  Double.parseDouble(parameters.getChQuality(channel)), current_frame, socVisualizer,
                  parameters.useDynamic()));
            analysisTasks.put(channel, future);
            ImagePlus imgToSave = prepareImgToSave(zProjectedFluoImg, currentFluoImage, channel, current_frame,
                  Boolean.parseBoolean(parameters.getProjected()));
//            if (saveRam) {
//               IJ.log("Due to lack of RAM, MAARS will append cropped images frame by frame on disk (much slower)");
//               MAARSImgSaver imgSaver = new MAARSImgSaver(pathToFluoImgsDir);
//               //TODO
//               CopyOnWriteArrayList<Integer> cellIndex = soc.getPotentialMitosisCell();
//               for (int i : cellIndex) {
//                  Cell c = soc.getCell(i);
////                     for (Cell c : soc_){
//                  imgToSave.setRoi(c.getCellShapeRoi());
//                  for (int j = 1; j <= imgToSave.getNChannels(); j++) {
//                     ImagePlus croppedImg = new Duplicator().run(imgToSave, j, j, 1, imgToSave.getNSlices(),
//                           1, imgToSave.getNFrames());
//                     imgSaver.saveImgs(croppedImg, i, channel, true);
//                  }
//               }
//            } else {
            concatenatedFluoImgs = concatenatedFluoImgs == null ?
                  imgToSave : concatenator.concatenate(concatenatedFluoImgs, imgToSave, false);
//            }
         }
         tasksSet.add(analysisTasks);
         if (stop.get()) {
            break;
         }
      }
      es.shutdown();
      return HyperStackConverter.toHyperStack(concatenatedFluoImgs, arrayChannels.size(),
            concatenatedFluoImgs.getStack().getSize() / arrayChannels.size() / totalFrame, totalFrame,
            "xyzct", "Grayscale");
   }

   private static int extractFromOMEmetadata(JSONObject omeData, String parameter) {
      try {
         return ((JSONObject) omeData.get("IntendedDimensions")).getInt(parameter);
      } catch (JSONException e) {
         e.printStackTrace();
      }
      return 0;
   }

   private static ImagePlus loadImgOfPosition(String pathToFluoImgsDir, String pos) {
      File folder = new File(pathToFluoImgsDir);
      File[] listOfFiles = folder.listFiles();
      String fluoTiffName = null;
      for (File f:listOfFiles){
         if (Pattern.matches(".*_MMStack_"+pos+"\\..*", f.getName())){
            fluoTiffName = f.getName();
         }
      }
      IJ.run("TIFF Virtual Stack...", "open=" + pathToFluoImgsDir + File.separator + fluoTiffName);
      ImagePlus im = IJ.getImage();
      String infoProperties = im.getInfoProperty();
      IOUtils.writeToFile(pathToFluoImgsDir + File.separator + "metadata.txt", im.getProperties());
      im.close();
      String tifNameBase = fluoTiffName.split("\\.", -1)[0];
      IJ.run("Image Sequence...", "open=" + pathToFluoImgsDir + " file=" + tifNameBase + " sort");
      ImagePlus im2 = IJ.getImage();
      im2.hide();
      im2.setProperty("Info", infoProperties);
      return im2;
   }

   private static ImagePlus processStackedImg(String pathToFluoImgsDir, String pos,
                                              MaarsParameters parameters, DefaultSetOfCells soc, SOCVisualizer socVisualizer,
                                              CopyOnWriteArrayList<Map<String, Future>> tasksSet, AtomicBoolean stop) {
      ImagePlus concatenatedFluoImgs = loadImgOfPosition(pathToFluoImgsDir, pos);

      JSONObject jsonObject = null;
      try {
         jsonObject = new JSONObject(concatenatedFluoImgs.getInfoProperty());
      } catch (JSONException e) {
         e.printStackTrace();
      }

      ArrayList<String> arrayChannels = new ArrayList<>();
      try {
         for (int i = 0; i < jsonObject.getJSONArray("ChNames").length(); i++) {
            arrayChannels.add(jsonObject.getJSONArray("ChNames").getString(i));
         }
      } catch (JSONException e) {
         e.printStackTrace();
      }

//        ArrayList<String> arrayChannels = (ArrayList) map.get("ChNames");
      int totalChannel = extractFromOMEmetadata(jsonObject, "channel");
      int totalSlice = extractFromOMEmetadata(jsonObject, "z");
      int totalFrame = extractFromOMEmetadata(jsonObject, "time");
//               totalPosition = (int) ((Map)map.get("IntendedDimensions")).get("position");

      IJ.log("Re-stack image : channel " + totalChannel + ", slice " + totalSlice + ", frame " + totalFrame);
      concatenatedFluoImgs = HyperStackConverter.toHyperStack(concatenatedFluoImgs, totalChannel, totalSlice, totalFrame
            , "xyzct", "Grayscale");
      ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      for (int i = 1; i <= totalFrame; i++) {
         Map<String, Future> chAnalysisTasks = new HashMap<>();
         for (int j = 1; j <= totalChannel; j++) {
            String channel = arrayChannels.get(j - 1);
            IJ.log("Processing channel " + channel + "_" + i);
            ImagePlus zProjectedFluoImg = ImgUtils.zProject(
                  new Duplicator().run(concatenatedFluoImgs, j, j, 1, totalSlice, i, i)
                  , concatenatedFluoImgs.getCalibration());
            Future future = es.submit(new FluoAnalyzer(zProjectedFluoImg.duplicate(), zProjectedFluoImg.getCalibration(),
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
      if (Boolean.parseBoolean(parameters.getProjected())) {
         IJ.run(concatenatedFluoImgs, "Z Project...", "projection=[Max Intensity] all");
         return (IJ.getImage());
      }
      es.shutdown();
      return concatenatedFluoImgs;
   }

   public static String[] getBfImgs(MaarsParameters parameters){
      String segImgsDir = FileUtils.convertPath(parameters.getSavingPath() + File.separator + Maars_Interface.SEG + File.separator);
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

   public static void post_segmentation(MaarsParameters parameters, String[] imgNames, String[] posNbs){
      segExecuter(parameters, imgNames, posNbs);
   }

   private static void segExecuter(MaarsParameters parameters, String[] imgNames, String[] posNbs) {
      ExecutorService es = Executors.newSingleThreadExecutor();
      ImagePlus segImg = null;
      for (int i=0; i< imgNames.length;i++){
         try {
            segImg = IJ.openImage(FileUtils.convertPath(parameters.getSavingPath()) + File.separator +
                        Maars_Interface.SEG + File.separator + imgNames[i]);
            System.out.println(FileUtils.convertPath(parameters.getSavingPath()) + File.separator +
                  Maars_Interface.SEG + File.separator + imgNames[i]);
            parameters.setCalibration(String.valueOf(segImg.getCalibration().pixelWidth));
         } catch (Exception e) {
            IOUtils.printErrorToIJLog(e);
         }
         es.execute(new MaarsSegmentation(parameters, segImg, posNbs[i]));
      }
      es.shutdown();
//      try {
//         es.awaitTermination(1, TimeUnit.MINUTES);
//      } catch (InterruptedException e) {
//         e.printStackTrace();
//      }
   }

   public static void post_fluoAnalysis(String[] posNbs, String rootDir, MaarsParameters parameters) {
      AtomicBoolean stop = new AtomicBoolean(false);
      PrintStream curr_err = null;
      PrintStream curr_out = null;
      DefaultSetOfCells soc = null;
      String fluoImgsDir = FileUtils.convertPath(rootDir + File.separator + Maars_Interface.FLUO + File.separator);
      String segAnaDirPrefix = rootDir + File.separator + parameters.getSegmentationParameter(MaarsParameters.SEG_PREFIX)
            + Maars_Interface.SEGANALYSIS_SUFFIX;
      for (String posNb:posNbs) {
         soc = new DefaultSetOfCells(posNb);
         String currentPosPrefix = segAnaDirPrefix + posNb + File.separator;
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
               PrintStream ps = new PrintStream(rootDir + File.separator + "FluoAnalysis.LOG");
               curr_err = System.err;
               curr_out = System.err;
               System.setOut(ps);
               System.setErr(ps);
            } catch (FileNotFoundException e) {
               IOUtils.printErrorToIJLog(e);
            }

            String fluoTiffName = FileUtils.getShortestTiffName(fluoImgsDir);
            CopyOnWriteArrayList<Map<String, Future>> tasksSet = new CopyOnWriteArrayList<>();
            ImagePlus concatenatedFluoImgs;
            if (fluoTiffName != null) {
               concatenatedFluoImgs = processStackedImg(fluoImgsDir, posNb,
                     parameters, soc, null, tasksSet, stop);
            } else {
               concatenatedFluoImgs = processSplitImgs(fluoImgsDir, parameters, soc,
                     null, tasksSet, stop);
            }
            concatenatedFluoImgs.getCalibration().frameInterval =
                  Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL)) / 1000;
            Maars_Interface.waitAllTaskToFinish(tasksSet);
            if (!stop.get() && soc.size() != 0) {
               long startWriting = System.currentTimeMillis();
               ArrayList<String> arrayChannels = new ArrayList<>();
               Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));
               FileUtils.createFolder(rootDir + File.separator + Maars_Interface.FLUOANALYSISDIR);
               IOUtils.saveAll(soc, concatenatedFluoImgs, rootDir + File.separator, parameters.useDynamic(),
                     arrayChannels, posNb);
               IJ.log("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                     + " sec for writing results");
               if (parameters.useDynamic()) {
                  Maars_Interface.analyzeMitosisDynamic(soc, parameters);
               }
            }
         }
      }
      System.setErr(curr_err);
      System.setOut(curr_out);
      soc.reset();
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
