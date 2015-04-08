package fiji.plugin.maars.maarslib;

import java.io.FileWriter;
import java.io.IOException;

import org.micromanager.utils.ReportingUtils;

import fiji.plugin.maars.cellboundaries.CellsBoundaries;
import fiji.plugin.maars.cellstateanalysis.Cell;
import fiji.plugin.maars.cellstateanalysis.SetOfCells;
import fiji.plugin.maars.cellstateanalysis.Spindle;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ZProjector;

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
	public Spindle getSpindle(ImagePlus image, Cell cell) {

		cell.addFluoImage(image);
		return cell.findFluoSpotTempFunction(
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
	 * @return cell corresponding to search (-1 if none of the cell are
	 *         corresponding to criteria)
	 */
	public Cell analyzeEntireField(ImagePlus fieldWideImage,
			String pathToResults) {
		int cellNumber = -1;
		double smallerSp = 900000;
		FileWriter spindleWriter = null;
		ReportingUtils.logMessage("Open writer");
		try {
			spindleWriter = new FileWriter(pathToResults
					+ "_spindleAnalysis.txt");
		} catch (IOException e) {
			ReportingUtils.logMessage("Could not create " + pathToResults
					+ "_spindleAnalysis.txt");
			e.printStackTrace();
		}
		try {
			spindleWriter.write("[");
		} catch (IOException e2) {
			ReportingUtils.logError(e2);
		}
		for (int i = 0; i < soc.length(); i++) {
			//TODO
			soc.getCell(i).addFluoImage(fieldWideImage);
			Spindle sp = soc.getCell(i).findFluoSpotTempFunction(
					true,
					parameters.getParametersAsJsonObject()
							.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
							.getAsJsonObject()
							.get(AllMaarsParameters.SPOT_RADIUS).getAsDouble());
//			ReportingUtils.logMessage("Writing spindle coordinates");
			try {
				if (i != soc.length() - 1) {
					spindleWriter.write(sp.toString(String.valueOf(soc.getCell(i).getCellNumber()))
							+ "\n,");
				} else {
					spindleWriter.write(sp.toString(String.valueOf(soc.getCell(i).getCellNumber()))
							+ "\n");
				}

			} catch (IOException e) {
				ReportingUtils.logMessage("could not write sp of cell "
						+ soc.getCell(i).getCellShapeRoi().getName());
				e.printStackTrace();
			}
			if (checkStartConditions(sp)) {
//				ReportingUtils.logMessage("Reset last spindle computed");
				if (sp.getLength() < smallerSp) {
					cellNumber = i;
					smallerSp = sp.getLength();
				}
			}
		}
		try {
			spindleWriter.write("]");
		} catch (IOException e2) {
			ReportingUtils.logError(e2);
		}
		ReportingUtils.logMessage("Close writer");
		try {
			spindleWriter.close();
		} catch (IOException e) {
			ReportingUtils.logMessage("Could not close writer");
			e.printStackTrace();
		}
		if (cellNumber != -1) {
			return soc.getCell(cellNumber);
		}else{
			return null;
		}
	}

	/**
	 * Method to analyse an entire field 
	 * 
	 * @param fieldWideImage
	 *            : image of field
	 * @param pathToResults
	 *            : path to save results
	 */
	public void analyzeEntireFieldNoReturn(ImagePlus fieldWideImage,
			String pathToResults) {
		FileWriter spindleWriter = null;
		try {
			spindleWriter = new FileWriter(pathToResults
					+ "_spindleAnalysis.txt");
		} catch (IOException e) {
			ReportingUtils.logMessage("Could not create " + pathToResults
					+ "_spindleAnalysis.txt");
			e.printStackTrace();
		}
		try {
			spindleWriter.write("[");
		} catch (IOException e2) {
			ReportingUtils.logError(e2);
		}
		for (int i = 0; i < soc.length(); i++) {
			Spindle sp = null;
			//TODO
			soc.getCell(i).addFluoImage(fieldWideImage);
			 sp = soc.getCell(i).findFluoSpotTempFunction(
					true,
					parameters.getParametersAsJsonObject()
							.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
							.getAsJsonObject()
							.get(AllMaarsParameters.SPOT_RADIUS).getAsDouble());
			try {
				if (i != soc.length() - 1) {
					spindleWriter.write(sp.toString(String.valueOf(soc.getCell(i).getCellNumber()))
							+ "\n,");
				} else {
					spindleWriter.write(sp.toString(String.valueOf(soc.getCell(i).getCellNumber()))
							+ "\n");
				}

			} catch (IOException e) {
				ReportingUtils.logMessage("could not write sp of cell "
						+ soc.getCell(i).getCellShapeRoi().getName());
				e.printStackTrace();
			}
		}
		try {
			spindleWriter.write("]");
		} catch (IOException e2) {
			ReportingUtils.logError(e2);
		}
		try {
			spindleWriter.close();
		} catch (IOException e) {
			ReportingUtils.logMessage("Could not close writer");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return Set of cells
	 */
	public SetOfCells getSetOfCells() {
		return soc;
	}

}
