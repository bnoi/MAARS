package maars.headless;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import maars.agents.DefaultSetOfCells;
import maars.cellAnalysis.FluoAnalyzer;
import maars.display.SOCVisualizer;
import maars.io.IOUtils;
import maars.main.MaarsParameters;
import maars.main.Maars_Interface;
import maars.utils.FileUtils;
import maars.utils.ImgUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Created by tong on 30/06/17.
 */
public class MaarsFluoAnalysis implements Runnable{
   String[] posNbs_;
   MaarsParameters parameters_;
   public MaarsFluoAnalysis(MaarsParameters parameters){
      String[] imgNames = Maars_Interface.getBfImgs(parameters);
      posNbs_ = Maars_Interface.getPosNbs(imgNames);
      parameters_ = parameters;
   }
   @Override
   public void run() {
      AtomicBoolean stop = new AtomicBoolean(false);
      PrintStream curr_err = null;
      PrintStream curr_out = null;
      DefaultSetOfCells soc;
      String fluoImgsDir = FileUtils.convertPath(parameters_.getSavingPath()) + File.separator +
            parameters_.getFluoParameter(MaarsParameters.FLUO_PREFIX) + File.separator;
      String segAnaDir = FileUtils.convertPath(parameters_.getSavingPath()) + File.separator +
            parameters_.getSegmentationParameter(MaarsParameters.SEG_PREFIX) + Maars_Interface.SEGANALYSIS_SUFFIX;
      for (String posNb:posNbs_) {
         ImagePlus concatenatedFluoImgs = null;
         soc = new DefaultSetOfCells(posNb);
         String currentPosPrefix = segAnaDir + posNb + File.separator;
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
               curr_out = System.err;
               System.setOut(ps);
               System.setErr(ps);
            } catch (FileNotFoundException e) {
               IOUtils.printErrorToIJLog(e);
            }
//            boolean containsTiffFile = FileUtils.containsTiffFile(fluoImgsDir);
            CopyOnWriteArrayList<Map<String, Future>> tasksSet = new CopyOnWriteArrayList<>();
//            if (containsTiffFile) {
            concatenatedFluoImgs = processStackedImg(fluoImgsDir, posNb,
                     parameters_, soc, null, tasksSet, stop);
//            } else {
//               concatenatedFluoImgs = processSplitImgs(fluoImgsDir, parameters_, soc,
//                     null, tasksSet, stop);
//            }
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
                     arrayChannels, posNb, parameters_.getFluoParameter(MaarsParameters.FLUO_PREFIX));
               IJ.log("it took " + (double) (System.currentTimeMillis() - startWriting) / 1000
                     + " sec for writing results");
               if (parameters_.useDynamic()) {
                  Maars_Interface.analyzeMitosisDynamic(soc, parameters_);
               }
            }
         }
         soc.reset();
         concatenatedFluoImgs.close();
         for (int i = 0; i <10; i++) {
            System.gc();
         }
      }
      System.setErr(curr_err);
      System.setOut(curr_out);
   }

   private static ImagePlus processStackedImg(String pathToFluoImgsDir, String pos,
                                             MaarsParameters parameters, DefaultSetOfCells soc, SOCVisualizer socVisualizer,
                                             CopyOnWriteArrayList<Map<String, Future>> tasksSet, AtomicBoolean stop) {
      ImagePlus concatenatedFluoImgs = loadImgOfPosition(pathToFluoImgsDir, pos);

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
      return concatenatedFluoImgs;
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
            ImagePlus currentFluoImage = IJ.openVirtual(pathToFluoMovie);
            ImagePlus zProjectedFluoImg = ImgUtils.zProject(currentFluoImage, currentFluoImage.getCalibration());
            Future future = es.submit(new FluoAnalyzer(zProjectedFluoImg, zProjectedFluoImg.getCalibration(),
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

   private static ImagePlus loadImgOfPosition(String pathToFluoImgsDir, String pos) {
      File[] listOfFiles = new File(pathToFluoImgsDir).listFiles();
      String fluoTiffName = null;
      for (File f:listOfFiles){
         if (Pattern.matches(".*_MMStack_"+pos+"\\..*", f.getName())){
            fluoTiffName = f.getName();
         }
      }
      IJ.log(fluoTiffName);
      HashMap map = populateSeriesImgNames(pathToFluoImgsDir + File.separator + fluoTiffName);
      String serie_number = (String) map.get(fluoTiffName.split("\\.")[0]);
      IJ.log(serie_number + " selected");
      ImagePlus im2 = ImgUtils.lociImport(pathToFluoImgsDir + File.separator + fluoTiffName, serie_number);
      return im2;
   }

   private static ImagePlus prepareImgToSave(ImagePlus projectedImg, ImagePlus notProjected, String channel, int frame,
                                             Boolean projected) {
      ImagePlus imgToSave = projected ? projectedImg : notProjected;
      for (int i = 1; i <= imgToSave.getStack().getSize(); i++) {
         imgToSave.getStack().setSliceLabel(channel + "_" + frame, i);
      }
      return imgToSave;
   }

   private static HashMap populateSeriesImgNames(String pathToTiffFile) {
      HashMap<String, String> seriesImgNames = new HashMap<>();
      IMetadata omexmlMetadata = MetadataTools.createOMEXMLMetadata();
      ImageReader reader = new ImageReader();
      reader.setMetadataStore(omexmlMetadata);
      try {
         reader.setId(pathToTiffFile);
      } catch (FormatException | IOException e) {
         e.printStackTrace();
      }
      int seriesCount = reader.getSeriesCount();
      for (int i = 0; i < seriesCount; i++) {
         reader.setSeries(i);
         String name = omexmlMetadata.getImageName(i); // this is the image name stored in the file
         String label = "series_" + (i + 1);  // this is the label that you see in ImageJ
         seriesImgNames.put(name, label);
      }
      IJ.log(seriesCount + " series registered");
      return seriesImgNames;
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
}
