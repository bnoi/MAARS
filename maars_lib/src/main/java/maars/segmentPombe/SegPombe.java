package maars.segmentPombe;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.WaitForUserDialog;
import ij.io.FileSaver;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import maars.io.IOUtils;

import javax.swing.*;
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
   private FloatProcessor integratedProcessor;
   private ImagePlus binImage;
   private ImagePlus imgCorrTemp;
   private ResultsTable resultTable;
   private RoiManager roiManager;
   // Options related to display and save
   private boolean showIntegratedImg;
   private boolean showBinaryImg;
   private boolean showDataFrame;
   private boolean showFocusImage;
   private boolean saveIntegratedImg;
   private boolean saveBinaryImg;
   private boolean saveDataFrame;
   private boolean saveFocusImage;
   private boolean saveRoi;
   private int direction;
   private PrintStream ps;
   private PrintStream curr_err;
   private PrintStream curr_out;

   public static final String BINARY = "Binary";
   public static final String INTEGRATED = "Integrated";
   public static final String FOCUS = "Focus";
   public static final String RESULTS = "Results";
   public static final String ROI = "ROI";
   public static final String SEGLOG = "Segmentation.log";

   /**
    * Constructor
    *
    * @param parameters parameters for segmentation
    */
   public SegPombe(SegPombeParameters parameters) {
      this.imageToAnalyze = parameters.getImageToAnalyze();
      this.savingPath = parameters.getSavingPath();
      try {
         ps = new PrintStream(savingPath + File.separator + SEGLOG);
         curr_err = System.err;
         curr_out = System.out;
         System.setOut(ps);
         System.setErr(ps);
      } catch (FileNotFoundException e) {
         IOUtils.printErrorToIJLog(e);
      }

      this.sigma = parameters.getSigma();
      this.filterAbnormalShape = parameters.filterAbnormalShape();
      this.filtrateWithMeanGrayValue = parameters.filtrateWithMeanGrayValue();
      this.minParticleInMicron = parameters.getMinParticleSize();
      this.maxParticleInMicron = parameters.getMaxParticleSize();
      this.direction = parameters.getDirection();

      // ResultOptions
      this.showIntegratedImg = parameters.showIntegratedImg();
      this.showBinaryImg = parameters.showBinaryImg();
      this.showDataFrame = parameters.showDataFrame();
      this.showFocusImage = parameters.showFocusImage();

      this.saveIntegratedImg = parameters.saveIntegratedImg();
      this.saveBinaryImg = parameters.saveBinaryImg();
      this.saveDataFrame = parameters.saveDataFrame();
      this.saveFocusImage = parameters.saveFocusImage();
      this.saveRoi = parameters.saveRoi();
      this.zFocus = parameters.getFocusSlide();

      getFocusImage();

      filterAbnormalShape = parameters.filterAbnormalShape();
      filtrateWithMeanGrayValue = parameters.filtrateWithMeanGrayValue();
      this.solidityThreshold = parameters.getSolidityThreshold();
      this.meanGreyValueThreshold = parameters.getMeanGreyValueThreshold();

   }

   private void getFocusImage() {
      System.out.println("get Focus Image");

      imageToAnalyze.setZ(Math.round(zFocus));

      focusImg = new ImagePlus("FocusImage", imageToAnalyze.getProcessor().duplicate());

      if (imageToAnalyze.getCalibration().scaled()) {
         focusImg.setCalibration(imageToAnalyze.getCalibration());
      }

      if (saveFocusImage) {
         IJ.run(focusImg, "Enhance Contrast", "saturated=0.35");
         FileSaver fileSaver = new FileSaver(focusImg);
         fileSaver.saveAsTiff(savingPath + File.separator + FOCUS + ".tif");
      }
      imageToAnalyze.flatten();
      System.out.println(FOCUS + " Image saved.");
   }

   /**
    * Create an image Integrated where each pixel corresponds to the
    * Integrated of a specific curve see equation in computeIntegrated object
    */
   public void createIntegratedImage() {

      System.out.println("creating Integrated image");
      System.out.println("Width : " + String.valueOf(imageToAnalyze.getWidth()) + ", Height : "
            + String.valueOf(imageToAnalyze.getHeight()));
      int nbProcessor = Runtime.getRuntime().availableProcessors();
      System.out.println("Compute Integrate with " + nbProcessor + " processor");
      ImageSplitter splitter = new ImageSplitter(imageToAnalyze, nbProcessor);
      int xPosition = 0;
      ImagePlus subImg;
      long start = System.currentTimeMillis();
      ExecutorService executor = Executors.newFixedThreadPool(nbProcessor);
      Map<Integer, Future<FloatProcessor>> map = new HashMap<>();
      Future<FloatProcessor> task;
      int[] widths = splitter.getWidths();
      for (int i = 0; i < nbProcessor; i++) {
         if (i == 0) {
            subImg = splitter.crop(xPosition, widths[1]);
            task = executor.submit(new ComputeImageIntegrate(subImg, zFocus, sigma, direction));
            map.put(xPosition, task);
            xPosition += widths[1];
         } else {
            IJ.showStatus("Computing Integrated image");
            subImg = splitter.crop(xPosition, widths[0]);
            task = executor.submit(new ComputeImageIntegrate(subImg, zFocus, sigma, direction));
            map.put(xPosition, task);
            xPosition += widths[0];
         }
      }
      integratedProcessor = new FloatProcessor(imageToAnalyze.getWidth(), imageToAnalyze.getHeight());
      try {
         for (int xPos : map.keySet()) {
            FloatProcessor processor = map.get(xPos).get();
            for (int x = 0; x < processor.getWidth(); x++) {
               IJ.showStatus("Rendering Integrated image");
               for (int y = 0; y < processor.getHeight(); y++) {
                  integratedProcessor.putPixel(x + xPos, y, processor.get(x, y));
               }
            }
         }
      } catch (InterruptedException | ExecutionException e) {
         IOUtils.printErrorToIJLog(e);
      }
      executor.shutdown();
      IJ.log("Segmentation took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec");
   }

   /**
    * This method set a threshold with Ostu method on the Integrated image and
    * convert it into Binary Image
    *
    * @param batchMode batch mode for optimization of segmentation
    */
   public void convertIntegratedToBinaryImage(Boolean batchMode, Double tolerance) {

      System.out.println("Convert Integrated image to binary image");

      ByteProcessor byteImage = integratedProcessor.convertToByteProcessor(true);
      byteImage.setAutoThreshold(AutoThresholder.Method.Otsu, true, BinaryProcessor.BLACK_AND_WHITE_LUT);

      // image pre-processing
      IJ.run("Options...", "iterations=1 count=1");
      byteImage.dilate();
      byteImage.erode();
      byteImage.applyLut();
      // if the thresholding and the making binary image produced a white
      // background, change it
      if (byteImage.getStatistics().mode > 127) {
         System.out.println("Invert image");
         byteImage.invert();
      }
      BinaryProcessor binImage = new BinaryProcessor(byteImage);
      this.binImage = new ImagePlus("binary Image of " + imageToAnalyze.getShortTitle(), binImage);

      if (imageToAnalyze.getCalibration().scaled()) {
         this.binImage.setCalibration(imageToAnalyze.getCalibration());
      }

      if (!batchMode || tolerance == Double.MAX_VALUE) {
         this.binImage.show();
         WaitForUserDialog waitForUserDialog = new WaitForUserDialog("Optimize (or not) segmentation of "+
               imageToAnalyze.getShortTitle()+ ", and click ok.");
         JButton adjWaterButton = new JButton("Adjustable Watershed");
         adjWaterButton.addActionListener(actionEvent -> {
            //IJ.run(this.binImage, "Adjustable_Watershed.java","");
            IJ.run(this.binImage, "Compile and Run...",
                  "compile="+IJ.getDirectory("plugins") +"Adjustable_Watershed.java");
         });
         waitForUserDialog.setAlwaysOnTop(false);
         waitForUserDialog.setLayout(new BorderLayout());
         waitForUserDialog.add(adjWaterButton, BorderLayout.SOUTH);
         waitForUserDialog.setMinimumSize(new Dimension(200, 130));
         waitForUserDialog.show();
         this.binImage.hide();
      } else {
         System.out.println("no improvement on binary");
//         IJ.run(this.binImage, IJ.getDirectory("plugins") +"Adjustable Watershed",
//               "tolerance=" + tolerance);
      }
   }

   /**
    * Run with output of convertIntegratedToBinaryImage as parameter. It
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

      imgCorrTemp = new ImagePlus("Integrated Image", integratedProcessor);

      ParticleAnalyzer.setRoiManager(roiManager);
      ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
            ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES + ParticleAnalyzer.SHOW_PROGRESS
                  + ParticleAnalyzer.ADD_TO_MANAGER,
             Measurements.MEAN + Measurements.SHAPE_DESCRIPTORS,
            resultTable, minParticleInMicron, maxParticleInMicron);
      System.out.println("minParticleSize " + minParticleInMicron + " maxParticleSize " + maxParticleInMicron);
      System.out.println("Analyse particles on " + binImage.getTitle() + " ...");

      particleAnalyzer.analyze(binImage);
      System.out.println("Done");
      Integer nbRoi = roiManager.getCount();
      ArrayList<CellFilter> filters = new ArrayList<>();
      if (filtrateWithMeanGrayValue) {
         filters.add(new CellFilter(ResultsTable.MEAN, meanGreyValueThreshold, Double.MAX_VALUE));
      }
      if (filterAbnormalShape) {
         filters.add(new CellFilter(ResultsTable.SOLIDITY, solidityThreshold, Double.MAX_VALUE));
      }
      if (!nbRoi.equals(0)) {
         CellFilterFacotory facotory = new CellFilterFacotory(resultTable, imgCorrTemp);
         facotory.filterAll(filters);
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
         IJ.log("No ROI detected!! Stop here!");
      }

      if (saveDataFrame && !nbRoi.equals(0)) {
         System.out.println("saving data frame...");
         try {
            resultTable.saveAs(savingPath + File.separator + "Results.csv");
         } catch (IOException io) {
            IJ.error("Error", "Could not save DataFrame");
         }
      }

      if (showDataFrame) {
         System.out.println("display data frame");
         resultTable.show("Result");
         System.out.println("done.");
      }

      if (saveRoi && !nbRoi.equals(0)) {
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
         binImage.setTitle(BINARY);
         FileSaver fileSaver = new FileSaver(binImage);
         fileSaver.saveAsTiff(savingPath + File.separator + BINARY + ".tif");
      }
      if (showBinaryImg) {
         System.out.println("show binary image");
         binImage.show();
      } else {
         System.out.println("flush binary image");
         binImage.flush();
      }

      if (saveIntegratedImg) {
         System.out.println("save Integrated image");
         imgCorrTemp.setTitle(INTEGRATED);
         IJ.run(imgCorrTemp, "Enhance Contrast", "saturated=0.35");
         FileSaver fileSaver = new FileSaver(imgCorrTemp);
         fileSaver.saveAsTiff(savingPath + File.separator + INTEGRATED +".tif");
      }
      if (showIntegratedImg) {
         System.out.println("show Integrated image");
         imgCorrTemp.show();
      } else {
         System.out.println("flush Integrated image");
         imgCorrTemp.flush();
      }
      ps.close();
      System.setOut(curr_out);
      System.setErr(curr_err);
   }

   public FloatProcessor getintegratedProcessor() {
      return integratedProcessor;
   }
}
