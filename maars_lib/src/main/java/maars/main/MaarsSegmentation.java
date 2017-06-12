package maars.main;

import maars.segmentPombe.SegPombe;
import maars.segmentPombe.SegPombeParameters;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import maars.utils.ImgUtils;

/**
 * Class to segment a multiple z-stack bright field image then find and record
 * cell shape Rois
 *
 * @author Tong LI
 */
public class MaarsSegmentation implements Runnable {
   private MaarsParameters parameters_;
   private ResultsTable rt;
   private ImagePlus img_;
   private int posNb_;

   /**
    * * Constructor :
    *
    * @param parameters : MAARS parameters (see class MaarsParameters)
    * @param img        image to segment
    * @param posNb      position id
    */
   public MaarsSegmentation(MaarsParameters parameters, ImagePlus img, int posNb) {
      img_ = img;
      parameters_ = parameters;
      posNb_ = posNb;
   }

   public ResultsTable getRoiMeasurements() {
      return this.rt;
   }

   @Override
   public void run() {
      SegPombeParameters segPombeParam = segParameterWrapper(parameters_);
      // Main segmentation process
      IJ.log("Begin segmentation...");
      SegPombe segPombe = new SegPombe(segPombeParam, posNb_);
      segPombe.createCorrelationImage();
      segPombe.convertCorrelationToBinaryImage(Boolean.valueOf(parameters_.getBatchMode()),
            Integer.valueOf(parameters_.getSegTolerance()));
      segPombe.analyseAndFilterParticles();
      segPombe.showAndSaveResultsAndCleanUp();
      IJ.log("Segmentation done");
      this.rt = segPombe.getRoiMeasurements();
   }

   private SegPombeParameters segParameterWrapper(MaarsParameters parameters) {
      IJ.log("Prepare parameters for segmentation...");
      SegPombeParameters segPombeParam = new SegPombeParameters();

      segPombeParam.setImageToAnalyze(img_);
      segPombeParam.setSavingPath(parameters.getSavingPath());

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
            (int) Math.round(Double.parseDouble(parameters.getSegmentationParameter(MaarsParameters.CELL_SIZE))
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
      IJ.log("Done.");
      return segPombeParam;
   }
}
