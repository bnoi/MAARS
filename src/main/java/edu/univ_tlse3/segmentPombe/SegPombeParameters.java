package edu.univ_tlse3.segmentPombe;

import ij.ImagePlus;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 4, 2015
 */
public class SegPombeParameters {
   public static final int WIDTH = 0;
   public static final int HEIGHT = 1;
   public static final int DEPTH = 2;
   public static final int PIXELS = 0;
   public static final int MICRONS = 1;
   /**
    * Class split from original CellsBoundaries to save all parameters.
    */
   // Segmenration output saving path
   private String savingPath;
   // Parameters of the algorithm
   private ImagePlus imageToAnalyze;
   private float sigma = 3;
   // it is 1 for image with cell boundaries be black then white
   // it is -1 for image with cell boundaries be white then black
   //	public static double acquisitionStep = 0.3;
   private int focusSlide = 17;
   private boolean changeScale = true;
   private int maxWidth = 1500;
   private int maxHeight = 1500;
   private int direction = -1; // this is the direction of the equation to integrate
   // Parameters to filter results
   private double minParticleSize = 500;
   private double maxParticleSize = 40000;
   private double solidityThreshold = 0.84;
   private double meanGreyValueThreshold = -177660;
   private boolean filterAbnormalShape = true;
   private boolean filtrateWithMeanGrayValue = true;
   // Options related to display and save
   private boolean showCorrelationImg = false;
   private boolean showBinaryImg = false;
   private boolean showDataFrame = false;
   private boolean showFocusImage = false;
   private boolean saveCorrelationImg = true;
   private boolean saveBinaryImg = true;
   private boolean saveDataFrame = true;
   private boolean saveFocusImage = true;
   private boolean saveRoi = true;
   // Width and Height indexes used in resolution array
   private double[] scales;

   public String getSavingPath() {
      return savingPath;
   }

   public void setSavingPath(String savingPath) {
      this.savingPath = savingPath;
   }

   public ImagePlus getImageToAnalyze() {
      return imageToAnalyze;
   }

   public void setImageToAnalyze(ImagePlus imageToAnalyze) {
      this.imageToAnalyze = imageToAnalyze;
   }

   public float getSigma() {
      return sigma;
   }

   public void setSigma(float sigma) {
      this.sigma = sigma;
   }

   public boolean changeScale() {
      return changeScale;
   }

   public void setChangeScale(boolean changeScale) {
      this.changeScale = changeScale;
   }

   public int getMaxWidth() {
      return maxWidth;
   }

   public void setMaxWidth(int maxWidth) {
      this.maxWidth = maxWidth;
   }

   public int getMaxHeight() {
      return maxHeight;
   }

   public void setMaxHeight(int maxHeight) {
      this.maxHeight = maxHeight;
   }

   public int getDirection() {
      return direction;
   }

   public void setDirection(int direction) {
      this.direction = direction;
   }

   public boolean showCorrelationImg() {
      return showCorrelationImg;
   }

   public void setShowCorrelationImg(boolean showCorrelationImg) {
      this.showCorrelationImg = showCorrelationImg;
   }

   public boolean showBinaryImg() {
      return showBinaryImg;
   }

   public void setShowBinaryImg(boolean showBinaryImg) {
      this.showBinaryImg = showBinaryImg;
   }

   public boolean showDataFrame() {
      return showDataFrame;
   }

   public void setShowDataFrame(boolean showDataFrame) {
      this.showDataFrame = showDataFrame;
   }

   public boolean showFocusImage() {
      return showFocusImage;
   }

   public void setShowFocusImage(boolean showFocusImage) {
      this.showFocusImage = showFocusImage;
   }

   public boolean saveCorrelationImg() {
      return saveCorrelationImg;
   }

   public void setSaveCorrelationImg(boolean saveCorrelationImg) {
      this.saveCorrelationImg = saveCorrelationImg;
   }

   public boolean saveBinaryImg() {
      return saveBinaryImg;
   }

   public void setSaveBinaryImg(boolean saveBinaryImg) {
      this.saveBinaryImg = saveBinaryImg;
   }

   public boolean saveDataFrame() {
      return saveDataFrame;
   }

   public void setSaveDataFrame(boolean saveDataFrame) {
      this.saveDataFrame = saveDataFrame;
   }

   public boolean saveFocusImage() {
      return saveFocusImage;
   }

   public void setSaveFocusImage(boolean saveFocusImage) {
      this.saveFocusImage = saveFocusImage;
   }

   public boolean saveRoi() {
      return saveRoi;
   }

   public void setSaveRoi(boolean saveRoi) {
      this.saveRoi = saveRoi;
   }

   public double[] getScales() {
      return scales;
   }

   public void setScale(double[] scales) {
      this.scales = scales;
   }

   public double getScale(final int INDEX) {
      return this.scales[INDEX];
   }

   public double getMinParticleSize() {
      return minParticleSize;
   }

   public void setMinParticleSize(double minParticleSize) {
      this.minParticleSize = minParticleSize;
   }

   public double getMaxParticleSize() {
      return maxParticleSize;
   }

   public void setMaxParticleSize(double maxParticleSize) {
      this.maxParticleSize = maxParticleSize;
   }

   public double getSolidityThreshold() {
      return solidityThreshold;
   }

   public void setSolidityThreshold(double solidityThreshold) {
      this.solidityThreshold = solidityThreshold;
   }

   public double getMeanGreyValueThreshold() {
      return meanGreyValueThreshold;
   }

   public void setMeanGreyValueThreshold(double meanGreyValueThreshold) {
      this.meanGreyValueThreshold = meanGreyValueThreshold;
   }

   public boolean filterAbnormalShape() {
      return filterAbnormalShape;
   }

   public void setFilterAbnormalShape(boolean filterAbnormalShape) {
      this.filterAbnormalShape = filterAbnormalShape;
   }

   public boolean filtrateWithMeanGrayValue() {
      return filtrateWithMeanGrayValue;
   }

   public void setFiltrateWithMeanGrayValue(boolean filtrateWithMeanGrayValue) {
      this.filtrateWithMeanGrayValue = filtrateWithMeanGrayValue;
   }

   public int getFocusSlide() {
      return focusSlide;
   }

   public void setFocusSlide(int focusSlide) {
      this.focusSlide = focusSlide;
   }
}
