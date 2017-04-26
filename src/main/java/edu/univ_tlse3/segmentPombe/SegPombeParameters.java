package edu.univ_tlse3.segmentPombe;

import ij.ImagePlus;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 4, 2015
 */
public class SegPombeParameters {
    static final int WIDTH = 0;
    static final int HEIGHT = 1;
    static final int DEPTH = 2;
    static final int PIXELS = 0;
    static final int MICRONS = 1;
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

    float getSigma() {
        return sigma;
    }

    public void setSigma(float sigma) {
        this.sigma = sigma;
    }

    boolean changeScale() {
        return changeScale;
    }

    void setChangeScale(boolean changeScale) {
        this.changeScale = changeScale;
    }

    int getMaxWidth() {
        return maxWidth;
    }

    void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    int getMaxHeight() {
        return maxHeight;
    }

    void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    int getDirection() {
        return direction;
    }

    void setDirection(int direction) {
        this.direction = direction;
    }

    boolean showCorrelationImg() {
        return showCorrelationImg;
    }

    void setShowCorrelationImg(boolean showCorrelationImg) {
        this.showCorrelationImg = showCorrelationImg;
    }

    boolean showBinaryImg() {
        return showBinaryImg;
    }

    void setShowBinaryImg(boolean showBinaryImg) {
        this.showBinaryImg = showBinaryImg;
    }

    boolean showDataFrame() {
        return showDataFrame;
    }

    void setShowDataFrame(boolean showDataFrame) {
        this.showDataFrame = showDataFrame;
    }

    boolean showFocusImage() {
        return showFocusImage;
    }

    void setShowFocusImage(boolean showFocusImage) {
        this.showFocusImage = showFocusImage;
    }

    boolean saveCorrelationImg() {
        return saveCorrelationImg;
    }

    void setSaveCorrelationImg(boolean saveCorrelationImg) {
        this.saveCorrelationImg = saveCorrelationImg;
    }

    boolean saveBinaryImg() {
        return saveBinaryImg;
    }

    void setSaveBinaryImg(boolean saveBinaryImg) {
        this.saveBinaryImg = saveBinaryImg;
    }

    boolean saveDataFrame() {
        return saveDataFrame;
    }

    void setSaveDataFrame(boolean saveDataFrame) {
        this.saveDataFrame = saveDataFrame;
    }

    boolean saveFocusImage() {
        return saveFocusImage;
    }

    void setSaveFocusImage(boolean saveFocusImage) {
        this.saveFocusImage = saveFocusImage;
    }

    boolean saveRoi() {
        return saveRoi;
    }

    void setSaveRoi(boolean saveRoi) {
        this.saveRoi = saveRoi;
    }

    public double[] getScales() {
        return scales;
    }

    void setScale(double[] scales) {
        this.scales = scales;
    }

    double getScale(final int INDEX) {
        return this.scales[INDEX];
    }

    double getMinParticleSize() {
        return minParticleSize;
    }

    public void setMinParticleSize(double minParticleSize) {
        this.minParticleSize = minParticleSize;
    }

    double getMaxParticleSize() {
        return maxParticleSize;
    }

    public void setMaxParticleSize(double maxParticleSize) {
        this.maxParticleSize = maxParticleSize;
    }

    double getSolidityThreshold() {
        return solidityThreshold;
    }

    public void setSolidityThreshold(double solidityThreshold) {
        this.solidityThreshold = solidityThreshold;
    }

    double getMeanGreyValueThreshold() {
        return meanGreyValueThreshold;
    }

    public void setMeanGreyValueThreshold(double meanGreyValueThreshold) {
        this.meanGreyValueThreshold = meanGreyValueThreshold;
    }

    boolean filterAbnormalShape() {
        return filterAbnormalShape;
    }

    public void setFilterAbnormalShape(boolean filterAbnormalShape) {
        this.filterAbnormalShape = filterAbnormalShape;
    }

    boolean filtrateWithMeanGrayValue() {
        return filtrateWithMeanGrayValue;
    }

    public void setFiltrateWithMeanGrayValue(boolean filtrateWithMeanGrayValue) {
        this.filtrateWithMeanGrayValue = filtrateWithMeanGrayValue;
    }

    int getFocusSlide() {
        return focusSlide;
    }

    void setFocusSlide(int focusSlide) {
        this.focusSlide = focusSlide;
    }
}
