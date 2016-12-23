package edu.univ_tlse3.segmentPombe;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.io.FileSaver;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import org.micromanager.internal.utils.ReportingUtils;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 4, 2015
 */
public class SegPombe {

   private static String bf = "BF";
   private String savingPath;
   private ImagePlus imageToAnalyze;
   private ImagePlus focusImg;
   private float sigma;
   private float zFocus;
   private double minParticleInMicron;
   private double maxParticleInMicron;
   private double solidityThreshold;
   private double meanGreyValueThreshold;
   private boolean filterAbnormalShape;
   private boolean filtrateWithMeanGrayValue;
   // Variables to get results
   private FloatProcessor imgCorrTempProcessor;
   private ImagePlus binImage;
   private ImagePlus imgCorrTemp;
   private ResultsTable resultTable;
   private RoiManager roiManager;
   // Options related to display and save
   private boolean showCorrelationImg;
   private boolean showBinaryImg;
   private boolean showDataFrame;
   private boolean showFocusImage;
   private boolean saveCorrelationImg;
   private boolean saveBinaryImg;
   private boolean saveDataFrame;
   private boolean saveFocusImage;
   private boolean saveRoi;
   private int direction;
   private boolean roiDetected = true;
   private PrintStream ps;
   private PrintStream curr_err;
   private PrintStream curr_out;

   /**
    * Constructor
    *
    * @param parameters parameters for segmentation
    */
   public SegPombe(SegPombeParameters parameters) {
      this.imageToAnalyze = parameters.getImageToAnalyze();
      this.savingPath = parameters.getSavingPath();

      try {
         ps = new PrintStream(savingPath + File.separator + bf + "_Segmentation.LOG");
         curr_err = System.err;
         curr_out = System.out;
         System.setOut(ps);
         System.setErr(ps);
      } catch (FileNotFoundException e) {
         IJ.error(e.toString());;
      }

      this.sigma = parameters.getSigma();
      this.filterAbnormalShape = parameters.filterAbnormalShape();
      this.filtrateWithMeanGrayValue = parameters.filtrateWithMeanGrayValue();
      this.minParticleInMicron = parameters.getMinParticleSize();
      this.maxParticleInMicron = parameters.getMaxParticleSize();
      this.direction = parameters.getDirection();

      // ResultOptions
      this.showCorrelationImg = parameters.showCorrelationImg();
      this.showBinaryImg = parameters.showBinaryImg();
      this.showDataFrame = parameters.showDataFrame();
      this.showFocusImage = parameters.showFocusImage();

      this.saveCorrelationImg = parameters.saveCorrelationImg();
      this.saveBinaryImg = parameters.saveBinaryImg();
      this.saveDataFrame = parameters.saveDataFrame();
      this.saveFocusImage = parameters.saveFocusImage();
      this.saveRoi = parameters.saveRoi();
      this.zFocus = parameters.getFocusSlide();

      getFocusImage();

      this.solidityThreshold = parameters.getSolidityThreshold();
      this.meanGreyValueThreshold = parameters.getMeanGreyValueThreshold();

   }

   private void getFocusImage() {
      System.out.println("get Focus Image");

      imageToAnalyze.setZ(Math.round(zFocus));

      focusImg = new ImagePlus(bf + "_FocusImage", imageToAnalyze.getProcessor().duplicate());

      if (imageToAnalyze.getCalibration().scaled()) {
         focusImg.setCalibration(imageToAnalyze.getCalibration());
      }

      if (saveFocusImage) {
         IJ.run(focusImg, "Enhance Contrast", "saturated=0.35");
         FileSaver fileSaver = new FileSaver(focusImg);
         fileSaver.saveAsTiff(savingPath + File.separator + bf + "_FocusImage.tif");
      }
      imageToAnalyze.flatten();
      System.out.println("FocusImage saved.");
   }

   /**
    * Create an image correlation where each pixel corresponds to the
    * correlation of a specific curve see equation in computeCorrelation object
    */
   public void createCorrelationImage() {

      System.out.println("creating correlation image");
      System.out.println("Width : " + String.valueOf(imageToAnalyze.getWidth()) + ", Height : "
              + String.valueOf(imageToAnalyze.getHeight()));
      int nbProcessor = Runtime.getRuntime().availableProcessors();
      System.out.println("Compute correlation with " + nbProcessor + " processor");
      ImageSplitter splitter = new ImageSplitter(imageToAnalyze, nbProcessor);
      int xPosition = 0;
      ImagePlus subImg;
      long start = System.currentTimeMillis();
      ExecutorService executor = Executors.newFixedThreadPool(nbProcessor);
      Map<Integer, Future<FloatProcessor>> map = new HashMap<Integer, Future<FloatProcessor>>();
      Future<FloatProcessor> task;
      int[] widths = splitter.getWidths();
      for (int i = 0; i < nbProcessor; i++) {
         if (i == 0) {
            subImg = splitter.crop(xPosition, widths[1]);
            task = executor.submit(new ComputeImageCorrelation(subImg, zFocus, sigma, direction));
            map.put(xPosition, task);
            xPosition += widths[1];
         } else {
            IJ.showStatus("Computing correlation image");
            subImg = splitter.crop(xPosition, widths[0]);
            task = executor.submit(new ComputeImageCorrelation(subImg, zFocus, sigma, direction));
            map.put(xPosition, task);
            xPosition += widths[0];
         }
      }
      imgCorrTempProcessor = new FloatProcessor(imageToAnalyze.getWidth(), imageToAnalyze.getHeight());
      try {
         for (int xPos : map.keySet()) {
            FloatProcessor processor = map.get(xPos).get();
            for (int x = 0; x < processor.getWidth(); x++) {
               IJ.showStatus("Rendering correlation image");
               for (int y = 0; y < processor.getHeight(); y++) {
                  imgCorrTempProcessor.putPixel(x + xPos, y, processor.get(x, y));
               }
            }
         }
      } catch (InterruptedException e) {
         IJ.error(e.toString());;
      } catch (ExecutionException e) {
         IJ.error(e.toString());;
      }
      executor.shutdown();
      IJ.log("Segmentation took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec");
   }

   /**
    * This method set a threshold with Ostu method on the correlation image and
    * convert it into Binary Image
    * @param useDynamic
    */
   public void convertCorrelationToBinaryImage(Boolean useDynamic) {

      System.out.println("Convert correlation image to binary image");

      ByteProcessor byteImage = imgCorrTempProcessor.convertToByteProcessor(true);
      byteImage.setAutoThreshold(AutoThresholder.Method.Otsu, true, BinaryProcessor.BLACK_AND_WHITE_LUT);

      // image pre-processing
      byteImage.dilate();
      byteImage.erode();
      byteImage.applyLut();
      // if the thresholding and the making binary image produced a white
      // background, change it
//      if (byteImage.getStatistics().mode > 127) {
//         System.out.println("Invert image");
//         byteImage.invert();
//      }
      BinaryProcessor binImage = new BinaryProcessor(byteImage);
      this.binImage = new ImagePlus("binary Image", binImage);

      if (imageToAnalyze.getCalibration().scaled()) {
         this.binImage.setCalibration(imageToAnalyze.getCalibration());
      }
      if (useDynamic){
         this.binImage.show();
         IJ.run("Adjustable Watershed");
         Window watershedWin =  WindowManager.getAllNonImageWindows()[0];
         while (watershedWin.isActive()){
            IJ.wait(500);
         }
         this.binImage.hide();
      }
      IJ.run(this.binImage, "Adjustable Watershed", "tolerance=15");
   }

   /**
    * Run with output of convertCorrelationToBinaryImage as parameter. It
    * analyse particles of the image and filter them according to there area,
    * and there solidity value (if requested)
    */
   public void analyseAndFilterParticles() {

      System.out.println("Segment and filtrate");

      resultTable = new ResultsTable();

      if (RoiManager.getInstance() != null) {
         roiManager = RoiManager.getInstance();

      } else {
         roiManager = new RoiManager();
      }

      imgCorrTemp = new ImagePlus("Correlation Image of " + bf, imgCorrTempProcessor);

      ParticleAnalyzer.setRoiManager(roiManager);
      ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
              ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES + ParticleAnalyzer.SHOW_PROGRESS
                      + ParticleAnalyzer.ADD_TO_MANAGER,
              Measurements.AREA + Measurements.CENTROID + Measurements.PERIMETER + Measurements.SHAPE_DESCRIPTORS
                      + Measurements.ELLIPSE,
              resultTable, minParticleInMicron, maxParticleInMicron);
      System.out.println("minParticleSize " + minParticleInMicron + " maxParticleSize " + maxParticleInMicron);
      System.out.println("Analyse particles on " + binImage.getTitle() + " ...");

      particleAnalyzer.analyze(binImage);
      System.out.println("Done");
      Integer nbRoi = roiManager.getCount();
      if (!nbRoi.equals(0)) {
         if (filterAbnormalShape || filtrateWithMeanGrayValue) {

            Roi[] roiArray;
            if (filtrateWithMeanGrayValue) {

               System.out.println("Filtering with mean grey value...");
               ArrayList<Integer> rowTodelete = new ArrayList<Integer>();
               int name = 1;
               System.out.println("- reset result table");
               resultTable.reset();

               System.out.println("- get roi as array");

               roiArray = roiManager.getRoisAsArray();
               System.out.println("- select and delete all roi from roi manager");

               roiManager.runCommand("Select All");
               roiManager.runCommand("Delete");

               System.out.println("- initialize analyser");

               Analyzer analyzer = new Analyzer(imgCorrTemp,
                       Measurements.AREA + Measurements.STD_DEV + Measurements.MIN_MAX
                               + Measurements.SHAPE_DESCRIPTORS + Measurements.MEAN + Measurements.CENTROID
                               + Measurements.PERIMETER + Measurements.ELLIPSE,
                       resultTable);

               System.out.println("- analyze each roi and add it to manager if it is wanted");
               for (Roi roi : roiArray) {
                  roi.setImage(imgCorrTemp);
                  imgCorrTemp.setRoi(roi);
                  analyzer.measure();
               }
               imgCorrTemp.deleteRoi();

               System.out.println("- delete from result table roi unwanted");
               for (int i = 0; i < resultTable.getColumn(ResultsTable.MEAN).length; i++) {

                  if (resultTable.getValue("Mean", i) <= meanGreyValueThreshold) {
                     rowTodelete.add(i);
                  } else {
                     roiArray[i].setName("" + name);
                     roiManager.addRoi(roiArray[i]);
                     name++;
                  }
               }
               deleteRowOfResultTable(rowTodelete);
               System.out.println("Filter done.");
            }

            if (filterAbnormalShape) {

               System.out.println("Filtering with solidity...");
               System.out.println("- get roi as array");
               roiArray = roiManager.getRoisAsArray();
               System.out.println("- select and delete all roi from roi manager");
               roiManager.runCommand("Select All");
               roiManager.runCommand("Delete");

               ArrayList<Integer> rowTodelete = new ArrayList<Integer>();
               int name = 1;

               System.out.println("- delete from result table roi unwanted");
               for (int i = 0; i < resultTable.getColumn(ResultsTable.SOLIDITY).length; i++) {
                  if (resultTable.getValue("Solidity", i) <= solidityThreshold) {
                     rowTodelete.add(i);
                  } else {
                     roiArray[i].setName("" + name);
                     roiManager.addRoi(roiArray[i]);
                     name++;
                  }
               }

               deleteRowOfResultTable(rowTodelete);
               System.out.println("Filter done.");
            }
         }
      } else {
         setRoiDetectedFalse();
      }
   }

   private void deleteRowOfResultTable(ArrayList<Integer> rowToDelete) {
      for (int i = 0; i < rowToDelete.size(); i++) {
         int row = rowToDelete.get(i) - i;
         resultTable.deleteRow(row);
      }
   }

   public ResultsTable getRoiMeasurements() {
      return this.resultTable;
   }

   /**
    * Method to show and saved specified results and flush unwanted results
    */
   public void showAndSaveResultsAndCleanUp() {
      Integer nbRoi = roiManager.getCount();
      if (nbRoi.equals(0)) {
         setRoiDetectedFalse();
      }

      if (saveDataFrame && roiDetected) {
         System.out.println("saving data frame...");
         try {
            resultTable.saveAs(savingPath + File.separator + bf + "_Results.csv");
         } catch (IOException io) {
            IJ.error("Error", "Could not save DataFrame");
         }
      }

      if (showDataFrame) {
         System.out.println("display data frame");
         resultTable.show("Result");
         System.out.println("done.");
      }

      if (saveRoi && roiDetected) {
         System.out.println("saving roi...");
         roiManager.runCommand("Select All");
         roiManager.runCommand("Save", savingPath + File.separator + "ROI.zip");
         roiManager.runCommand("Select All");
         roiManager.runCommand("Delete");
      }

      if (showFocusImage) {
         System.out.println("show focus image");
         focusImg.show();
      } else {
         System.out.println("flush focus image");
         focusImg.flush();
      }

      if (saveBinaryImg) {
         System.out.println("save binary image");
         binImage.setTitle(bf + "_BinaryImage");
         FileSaver fileSaver = new FileSaver(binImage);
         fileSaver.saveAsTiff(savingPath + File.separator + bf + "_BinaryImage.tif");
      }
      if (showBinaryImg) {
         System.out.println("show binary image");
         binImage.show();
      } else {
         System.out.println("flush binary image");
         binImage.flush();
      }

      if (saveCorrelationImg) {
         System.out.println("save correlation image");
         imgCorrTemp.setTitle(bf + "_CorrelationImage");
         IJ.run(imgCorrTemp, "Enhance Contrast", "saturated=0.35");
         FileSaver fileSaver = new FileSaver(imgCorrTemp);
         fileSaver.saveAsTiff(savingPath + File.separator + bf + "_CorrelationImage.tif");
      }
      if (showCorrelationImg) {
         System.out.println("show correlation image");
         imgCorrTemp.show();
      } else {
         System.out.println("flush correlation image");
         imgCorrTemp.flush();
      }
      ps.close();
      System.setOut(curr_out);
      System.setErr(curr_err);
   }

   /**
    * Return if any roi detected
    *
    * @return if MAARS got cells detected
    */
   public boolean roiDetected() {
      return this.roiDetected;
   }

   private void setRoiDetectedFalse() {
      this.roiDetected = false;
   }

}
