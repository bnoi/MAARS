package edu.univ_tlse3.maars;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.univ_tlse3.cellstateanalysis.Cell;
import edu.univ_tlse3.cellstateanalysis.FluoAnalyzer;
import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import edu.univ_tlse3.display.SOCVisualizer;
import edu.univ_tlse3.gui.MaarsFluoAnalysisDialog;
import edu.univ_tlse3.gui.MaarsMainDialog;
import edu.univ_tlse3.resultSaver.MAARSImgSaver;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.IOUtils;
import edu.univ_tlse3.utils.ImgUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.frame.RoiManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 22, 2015
 */
public class MAARSNoAcq implements Runnable {
   private PrintStream curr_err;
   private PrintStream curr_out;
   private MaarsParameters parameters;
   private SetOfCells soc_;
   private String rootDir;
   private SOCVisualizer socVisualizer_;
   private ExecutorService es_;
   public boolean skipAllRestFrames = false;
   private CopyOnWriteArrayList<Map<String, Future>> tasksSet_;

   public MAARSNoAcq(MaarsParameters parameters, SOCVisualizer socVisualizer,
                     ExecutorService es, CopyOnWriteArrayList<Map<String, Future>> tasksSet,
                     SetOfCells soc) {
      this.parameters = parameters;
      tasksSet_ = tasksSet;
      rootDir = parameters.getSavingPath();
      soc_ = soc;
      socVisualizer_ = socVisualizer;
      es_ = es;
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

   private ArrayList<Integer> getFluoAcqStructure(String pathToFluoDir){
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

   private Future process(ImagePlus zProjectedFluoImg, String channel, int frame){
      return es_.submit(new FluoAnalyzer(zProjectedFluoImg.duplicate(),zProjectedFluoImg.getCalibration(),
              soc_, channel, Integer.parseInt(parameters.getChMaxNbSpot(channel)),
              Double.parseDouble(parameters.getChSpotRaius(channel)),
              Double.parseDouble(parameters.getChQuality(channel)), frame, socVisualizer_,
              parameters.useDynamic()));
   }

   private ImagePlus prepareImgToSave(ImagePlus projected, ImagePlus notProjected, String channel, int frame){
      ImagePlus imgToSave = Boolean.parseBoolean(parameters.getProjected())?projected:notProjected;
      for (int i =1; i <= imgToSave.getStack().getSize();i++){
         imgToSave.getStack().setSliceLabel(channel+"_" + frame, i);
      }
      return imgToSave;
   }

   @Override
   public void run() {
      // Start time
      long start = System.currentTimeMillis();
      for (String[] pos : getAcqPositions()) {
         if (skipAllRestFrames) {
            break;
         }
         soc_.reset();
         String xPos = pos[0];
         String yPos = pos[1];
         IJ.log("x : " + xPos + " y : " + yPos);
         String pathToSegDir = FileUtils.convertPath(rootDir + "/X" + xPos + "_Y" + yPos);
         String pathToFluoDir = pathToSegDir + "_FLUO/";
         String pathToSegMovie = FileUtils.convertPath(pathToSegDir + "/_1/_1_MMStack_Pos0.ome.tif");
         //update saving path
         parameters.setSavingPath(pathToSegDir);
         Boolean skipSegmentation = Boolean.parseBoolean(parameters.getSkipSegmentation());
         ImagePlus segImg = null;
         try {
            segImg = IJ.openImage(pathToSegMovie);
            parameters.setCalibration(String.valueOf(segImg.getCalibration().pixelWidth));
         } catch (Exception e) {
            IOUtils.printErrorToIJLog(e);
         }
         // --------------------------segmentation-----------------------------//
         MaarsSegmentation ms = new MaarsSegmentation(parameters, segImg);
         Future future;
         if (!skipSegmentation) {
            future = es_.submit(ms);
            try {
               future.get();
            } catch (InterruptedException | ExecutionException e) {
               IOUtils.printErrorToIJLog(e);
            }
         }
         if (ms.roiDetected()) {
            soc_.reset();
            // from Roi.zip initialize a set of cell
            soc_.loadCells(pathToSegDir);
            ResultsTable rt;
            if (skipSegmentation) {
               IJ.open(pathToSegDir + File.separator +"BF_Results.csv");
               rt = ResultsTable.getResultsTable();
               ResultsTable.getResultsWindow().close(false);
            }else{
               rt = ms.getRoiMeasurements();
            }
            soc_.setRoiMeasurementIntoCells(rt);

            // ----------------start acquisition and analysis --------//
            try {
               PrintStream ps = new PrintStream(pathToSegDir + "/CellStateAnalysis.LOG");
               curr_err = System.err;
               curr_out = System.err;
               System.setOut(ps);
               System.setErr(ps);
            } catch (FileNotFoundException e) {
               IOUtils.printErrorToIJLog(e);
            }

            ArrayList<String> arrayChannels = new ArrayList<>();
            Collections.addAll(arrayChannels, parameters.getUsingChannels().split(",", -1));

            ImagePlus concatenatedFluoImgs = null;
            Boolean saveRam_ = MaarsFluoAnalysisDialog.saveRam_;
            int totalFrame;
            Concatenator concatenator = new Concatenator();
            concatenator.setIm5D(true);
            ImagePlus currentFluoImage;

            String fluoTiffName = FileUtils.getShortestTiffName(pathToFluoDir);

            if (fluoTiffName != null){
               ImagePlus im = IJ.openImage(pathToFluoDir + File.separator + fluoTiffName);
               Map<String,Object> map = new Gson().fromJson(im.getInfoProperty(), new TypeToken<HashMap<String, Object>>() {}.getType());
               ArrayList<String> channelNames = (ArrayList) map.get("ChNames");
               int totalChannel = channelNames.size();
               int totalSlice = ((Double) ((Map)map.get("IntendedDimensions")).get("z")).intValue();
//               totalPosition = (int) ((Map)map.get("IntendedDimensions")).get("position");
               String tifNameBase = fluoTiffName.split("\\.",-1)[0];
               IJ.run("Image Sequence...", "open="+pathToFluoDir+" file="+tifNameBase+"_ sort");
               ImagePlus im2 = IJ.getImage();
               concatenatedFluoImgs = concatenator.concatenate(im, im2, false);
               totalFrame = (int) concatenatedFluoImgs.getNSlices()/totalChannel/totalSlice;
               ImagePlus imp2 = HyperStackConverter.toHyperStack(concatenatedFluoImgs, totalChannel, totalSlice,totalFrame
                       , "xyzct", "Grayscale");
               imp2.show();
               for (int i=1;i<=totalFrame; i++) {
                  Map<String, Future> analysisTasks = new HashMap<>();
                  for (int j = 1; j <= totalChannel; j++) {
                     String channel = arrayChannels.get(j-1);
                     IJ.log("Processing channel " + channel + "_" + i);
                     currentFluoImage = new Duplicator().run(concatenatedFluoImgs, j, j, 1, totalSlice, i, i);
                     ImagePlus zProjectedFluoImg = ImgUtils.zProject(currentFluoImage, concatenatedFluoImgs.getCalibration());
                     future = process(zProjectedFluoImg, channel, i);
                     analysisTasks.put(channel, future);
                  }
                  tasksSet_.add(analysisTasks);
                  if (skipAllRestFrames) {
                     break;
                  }
               }
            }else{
               ArrayList<Integer> arrayImgFrames = getFluoAcqStructure(pathToFluoDir);
               totalFrame = arrayImgFrames.size();

               for (Integer current_frame : arrayImgFrames) {
                  Map<String, Future> analysisTasks = new HashMap<>();
                  for (String channel : arrayChannels) {
                     IJ.log("Processing channel " + channel + "_" + current_frame);
                     String pathToFluoMovie = pathToFluoDir + channel + "_" + current_frame + "/" + channel + "_" + current_frame + "_MMStack_Pos0.ome.tif";
                     currentFluoImage = IJ.openImage(pathToFluoMovie);
                     ImagePlus zProjectedFluoImg = ImgUtils.zProject(currentFluoImage, currentFluoImage.getCalibration());
                     future = process(zProjectedFluoImg, channel, current_frame);
                     analysisTasks.put(channel, future);
                     ImagePlus imgToSave = prepareImgToSave(zProjectedFluoImg, currentFluoImage, channel, current_frame);
                     if (saveRam_) {
                        IJ.log("Due to lack of RAM, MAARS will append cropped images frame by frame on disk (much slower)");
                        String croppedImgsDir = pathToFluoDir + MAARSImgSaver.croppedImgs + File.separator;
                        FileUtils.createFolder(croppedImgsDir);
                        //TODO
                        CopyOnWriteArrayList<Integer> cellIndex = soc_.getPotentialMitosisCell();
                        for (int i : cellIndex) {
                           Cell c = soc_.getCell(i);
//                     for (Cell c : soc_){
                           String pathToImg = croppedImgsDir + i + "_" + channel + ".tif";
                           ImagePlus croppedImg = ImgUtils.cropImgWithRoi(imgToSave, c.getCellShapeRoi());
                           if (!FileUtils.exists(pathToImg)) {
                              IJ.saveAsTiff(croppedImg, pathToImg);
                           } else {
                              ImagePlus new_croppedImg = concatenator.concatenate(IJ.openImage(pathToImg), croppedImg, false);
                              new_croppedImg.setRoi(croppedImg.getRoi());
                              IJ.run(new_croppedImg, "Enhance Contrast", "saturated=0.35");
                              IJ.saveAsTiff(new_croppedImg, pathToImg);
                           }
                        }
                     } else {
                        concatenatedFluoImgs = concatenatedFluoImgs == null ?
                                imgToSave : concatenator.concatenate(concatenatedFluoImgs, imgToSave, false);
                     }
                  }


                  tasksSet_.add(analysisTasks);
                  if (skipAllRestFrames) {
                     break;
                  }
               }
            }
            concatenatedFluoImgs.getCalibration().frameInterval =
                    Double.parseDouble(parameters.getFluoParameter(MaarsParameters.TIME_INTERVAL)) / 1000;
            MaarsMainDialog.waitAllTaskToFinish(tasksSet_);
            if (!skipAllRestFrames) {
               RoiManager.getInstance().reset();
               RoiManager.getInstance().close();
               if (soc_.size() != 0) {
                  long startWriting = System.currentTimeMillis();
                  if (saveRam_) {
                     MAARS.saveAll(soc_, pathToFluoDir, parameters.useDynamic());
                  } else{
                     MAARS.saveAll(soc_, concatenatedFluoImgs, pathToFluoDir, parameters.useDynamic(),
                             arrayChannels, totalFrame);
                  }
                  IJ.log("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                          + " sec for writing results");
                  if (parameters.useDynamic()) {
                     if (IJ.isWindows()) {
                        pathToSegDir = FileUtils.convertPathToLinuxType(pathToSegDir);
                     }
                     MAARS.analyzeMitosisDynamic(soc_, parameters, pathToSegDir, true);
                  }
               }else if (soc_.size() == 0){
                  try {
                     org.apache.commons.io.FileUtils.deleteDirectory(new File(pathToSegDir));
                  } catch (IOException e) {
                     IOUtils.printErrorToIJLog(e);
                  }
               }
            }
         }
      }
      System.setErr(curr_err);
      System.setOut(curr_out);
      soc_.reset();
      soc_ = null;
      if (!skipAllRestFrames){
         IJ.log("it took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec for analysing all fields");
      }
      System.gc();
   }
}