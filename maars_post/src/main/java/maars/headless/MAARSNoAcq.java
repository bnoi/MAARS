package maars.headless;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import maars.agents.SetOfCells;
import maars.cellAnalysis.FluoAnalyzer;
import maars.display.SOCVisualizer;
import maars.io.IOUtils;
import maars.main.MaarsParameters;
import maars.main.MaarsSegmentation;
import maars.main.Maars_Interface;
import maars.utils.FileUtils;
import maars.utils.ImgUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 22, 2015
 */
public class MAARSNoAcq implements Runnable {
   private AtomicBoolean stop_ = new AtomicBoolean(false);
   private PrintStream curr_err;
   private PrintStream curr_out;
   private MaarsParameters parameters;
   private SetOfCells soc_;
   private String rootDir;
   private SOCVisualizer socVisualizer_;

   MAARSNoAcq(MaarsParameters parameters, SOCVisualizer socVisualizer,
                     SetOfCells soc) {
      this.parameters = parameters;
      rootDir = parameters.getSavingPath();
      soc_ = soc;
      socVisualizer_ = socVisualizer;
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

   private static int extractFromOMEmetadata(JSONObject omeData, String parameter) {
      try {
         return ((JSONObject) omeData.get("IntendedDimensions")).getInt(parameter);
      } catch (JSONException e) {
         e.printStackTrace();
      }
      return 0;
   }

   private static ImagePlus loadImg(String pathToFluoImgsDir, String fluoTiffName) {
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

   private static ImagePlus processSplitImgs(String pathToFluoImgsDir, MaarsParameters parameters, SetOfCells soc,
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

   private static ImagePlus processStackedImg(String pathToFluoImgsDir, String fluoTiffName,
                                              MaarsParameters parameters, SetOfCells soc, SOCVisualizer socVisualizer,
                                              CopyOnWriteArrayList<Map<String, Future>> tasksSet, AtomicBoolean stop) {
      ImagePlus concatenatedFluoImgs = loadImg(pathToFluoImgsDir, fluoTiffName);

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

   private ArrayList<String[]> getAcqPositions() {
      ArrayList<String[]> acqPos = new ArrayList<>();
      String[] listAcqNames = new File(rootDir).list();
      String pattern = "(X)(\\d+)(_)(Y)(\\d+)(_FLUO)";
      assert listAcqNames != null;
      for (String acqName : listAcqNames) {
         if (Pattern.matches(pattern, acqName)) {
            acqPos.add(new String[]{acqName.split("_", -1)[0].substring(1),
                  acqName.split("_", -1)[1].substring(1)});
         }
      }
      return acqPos;
   }

   @Override
   public void run() {
      // Start time
      long start = System.currentTimeMillis();
      String pathToSegDir = FileUtils.convertPath(rootDir + File.separator + Maars_Interface.SEG + File.separator);
      String[] posNbs = Maars_Interface.post_segmentation(pathToSegDir,parameters);
      String fluoImgsDir = FileUtils.convertPath(rootDir + File.separator + Maars_Interface.FLUO + File.separator);
      String segAnaDirPrefix = rootDir + File.separator + Maars_Interface.SEGANALYSISDIR;
      for (String posNb:posNbs) {
         String currentPosPrefix = segAnaDirPrefix + posNb + File.separator;
         String currentZipPath = currentPosPrefix + "ROI.zip";
         if (FileUtils.exists(currentZipPath)) {
            // from Roi.zip initialize a set of cell
            soc_.loadCells(currentZipPath);
            IJ.open(currentPosPrefix + "Results.csv");
            ResultsTable rt = ResultsTable.getResultsTable();
            ResultsTable.getResultsWindow().close(false);
            soc_.setRoiMeasurementIntoCells(rt);
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
               concatenatedFluoImgs = processStackedImg(fluoImgsDir, fluoTiffName,
                     parameters, soc_, socVisualizer_, tasksSet, stop_);
            } else {
               concatenatedFluoImgs = processSplitImgs(fluoImgsDir, parameters, soc_,
                     socVisualizer_, tasksSet, stop_);
            }
            concatenatedFluoImgs.getCalibration().frameInterval =
                  Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL)) / 1000;
            Maars_Interface.waitAllTaskToFinish(tasksSet);
            if (!stop_.get() && soc_.size() != 0) {
               long startWriting = System.currentTimeMillis();
               ArrayList<String> arrayChannels = new ArrayList<>();
               Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));
               IOUtils.saveAll(soc_, concatenatedFluoImgs, rootDir + File.separator, parameters.useDynamic(),
                     arrayChannels, Integer.valueOf(posNb));
               IJ.log("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                     + " sec for writing results");
               if (parameters.useDynamic()) {
                  if (IJ.isWindows()) {
                     pathToSegDir = FileUtils.convertPathToLinuxType(pathToSegDir);
                  }
                  Maars_Interface.analyzeMitosisDynamic(soc_, parameters,
                        rootDir + File.separator, Integer.valueOf(posNb));
               }
            }
         }
      }
      System.setErr(curr_err);
      System.setOut(curr_out);
      soc_.reset();
      soc_ = null;
      if (!stop_.get()) {
         IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing all fields");
      }
   }
}