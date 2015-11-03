package fiji.plugin.maars.maarslib;

import java.io.FileWriter;
import java.io.IOException;

import org.micromanager.internal.utils.ReportingUtils;

import fiji.plugin.maars.cellboundaries.CellsBoundaries;
import fiji.plugin.maars.cellboundaries.CellsBoundariesIdentification;
import ij.IJ;
import ij.ImagePlus;

/**
 * Class to segment a multiple z-stack bright field image then find and record
 * cell shape Rois
 * 
 * @author marie
 *
 */
public class MaarsSegmentation {
	private AllMaarsParameters parameters;
	private String pathToSegMovie;
	private String pathToSegDir;
	private CellsBoundaries cB;
	private boolean roiDetected = true;

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
		this.pathToSegDir = AllMaarsParameters.convertPath(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.SAVING_PATH).getAsString() + "/movie_X"
						+ Math.round(positionX) + "_Y" + Math.round(positionY));
//		this.pathToSegMovie = AllMaarsParameters.convertPath(pathToSegDir + "/MMStack_Pos0.ome.tif");
		this.pathToSegMovie = AllMaarsParameters.convertPath(pathToSegDir + "/MMStack.ome.tif");
	}

	/**
	 * Get the parameters and use them to segment cells
	 */
	public void segmentation() {

		ReportingUtils.logMessage("Movie path for segmentation : " + pathToSegMovie);
		ImagePlus img = IJ.openImage(pathToSegMovie);

		cB = new CellsBoundaries();
		cB.setMainWindow();

		cB.getDisplayFocusImage().setState(false);
		cB.getSaveBinaryImg().setState(true);
		cB.getSaveCorrelationImg().setState(true);
		cB.getSaveDataFrame().setState(true);
		cB.getSaveFocusImage().setState(true);
		cB.getSaveRoi().setState(true);
		cB.getFilterUnususalCkb()
				.setState(parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.FILTER_SOLIDITY).getAsBoolean());
		cB.getFilterWithMeanGreyValueCkb()
				.setState(parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.FILTER_MEAN_GREY_VALUE).getAsBoolean());
		cB.setImageToAnalyze(img);
		cB.setPathDirField(img.getOriginalFileInfo().directory);
		cB.getImageToAnalyze().getCalibration().pixelDepth = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject().get(AllMaarsParameters.STEP)
				.getAsDouble();

		cB.getRunAction().checkUnitsAndScale();
		cB.getRunAction().changeScale(
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.NEW_MAX_WIDTH_FOR_CHANGE_SCALE).getAsInt(),
				parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS).getAsJsonObject()
						.get(AllMaarsParameters.NEW_MAX_HEIGTH_FOR_CHANGE_SCALE).getAsInt());

		int cellSizePixel = (int) Math
				.round(parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.CELL_SIZE).getAsDouble()
						/ parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
								.getAsJsonObject().get(AllMaarsParameters.STEP).getAsDouble());

		int minSize = (int) Math
				.round(parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.MINIMUM_CELL_AREA).getAsDouble()
						/ cB.getImageToAnalyze().getCalibration().pixelWidth);

		int maxSize = (int) Math
				.round(parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.MAXIMUM_CELL_AREA).getAsDouble()
						/ cB.getImageToAnalyze().getCalibration().pixelWidth);

		double solidity = parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.SOLIDITY).getAsDouble();

		double meanGrey = parameters.getParametersAsJsonObject().get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.MEAN_GREY_VALUE).getAsDouble();

		CellsBoundariesIdentification cBI = new CellsBoundariesIdentification(cB, cellSizePixel, minSize, maxSize, -1,
				(int) Math.round(cB.getImageToAnalyze().getNSlices() / 2), solidity, meanGrey, true, false);
		// cBI.identifyCellesBoundaries() return true, if no ROI detected.
		cBI.identifyCellsBoundaries();
		if (cBI.roiDetected()){
			this.roiDetected = true;
			cBI.getRoiManager().close();
		}
	}

	/**
	 * 
	 * @return CellsBoundaries object
	 */
	public CellsBoundaries getSegmentationObject() {
		return cB;
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
			configFile = new FileWriter(AllMaarsParameters.convertPath(pathToSegDir + "/configUsed.txt"));
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

}
