package org.micromanager.maarslib;

import org.micromanager.cellstateanalysis.Cell;
import org.micromanager.cellstateanalysis.CellChannelFactory;
import org.micromanager.cellstateanalysis.SetOfCells;
import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.maars.MaarsParameters;
import org.micromanager.segmentPombe.SegPombeParameters;
import org.micromanager.utils.FileUtils;

import ij.ImagePlus;
import ij.plugin.ZProjector;

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
		if (!FileUtils.exists(pathToFluoDir)){
			FileUtils.createFolder(pathToFluoDir);
		}
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
	 * z-projection of fluoImage with max_intesity
	 */
	public void zProject() {
		ZProjector projector = new ZProjector();
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.setImage(this.fluoImg);
		projector.doProjection();
		ImagePlus imgProject = projector.getProjection();
		imgProject.setCalibration(fluoImg.getCalibration());
		this.fluoImg = imgProject;

	}

	/**
	 * crop all cells with cells' roi
	 */
	public void cropAllCells() {

		ReportingUtils.logMessage("Cropping cell");
		for (Cell cell : soc) {
			cell.setFluoImage(this.fluoImg);
			cell.rescaleRoiForFluoImg();
			cell.cropFluoImage();
			
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
