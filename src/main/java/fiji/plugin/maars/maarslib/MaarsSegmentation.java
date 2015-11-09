package fiji.plugin.maars.maarslib;

import java.io.FileWriter;
import java.io.IOException;

import org.micromanager.internal.utils.ReportingUtils;

import fiji.plugin.maars.segmentPombe.ParametersProcessing;
import fiji.plugin.maars.segmentPombe.SegPombe;
import fiji.plugin.maars.segmentPombe.SegPombeParameters;
import fiji.plugin.maars.utils.FileUtils;
import ij.IJ;
import ij.ImagePlus;

/**
 * Class to segment a multiple z-stack bright field image then find and record
 * cell shape Rois
 * 
 * @author Tong LI
 *
 */
public class MaarsSegmentation {
	private AllMaarsParameters parameters;
	private String pathToSegMovie;
	private String pathToSegDir;
	private SegPombeParameters segPombeParam;
	private boolean roiDetected = false;

	/**
	 * Constructor :
	 * 
	 * @param parameters
	 *            : MAARS parameters (see class AllMaarsParameters)
	 * @param positionX
	 *            : current X coordinate of microscope's view.
	 * @param positionY
	 *            : current Y coordinate of microscope's view.
	 */
	public MaarsSegmentation(AllMaarsParameters parameters, double positionX, double positionY) {

		this.parameters = parameters;
		this.pathToSegDir = FileUtils.convertPath(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.SAVING_PATH).getAsString() + "/movie_X"
						+ Math.round(positionX) + "_Y" + Math.round(positionY) + "/");
		this.pathToSegMovie = FileUtils.convertPath(pathToSegDir + "MMStack.ome.tif");
	}

	/**
	 * Get the parameters and use them to segment cells
	 */
	public void segmentation() {

		ReportingUtils.logMessage("Segmentation movie path : " + pathToSegMovie);
		ImagePlus img = null;
		if (FileUtils.isValid(pathToSegMovie)) {
			img = IJ.openImage(pathToSegMovie);
		} else {
			IJ.error("Path not valid");
		}

		segPombeParam = new SegPombeParameters();

		segPombeParam.setImageToAnalyze(img);
		segPombeParam.setSavingPath(pathToSegDir);

		segPombeParam.setFilterAbnormalShape(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.FILTER_SOLIDITY).getAsBoolean());

		segPombeParam.setFiltrateWithMeanGrayValue(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.FILTER_MEAN_GREY_VALUE).getAsBoolean());

		segPombeParam.getImageToAnalyze().getCalibration().pixelDepth = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject().get(AllMaarsParameters.STEP)
				.getAsDouble();

		ParametersProcessing process = new ParametersProcessing(segPombeParam);

		process.checkImgUnitsAndScale();
		process.changeScale(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.NEW_MAX_WIDTH_FOR_CHANGE_SCALE).getAsInt(),
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.NEW_MAX_HEIGTH_FOR_CHANGE_SCALE).getAsInt());

		segPombeParam = process.getParameters();

		segPombeParam.setSigma(
				(int) Math.round(parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.CELL_SIZE).getAsDouble()
						/ parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.STEP).getAsDouble()));

		segPombeParam.setMinParticleSize(
				(int) Math.round(parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.MINIMUM_CELL_AREA).getAsDouble()
						/ segPombeParam.getImageToAnalyze().getCalibration().pixelWidth)
				/ segPombeParam.getImageToAnalyze().getCalibration().pixelHeight);

		segPombeParam.setMaxParticleSize(
				(int) Math.round(parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.MAXIMUM_CELL_AREA).getAsDouble()
						/ segPombeParam.getImageToAnalyze().getCalibration().pixelWidth)
				/ segPombeParam.getImageToAnalyze().getCalibration().pixelHeight);

		segPombeParam.setSolidityThreshold(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.SOLIDITY).getAsDouble());

		segPombeParam.setMeanGreyValueThreshold(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.MEAN_GREY_VALUE).getAsDouble());

		SegPombe segPombe = new SegPombe(segPombeParam);
		segPombe.createCorrelationImage();
		segPombe.convertCorrelationToBinaryImage();
		segPombe.analyseAndFilterParticles();
		segPombe.showAndSaveResultsAndCleanUp();
		if (segPombe.roiDetected()) {
			this.roiDetected = true;
			segPombe.getRoiManager().close();
		}
	}

	/**
	 * 
	 * @return if no Roi detected
	 */
	public boolean roiDetected() {
		return this.roiDetected;
	}

	/**
	 * write config parameters used in current analysis
	 * 
	 */
	public void writeUsedConfig() {
		double timeInterval = parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.TIME_INTERVAL).getAsDouble() / 1000;
		int maxNbSpot = parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT).getAsInt();
		int maxWidth = parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.NEW_MAX_WIDTH_FOR_CHANGE_SCALE).getAsInt();
		int maxHeight = parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.NEW_MAX_HEIGTH_FOR_CHANGE_SCALE).getAsInt();
		double cellSize = parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.CELL_SIZE).getAsDouble();
		double segRange = parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE).getAsDouble();
		double segStep = parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.STEP).getAsDouble();
		double minCellArea = parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.MINIMUM_CELL_AREA).getAsDouble();
		double maxCellArea = parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.MAXIMUM_CELL_AREA).getAsDouble();
		double solidity = parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.SOLIDITY).getAsDouble();
		double meanGrey = parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.MEAN_GREY_VALUE).getAsDouble();
		String rootDirName = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS).getAsJsonObject()
				.get(AllMaarsParameters.SAVING_PATH).getAsString();
		double fluoRange = parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.RANGE_SIZE_FOR_MOVIE).getAsDouble();
		double fluoStep = parameters.getParametersAsJsonObject().get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.STEP).getAsDouble();
		boolean filterUnusualShape = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
				.get(AllMaarsParameters.FILTER_SOLIDITY).getAsBoolean();
		boolean filterWithMeanGrayValue = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
				.get(AllMaarsParameters.FILTER_MEAN_GREY_VALUE).getAsBoolean();
		FileWriter configFile = null;
		try {
			configFile = new FileWriter(FileUtils.convertPath(pathToSegDir + "/configUsed.txt"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			configFile.write("Interval between 2 frames:\t" + String.valueOf(timeInterval) + "\n"
					+ "Max number of spot:\t" + String.valueOf(maxNbSpot) + "\n" + "Max width of bright field:\t"
					+ String.valueOf(maxWidth) + "\n" + "Max height of bright field:\t" + String.valueOf(maxHeight)
					+ "\n" + "Typical cell size:\t" + String.valueOf(cellSize) + "\n" + "Segmentation range:\t"
					+ String.valueOf(segRange) + "\n" + "Segmentation step size:\t" + String.valueOf(segStep) + "\n"
					+ "Fluo acquisition range:\t" + String.valueOf(fluoRange) + "\n" + "Fluo acquisition step size:\t"
					+ String.valueOf(fluoStep) + "\n" + "Minimum cell area:\t" + String.valueOf(minCellArea) + "\n"
					+ "Maximum cell area:\t" + String.valueOf(maxCellArea) + "\n" + "Solidity thresold enable:\t"
					+ String.valueOf(filterUnusualShape) + "\n" + "Solidity thresold:\t" + String.valueOf(solidity)
					+ "\n" + "Gray level thresold enable:\t" + String.valueOf(filterWithMeanGrayValue) + "\n"
					+ "Gray level thresold:\t" + String.valueOf(meanGrey) + "\n" + "Root dir:\t" + rootDirName + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			configFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SegPombeParameters getSegPombeParam(){
		return this.segPombeParam;
	}
}
