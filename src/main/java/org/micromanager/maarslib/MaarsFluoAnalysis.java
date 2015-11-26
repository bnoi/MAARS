package org.micromanager.maarslib;

import org.micromanager.cellstateanalysis.Cell;
import org.micromanager.cellstateanalysis.CellChannelFactory;
import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.maars.MaarsParameters;
import org.micromanager.segmentPombe.SegPombeParameters;
import org.micromanager.utils.FileUtils;
import org.micromanager.utils.ImgUtils;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;

/**
 * Class to find and measure mitotic spindle using fluorescence image analysis
 * 
 * @author Tong LI
 *
 */
public class MaarsFluoAnalysis {

	private MaarsParameters parameters;
	private SetOfCells soc;
	private String pathToFluoDir;
	private double positionX;
	private double positionY;
	private ImagePlus fluoImg;
	private ImagePlus focusImg;
	private Calibration bfImgCal;
	private CellChannelFactory currentFactory;
	private int currentFrame;

	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            : parameters used for algorithm
	 */
	public MaarsFluoAnalysis(MaarsParameters parameters,
			SegPombeParameters segParam, double positionX, double positionY) {
		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
		this.pathToFluoDir = FileUtils.convertPath(parameters.getSavingPath()
				+ "/movie_X" + Math.round(this.positionX) + "_Y"
				+ Math.round(this.positionY) + "_FLUO");
		if (!FileUtils.exists(pathToFluoDir)) {
			FileUtils.createFolder(pathToFluoDir);
		}
		ImagePlus bfImg = segParam.getImageToAnalyze();
		this.bfImgCal = bfImg.getCalibration();
		ImageStack stack = bfImg.getStack();
		focusImg = new ImagePlus(bfImg.getShortTitle(),
				stack.getProcessor(segParam.getFocusSlide()));
		focusImg.setCalibration(bfImg.getCalibration());

		System.out.println("Initialize set of cells...");
		soc = new SetOfCells(segParam);

	}

	/**
	 * 
	 * @return Set of cells
	 */
	public SetOfCells getSetOfCells() {
		return soc;
	}

	/**
	 * set fluo image
	 */
	public void setFluoImage(ImagePlus fluoImg) {
		this.fluoImg = fluoImg;
	}

	/**
	 * Get fluo image
	 */
	public ImagePlus getFluoImage() {
		return this.fluoImg;
	}

	/**
	 * 
	 * @param frame
	 */
	public void setCurrentFrame(int frame) {
		this.currentFrame = frame;
	}

	/**
	 * crop all cells with cells' roi
	 */
	public void cropAllCells() {

		ReportingUtils.logMessage("Cropping cell");
		for (Cell cell : soc) {
			cell.setFocusImage(ImgUtils.cropImgWithRoi(this.focusImg,
					cell.getCellShapeRoi()));
			this.fluoImg = ImgUtils.unitCmToMicron(this.fluoImg);
			double[] factors = ImgUtils.getRescaleFactor(bfImgCal,
					this.fluoImg.getCalibration());
			Roi rescaledRoi = cell.rescaleRoi(factors);
			this.fluoImg = ImgUtils.cropImgWithRoi(this.fluoImg, rescaledRoi);
			cell.setFluoImage(this.fluoImg);
			cell.addCroppedFluoSlice();
		}
	}

	/**
	 * Method to analyse an entire field
	 * 
	 * @param pathToResultsdouble
	 *            positionX, double positionY : path to save results
	 * @param channel
	 *            : channel used for this fluoimage
	 */
	public void analyzeEachCell() {
		ReportingUtils.logMessage("Detecting spots...");
		for (Cell cell : soc) {
			cell.setChannelRelated(currentFactory);
			cell.setCurrentFrame(currentFrame);
			cell.measureBfRoi();
			cell.findFluoSpotTempFunction();
			// can be optional
			FileUtils.writeSpotFeatures(parameters.getSavingPath(),
					cell.getCellNumber(), currentFactory.getChannel(),
					cell.getModelOf(currentFactory.getChannel()));
		}
		ReportingUtils.logMessage("Spots detection done...");
	}

	public void createCellChannelFactory(String currentChannel) {
		currentFactory = new CellChannelFactory(currentChannel,
				Integer.parseInt(parameters.getChMaxNbSpot(currentChannel)),
				Double.parseDouble(parameters.getChSpotRaius(currentChannel)));
	}

	public void saveCroppedImgs() {
		for (Cell cell : soc) {
			cell.saveCroppedImage(pathToFluoDir);
		}
	}
}
