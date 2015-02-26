package fiji.plugin.maars.maarslib;

import java.io.FileWriter;
import java.io.IOException;

import org.micromanager.utils.ReportingUtils;

import fiji.plugin.maars.cellboundaries.CellsBoundaries;
import fiji.plugin.maars.cellstateanalysis.SetOfCells;
import fiji.plugin.maars.cellstateanalysis.Spindle;
import ij.IJ;
import ij.ImagePlus;

/**
 * Class to find and measure mitotic spindle using fluorescence image analysis
 * 
 * @author marie
 *
 */
public class MaarsFluoAnalysis {

	private AllMaarsParameters parameters;
	private SetOfCells soc;

	/**
	 * Constructor 1:
	 * 
	 * @param parameters
	 *            : parameters used for algorithm
	 * @param cB
	 *            : CellsBoundaries object (used for segmentation)
	 */
	public MaarsFluoAnalysis(AllMaarsParameters parameters, CellsBoundaries cB) {

		this.parameters = parameters;

		ImagePlus corrImg = IJ.openImage(cB.getPathDirField().getText()
				+ cB.getImageToAnalyze().getShortTitle()
				+ "_CorrelationImage.tif");

		soc = new SetOfCells(cB.getImageToAnalyze(), corrImg,
				(int) Math.round(cB.getImageToAnalyze().getNSlices() / 2), -1,
				cB.getPathDirField().getText()
						+ cB.getImageToAnalyze().getShortTitle() + "_ROI.zip",
				cB.getPathDirField().getText());

	}

	/**
	 * Constructor 2:
	 * 
	 * @param parameters
	 *            : parameters used for algorithm
	 * @param soc
	 *            : SetOfCells found by segmentation
	 */
	public MaarsFluoAnalysis(AllMaarsParameters parameters, SetOfCells soc) {

		this.parameters = parameters;

		this.soc = soc;
	}

	/**
	 * Find spindle in image for cell specified by an index
	 * 
	 * @param image
	 *            : fluorescent image where spindle pole bodies are tagged
	 * @param cellNumber
	 *            : cell index
	 * @return Spindle object
	 */
	public Spindle getSpindle(ImagePlus image, int cellNumber) {

		soc.getCell(cellNumber).addFluoImage(image);
		return soc.getCell(cellNumber).findFluoSpotTempFunction(
				false,
				parameters.getParametersAsJsonObject()
						.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
						.getAsJsonObject().get(AllMaarsParameters.SPOT_RADIUS)
						.getAsDouble());
	}

	/**
	 * Method to check if the system should start to film mitosis (according to
	 * criteria defined by the user)
	 * 
	 * @param sp
	 *            : Spindle object identified from fluorescent image
	 * @return true if the system should start to film
	 */
	public boolean checkStartConditions(Spindle sp) {
		boolean conditions = false;

		if (!sp.getFeature().equals(Spindle.NO_SPINDLE)
				&& !sp.getFeature().equals(Spindle.NO_SPOT)) {
			conditions = true;
		}

		if (sp.getNumberOfSpotDetected() > parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT).getAsInt()) {
			conditions = false;
		}

		if (parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject().get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE)
				.getAsBoolean()) {

			conditions = conditions
					&& sp.getLength() >= parameters
							.getParametersAsJsonObject()
							.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
							.getAsJsonObject()
							.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
							.getAsJsonObject()
							.get(AllMaarsParameters.VALUES)
							.getAsJsonObject()
							.get(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE)
							.getAsDouble();
		}
		if (parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject().get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
				.getAsBoolean()) {

			conditions = conditions
					&& sp.getLength() <= parameters
							.getParametersAsJsonObject()
							.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
							.getAsJsonObject()
							.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
							.getAsJsonObject()
							.get(AllMaarsParameters.VALUES)
							.getAsJsonObject()
							.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
							.getAsDouble();
		}
		if (parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
				.getAsJsonObject()
				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
				.getAsJsonObject().get(AllMaarsParameters.CONDITIONS)
				.getAsJsonObject()
				.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE).getAsBoolean()) {

			conditions = conditions
					&& sp.getAngleToMajorAxis() >= parameters
							.getParametersAsJsonObject()
							.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
							.getAsJsonObject()
							.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
							.getAsJsonObject().get(AllMaarsParameters.VALUES)
							.getAsJsonObject()
							.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE)
							.getAsDouble();
		}
		return conditions;
	}

	/**
	 * Method to analyse an entire field and find the cell corresponding to
	 * criteria with the smallest spindle
	 * 
	 * @param fieldWideImage
	 *            : image of field
	 * @param pathToResults
	 *            : path to save results
	 * @return index of cell corresponding to search (-1 if none of the cell are
	 *         corresponding to criteria)
	 */
	public int analyzeEntireField(ImagePlus fieldWideImage, String pathToResults) {
		int cellNumber = -1;
		double smallerSp = 900000;
		FileWriter writer = null;
		try {
			writer = new FileWriter(pathToResults + "spindleAnalysis.txt");
		} catch (IOException e) {
			ReportingUtils.logMessage("Could not create " + pathToResults
					+ "spindleAnalysis.csv");
			e.printStackTrace();
		}
		for (int i = 0; i < soc.length(); i++) {
			soc.getCell(i).addFluoImage(fieldWideImage);
			Spindle sp = soc.getCell(i).findFluoSpotTempFunction(
					true,
					parameters.getParametersAsJsonObject()
							.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
							.getAsJsonObject()
							.get(AllMaarsParameters.SPOT_RADIUS).getAsDouble());

			try {
				writer.write(sp.toString(soc.getCell(i).getCellShapeRoi()
						.getName())
						+ "\n");
			} catch (IOException e) {
				ReportingUtils.logMessage("could not write sp of cell "
						+ soc.getCell(i).getCellShapeRoi().getName());
				e.printStackTrace();
			}

			if (checkStartConditions(sp)) {
				if (sp.getLength() < smallerSp) {
					cellNumber = i;
					smallerSp = sp.getLength();
				}
			}
		}

		try {
			writer.close();
		} catch (IOException e) {
			ReportingUtils.logMessage("Could not close writer");
			e.printStackTrace();
		}
		return cellNumber;
	}

	/**
	 * 
	 * @return Set of cells
	 */
	public SetOfCells getSetOfCells() {
		return soc;
	}

}
