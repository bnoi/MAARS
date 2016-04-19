package org.micromanager.maars;

import org.micromanager.segmentPombe.ParametersProcessor;
import org.micromanager.segmentPombe.SegPombe;
import org.micromanager.segmentPombe.SegPombeParameters;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;

/**
 * Class to segment a multiple z-stack bright field image then find and record
 * cell shape Rois
 * 
 * @author Tong LI
 *
 */
public class MaarsSegmentation {
	private MaarsParameters parameters;
	private SegPombeParameters segPombeParam;
	private boolean roiDetected = false;
	private ResultsTable rt;

	/**
	 * Constructor :
	 * 
	 * @param parameters
	 *            : MAARS parameters (see class MaarsParameters)
	 * @param positionX
	 *            : current X coordinate of microscope's view.
	 * @param positionY
	 *            : current Y coordinate of microscope's view.
	 */
	public MaarsSegmentation(MaarsParameters parameters, String positionX, String positionY) {

		this.parameters = parameters;
	}

	/**
	 * Get the parameters and use them to segment cells
	 * 
	 * @param img
	 *            : image to segmente
	 */
	public void segmentation(ImagePlus img, String pathToSegDir) {

		IJ.log("Prepare parameters for segmentation...");
		segPombeParam = new SegPombeParameters();

		segPombeParam.setImageToAnalyze(img);
		segPombeParam.setSavingPath(pathToSegDir);

		segPombeParam.setFilterAbnormalShape(
				Boolean.parseBoolean(parameters.getSegmentationParameter(MaarsParameters.FILTER_SOLIDITY)));

		segPombeParam.setFiltrateWithMeanGrayValue(
				Boolean.parseBoolean(parameters.getSegmentationParameter(MaarsParameters.FILTER_MEAN_GREY_VALUE)));
		segPombeParam.getImageToAnalyze().getCalibration().pixelDepth = Double
				.parseDouble(parameters.getSegmentationParameter(MaarsParameters.STEP));
		// Calibrate parameters
		ParametersProcessor process = new ParametersProcessor(segPombeParam);

		process.checkImgUnitsAndScale();
		process.changeScale(
				Integer.parseInt(parameters.getSegmentationParameter(MaarsParameters.NEW_MAX_WIDTH_FOR_CHANGE_SCALE)),
				Integer.parseInt(parameters.getSegmentationParameter(MaarsParameters.NEW_MAX_HEIGTH_FOR_CHANGE_SCALE)));

		segPombeParam = process.getParameters();

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
		// Main segmentation process
		IJ.log("Begin segmentation...");
		SegPombe segPombe = new SegPombe(segPombeParam);
		segPombe.createCorrelationImage();
		segPombe.convertCorrelationToBinaryImage();
		segPombe.analyseAndFilterParticles();
		segPombe.showAndSaveResultsAndCleanUp();
		IJ.log("Segmentation done");
		this.rt = segPombe.getRoiMeasurements();
		if (segPombe.roiDetected()) {
			this.roiDetected = true;
		} else {
			IJ.log("No ROI detected!! Stop here!");
		}
	}

	/**
	 * 
	 * @return if no Roi detected
	 */
	public boolean roiDetected() {
		return this.roiDetected;
	}

	public SegPombeParameters getSegPombeParam() {
		return this.segPombeParam;
	}

	public ResultsTable getRoiMeasurements() {
		return this.rt;
	}
}
