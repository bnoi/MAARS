package fiji.plugin.maars.cellboundaries;

import ij.ImagePlus;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 4, 2015
 */
public class CBParameters {
	/**
	 * Class split from original CellsBoundaries to save all parameters.
	 */
	// Segmenration output saving path
	private String savingPath;

	// Parameters of the algorithm
	private ImagePlus imageToAnalyze;
	private ImagePlus focusImage;
	private float sigma = 3;
	private boolean willChangeScale = true;
	private int maxWidth = 1500;
	private int maxHeight = 1500;
	private int direction; // this is the direction of the equation to integrate
	// it is 1 for image with cell boundaries be black then white
	// it is -1 for image with cell boundaries be white then black

	// Options related to display and sava
	private boolean willShowCorrelationImg = false;
	private boolean willShowBinaryImg = false;
	private boolean willShowDataFrame = false;
	private boolean willShowFocusImage = false;
	private boolean willSaveCorrelationImg = true;
	private boolean willSaveBinaryImg = true;
	private boolean willSaveDataFrame = true;
	private boolean willSaveFocusImage = true;
	private boolean willSaveRoi = true;
	private boolean willFlushImageToAnalyze = false;

	// Width and Height indexes used in resolution array
	private double[] scale;
	public static final int WIDTH = 0;
	public static final int HEIGHT = 1;
	public static final int DEPTH = 2;

	// Parameters to filter results
	private double minParticleSize = 500;
	private double maxParticleSize = 40000;
	private double solidityThreshold = 0.84;
	private double meanGreyValueThreshold = -177660;
	private boolean willFiltrateUnusualShape = true;
	private boolean willFiltrateWithMeanGrayValue = true;

	//******************************Setters*****************************

	public void setImageToAnalyze(ImagePlus img) {
		this.imageToAnalyze = img;
	}

	public void setFocusImage(ImagePlus focusImage) {
		this.focusImage = focusImage;
	}

	public void setSavingPath(String path) {
		this.savingPath = path;
	}

	public void setSigma(float sigma) {
		this.sigma = sigma;
	}

	public void setChangeScale(boolean changeScale) {
		this.willChangeScale = changeScale;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	public void setScale(double[] scale) {
		this.scale = scale;
	}

	public void setSolidityThreshold(double solidityThreshold) {
		this.solidityThreshold = solidityThreshold;
	}

	public void setMeanGreyValueThreshold(double meanGreyValueThreshold) {
		this.meanGreyValueThreshold = meanGreyValueThreshold;
	}

	public void setMinParticleSize(double minParticleSize) {
		this.minParticleSize = minParticleSize;
	}

	public void setMaxParticleSize(double maxParticleSize) {
		this.maxParticleSize = maxParticleSize;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void setWillChangeScale(boolean willChangeScale) {
		this.willChangeScale = willChangeScale;
	}

	public void setWillShowCorrelationImg(boolean willShowCorrelationImg) {
		this.willShowCorrelationImg = willShowCorrelationImg;
	}

	public void setWillShowBinaryImg(boolean willShowBinaryImg) {
		this.willShowBinaryImg = willShowBinaryImg;
	}

	public void setWillShowDataFrame(boolean willShowDataFrame) {
		this.willShowDataFrame = willShowDataFrame;
	}

	public void setWillShowFocusImage(boolean willShowFocusImage) {
		this.willShowFocusImage = willShowFocusImage;
	}

	public void setWillSaveCorrelationImg(boolean willSaveCorrelationImg) {
		this.willSaveCorrelationImg = willSaveCorrelationImg;
	}

	public void setWillSaveBinaryImg(boolean willSaveBinaryImg) {
		this.willSaveBinaryImg = willSaveBinaryImg;
	}

	public void setWillSaveDataFrame(boolean willSaveDataFrame) {
		this.willSaveDataFrame = willSaveDataFrame;
	}

	public void setWillSaveFocusImage(boolean willSaveFocusImage) {
		this.willSaveFocusImage = willSaveFocusImage;
	}

	public void setWillSaveRoi(boolean willSaveRoi) {
		this.willSaveRoi = willSaveRoi;
	}

	public void setWillFlushImageToAnalyze(boolean willFlushImageToAnalyze) {
		this.willFlushImageToAnalyze = willFlushImageToAnalyze;
	}

	public void setWillFiltrateUnusualShape(boolean willFiltrateUnusualShape) {
		this.willFiltrateUnusualShape = willFiltrateUnusualShape;
	}

	public void setWillFiltrateWithMeanGrayValue(
			boolean willFiltrateWithMeanGrayValue) {
		this.willFiltrateWithMeanGrayValue = willFiltrateWithMeanGrayValue;
	}

	//****************************** Getters*****************************

	public ImagePlus getImageToAnalyze() {
		return imageToAnalyze;
	}

	public ImagePlus getFocusImage() {
		return focusImage;
	}

	public String getSavingPath() {
		return this.savingPath;
	}

	public double getSigma() {
		return this.sigma;
	}

	public boolean getChangeScale() {
		return this.willChangeScale;
	}

	public int getMaxWidth() {
		return this.maxWidth;
	}

	public int getMaxHeight() {
		return this.maxHeight;
	}

	public double getScale(final int index) {
		return scale[index];
	}

	public double getSolidityThreshold() {
		return this.solidityThreshold;
	}

	public double getMeanGreyValueThreshold() {
		return this.meanGreyValueThreshold;
	}

	public double getMinParticleSize() {
		return this.minParticleSize;
	}

	public double getMaxParticleSize() {
		return this.maxParticleSize;
	}

	public int getDirection() {
		return direction;
	}

	public double[] getScale() {
		return scale;
	}

	public boolean willChangeScale() {
		return willChangeScale;
	}

	public boolean willShowCorrelationImg() {
		return willShowCorrelationImg;
	}

	public boolean willShowBinaryImg() {
		return willShowBinaryImg;
	}

	public boolean willShowDataFrame() {
		return willShowDataFrame;
	}

	public boolean willShowFocusImage() {
		return willShowFocusImage;
	}

	public boolean willSaveCorrelationImg() {
		return willSaveCorrelationImg;
	}

	public boolean willSaveBinaryImg() {
		return willSaveBinaryImg;
	}

	public boolean willSaveDataFrame() {
		return willSaveDataFrame;
	}

	public boolean willSaveFocusImage() {
		return willSaveFocusImage;
	}

	public boolean willSaveRoi() {
		return willSaveRoi;
	}

	public boolean willFlushImageToAnalyze() {
		return willFlushImageToAnalyze;
	}

	public boolean willFiltrateUnusualShape() {
		return willFiltrateUnusualShape;
	}

	public boolean willFiltrateWithMeanGrayValue() {
		return willFiltrateWithMeanGrayValue;
	}

}
