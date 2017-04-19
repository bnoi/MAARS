package edu.univ_tlse3.sigmaoptimization;

import edu.univ_tlse3.segmentPombe.ComputeCorrelation;
import edu.univ_tlse3.utils.IOUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * ImajeJ plugin used to optimise sigma parameter used in segmentation process
 * of SegmentPombe
 *
 * @author marie
 */
class SigmaOptimization implements PlugIn {

   private GenericDialog dialog;
   private double lowerSigma = 1;
   private double upperSigma = 9;
   private double step = 0.2;
   private int zf;
   private int direction = 1;
   private ImagePlus image;
   private String pathToSaveResult;

   /**
    * Create Dialog window to set parameters of algorithm
    */
   private void createDialog() {

      image = IJ.getImage();
      pathToSaveResult = image.getOriginalFileInfo().directory;

      zf = (image.getNSlices() / 2) + 1;

      dialog = new GenericDialog("Choose sigma range");
      dialog.setBounds(0, 0, 300, 400);
      dialog.addNumericField("Lower value", lowerSigma, 5);
      dialog.addNumericField("Upper value", upperSigma, 5);
      dialog.addNumericField("step", step, 5);
      dialog.addStringField("path to save results", pathToSaveResult, 20);
      dialog.addNumericField("focus slice", zf, 3);
      dialog.addNumericField("direction", direction, 3);
      Button okButton = new Button("ok");
      OkAction action = new OkAction(this);
      okButton.addActionListener(action);
      dialog.add(okButton);
   }

   /**
    * Set lower value of range to test
    *
    * @param value value to set
    */
   void setLowerSigma(double value) {
      lowerSigma = value;
   }

   /**
    * Set upper value of range to test
    *
    * @param value value to set
    */
   void setUpperSigma(double value) {
      upperSigma = value;
   }

   /**
    * Set step of range to test
    *
    * @param value value to set
    */
   void setStep(double value) {
      step = value;
   }

   /**
    * Set path where results will be stored
    *
    * @param path path to set
    */
   public void setPath(String path) {
      pathToSaveResult = path;
   }

   /**
    * slice index corresponding to focus plane
    *
    * @param zf focus to set
    */
   void setZFocus(int zf) {
      this.zf = zf;
   }

   /**
    * @param direction direction to set
    */
   void setDirection(int direction) {
      this.direction = direction;
   }

   /**
    * @return dialog
    */
   GenericDialog getDialog() {
      return dialog;
   }

   /**
    * Runs the plugin : compute mean correlation value for each sigma in the
    * range tested
    */
   public void run(String arg) {
      createDialog();
      dialog.setVisible(true);

      FileWriter fw = null;
      try {
         fw = new FileWriter(pathToSaveResult + "opti_sigma.csv");
      } catch (IOException e) {
         System.out.println("could not create writer");
         IOUtils.printErrorToIJLog(e);
      }
      assert fw != null;
      BufferedWriter bw = new BufferedWriter(fw);

      try {
         bw.write("sigma,mean_correlation");
         bw.newLine();
      } catch (IOException e) {
         System.out.println("could not write in file");
         IOUtils.printErrorToIJLog(e);
      }
      System.out.println("create roi manager");
      RoiManager manager = RoiManager.getInstance();
      System.out.println("get roi as array");
      Roi[] rois = manager.getRoisAsArray();
      System.out.println("nb of roi :" + rois.length);
      image.hide();

      for (float sigma = (float) lowerSigma; sigma <= upperSigma; sigma = sigma
              + (float) step) {
         System.out.println("for sigma = " + sigma);

         double mean = 0;
         for (Roi roi1 : rois) {
            double x = roi1.getXBase();
            double y = roi1.getYBase();
            float[] iz = new float[image.getNSlices()];

            System.out.println("for pixel x : " + x + " y : " + y);

            for (int z = 0; z < image.getNSlices(); z++) {
               image.setZ(z);
               iz[z] = image.getPixel((int) x, (int) y)[0];
            }
            ComputeCorrelation computeCorrelationImage = new ComputeCorrelation(zf, sigma
                    / (float) image.getCalibration().pixelDepth,
                    direction);
            computeCorrelationImage.preCalculateParameters(0,image.getNSlices()-1);
            mean = mean
                    + computeCorrelationImage.integrate(iz);
         }
         mean = mean / rois.length;
         System.out.println("mean = " + mean);
         try {
            bw.write(sigma + "," + mean);
            bw.newLine();
         } catch (IOException e) {
            System.out.println("could not write in file");
            IOUtils.printErrorToIJLog(e);
         }

      }
      try {
         bw.close();
      } catch (IOException e) {
         System.out.println("could not close writer");
         IOUtils.printErrorToIJLog(e);
      }
   }
}
