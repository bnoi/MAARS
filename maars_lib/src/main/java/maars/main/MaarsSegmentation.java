package maars.main;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import maars.segmentPombe.SegPombe;
import maars.segmentPombe.SegPombeParameters;
import maars.utils.FileUtils;
import maars.utils.ImgUtils;

import java.io.File;

/**
 * Class to segment a multiple z-stack bright field image then find and record
 * cell shape Rois
 *
 * @author Tong LI
 */
public class MaarsSegmentation implements Runnable {
   private ResultsTable rt;
   private ImagePlus img_;
   private String posNb_;
   private SegPombeParameters segPombeParam_;
   private Boolean batchMode = false;
   private Double tolerance = Double.MAX_VALUE;

   /**
    * * Constructor :
    *
    * @param parameters : MAARS parameters (see class MaarsParameters)
    * @param img        image to segment
    * @param posNb      position id
    */
   public MaarsSegmentation(MaarsParameters parameters, ImagePlus img, String posNb) {
      img_ = img;
      posNb_ = posNb;
      batchMode = Boolean.valueOf(parameters.getBatchMode());
      tolerance = Double.valueOf(parameters.getSegTolerance());
      segPombeParam_ = segParameterWrapper(parameters);
   }

   /**
    * Constructor 2
    */
   public MaarsSegmentation(SegPombeParameters segPombeParam){
      segPombeParam_ = segPombeParam;
   }

   public ResultsTable getRoiMeasurements() {
      return this.rt;
   }

   @Override
   public void run() {
      // Main segmentation process
      IJ.log("Begin segmentation...");
      SegPombe segPombe = new SegPombe(segPombeParam_);
      segPombe.createIntegratedImage();
      segPombe.convertIntegratedToBinaryImage(batchMode,tolerance);
      segPombe.analyseAndFilterParticles();
      segPombe.showAndSaveResultsAndCleanUp();
      IJ.log("Segmentation done");
      this.rt = segPombe.getRoiMeasurements();
   }

   private SegPombeParameters segParameterWrapper(MaarsParameters parameters) {
      IJ.log("Prepare parameters for segmentation...");
      SegPombeParameters segPombeParam = new SegPombeParameters();

      segPombeParam.setImageToAnalyze(img_);
      String root = parameters.getSavingPath() + File.separator + parameters.getSegmentationParameter(MaarsParameters.SEG_PREFIX) +
            Maars_Interface.SEGANALYSIS_SUFFIX + posNb_ + File.separator;
      FileUtils.createFolder(root);
      segPombeParam.setSavingPath(root);
      segPombeParam.setSaveBinaryImg(true);
      segPombeParam.setSaveFocusImage(true);
      segPombeParam.setSaveDataFrame(true);
      segPombeParam.setSaveIntegratedImg(true);
      segPombeParam.setSaveRoi(true);

      segPombeParam.setFilterAbnormalShape(
            Boolean.parseBoolean(parameters.getSegmentationParameter(MaarsParameters.FILTER_SOLIDITY)));

      segPombeParam.setFiltrateWithMeanGrayValue(
            Boolean.parseBoolean(parameters.getSegmentationParameter(MaarsParameters.FILTER_MEAN_GREY_VALUE)));
      segPombeParam.getImageToAnalyze().getCalibration().pixelDepth = Double
            .parseDouble(parameters.getSegmentationParameter(MaarsParameters.STEP));
      // Calibrate parameters

      ImgUtils.checkImgUnitsAndScale(img_, segPombeParam);
      ImgUtils.changeScale(
            img_,
            Integer.parseInt(parameters.getSegmentationParameter(MaarsParameters.NEW_MAX_WIDTH_FOR_CHANGE_SCALE)),
            Integer.parseInt(parameters.getSegmentationParameter(MaarsParameters.NEW_MAX_HEIGTH_FOR_CHANGE_SCALE)),
            segPombeParam);

      segPombeParam.setSigma(
            (int) Math.round(Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.SIGMA))
                  / Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.STEP))));

      segPombeParam.setMinParticleSize((int) Math
            .round(Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.MINIMUM_CELL_AREA))
                  / segPombeParam.getImageToAnalyze().getCalibration().pixelWidth)
            / segPombeParam.getImageToAnalyze().getCalibration().pixelHeight);

      segPombeParam.setMaxParticleSize((int) Math
            .round(Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.MAXIMUM_CELL_AREA))
                  / segPombeParam.getImageToAnalyze().getCalibration().pixelWidth)
            / segPombeParam.getImageToAnalyze().getCalibration().pixelHeight);

      segPombeParam.setSolidityThreshold(
            Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.SOLIDITY)));

      segPombeParam.setMeanGreyValueThreshold(
            Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.MEAN_GREY_VALUE)));

      segPombeParam.setFocusSlide(Integer.parseInt(parameters.getSegmentationParameter(MaarsParameters.FOCUS)));
      segPombeParam.setDirection(Integer.parseInt(parameters.getSegmentationParameter(MaarsParameters.DIRECTION)));
      IJ.log("Done.");
      return segPombeParam;
   }

   public SegPombeParameters getSegPombeParam() {
      return segPombeParam_;
   }
}
