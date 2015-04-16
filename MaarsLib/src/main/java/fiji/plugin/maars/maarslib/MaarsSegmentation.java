package fiji.plugin.maars.maarslib;

import java.io.File;

import org.micromanager.utils.ReportingUtils;

import fiji.plugin.maars.cellboundaries.CellsBoundaries;
import fiji.plugin.maars.cellboundaries.CellsBoundariesIdentification;
import ij.IJ;

/**
 * Class to segment an specific type of image and find and record cell shape and
 * location
 * 
 * @author marie
 *
 */
public class MaarsSegmentation {
	private AllMaarsParameters parameters;
	private String moviePath;
	private CellsBoundaries cB;
	private boolean noRoiDetected = false;

	/**
	 * Constructor :
	 * 
	 * @param parameters
	 *            : parameters of segmentation
	 * @param moviePath
	 *            : where is stored image to segment
	 */
	public MaarsSegmentation(AllMaarsParameters parameters, String moviePath) {

		this.parameters = parameters;
		this.moviePath = AllMaarsParameters.convertPath(moviePath);
		ReportingUtils.logMessage("Movie path for segmentation : " + moviePath);

	}

	/**
	 * Get the parameters and use them to segment image
	 */
	public void segmentation() {

		IJ.open(moviePath);
		cB = new CellsBoundaries();
		cB.setMainWindow();

		cB.getDisplayFocusImage().setState(false);
		cB.getSaveBinaryImg().setState(true);
		cB.getSaveCorrelationImg().setState(true);
		cB.getSaveDataFrame().setState(true);
		cB.getSaveFocusImage().setState(true);
		cB.getSaveRoi().setState(true);
		cB.getFilterUnususalCkb().setState(true);
		cB.getFilterWithMeanGreyValueCkb().setState(true);
		cB.getAlreadryOpenedImage();
		cB.getImageToAnalyze().getCalibration().pixelDepth = parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.STEP).getAsDouble();

		cB.getRunAction().checkUnitsAndScale();
		cB.getRunAction()
				.changeScale(
						parameters
								.getParametersAsJsonObject()
								.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
								.getAsJsonObject()
								.get(AllMaarsParameters.NEW_MAX_WIDTH_FOR_CHANGE_SCALE)
								.getAsInt(),
						parameters
								.getParametersAsJsonObject()
								.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
								.getAsJsonObject()
								.get(AllMaarsParameters.NEW_MAX_HEIGTH_FOR_CHANGE_SCALE)
								.getAsInt());

		int cellSizePixel = (int) Math.round(parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.CELL_SIZE)
				.getAsDouble()
				/ parameters.getParametersAsJsonObject()
						.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.STEP)
						.getAsDouble());

		int minSize = (int) Math.round(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.MINIMUM_CELL_AREA)
				.getAsDouble()
				/ cB.getImageToAnalyze().getCalibration().pixelWidth);

		int maxSize = (int) Math.round(parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.MAXIMUM_CELL_AREA)
				.getAsDouble()
				/ cB.getImageToAnalyze().getCalibration().pixelWidth);

		double solidity = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.SOLIDITY)
				.getAsDouble();

		double meanGrey = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.SEGMENTATION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.MEAN_GREY_VALUE)
				.getAsDouble();

		CellsBoundariesIdentification cBI = new CellsBoundariesIdentification(
				cB, cellSizePixel, minSize, maxSize, -1, (int) Math.round(cB
						.getImageToAnalyze().getNSlices() / 2), solidity,
				meanGrey, true, false);
		boolean noRoiDec = cBI.identifyCellesBoundaries();
		ReportingUtils.logMessage("lala " + noRoiDec);
		if(noRoiDec){
			this.noRoiDetected = true;
		}
		IJ.getImage().close();
	}

	/**
	 * 
	 * @return true if program is still working on segmentation
	 */
	public boolean isAnalysing() {

		File file = new File(cB.getPathDirField().getText()
				+ cB.getImageToAnalyze().getShortTitle()
				+ "_CorrelationImage.tif");
		if (file.exists()) {
			return false;
		} else {
			return true;
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
	public boolean noRoiDetected() {
		return this.noRoiDetected;
	}
}
