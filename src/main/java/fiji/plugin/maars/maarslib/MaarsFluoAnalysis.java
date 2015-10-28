package fiji.plugin.maars.maarslib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.micromanager.utils.ReportingUtils;

import au.com.bytecode.opencsv.CSVWriter;
import fiji.plugin.maars.cellboundaries.CellsBoundaries;
import fiji.plugin.maars.cellstateanalysis.Cell;
import fiji.plugin.maars.cellstateanalysis.SetOfCells;
import fiji.plugin.maars.cellstateanalysis.Spindle;
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
	private String pathToFluoDir;
	private double positionX;
	private double positionY;

	/**
	 * Constructor 1:
	 * 
	 * @param parameters
	 *            : parameters used for algorithm
	 * @param cB
	 *            : CellsBoundaries object (used for segmentation)
	 */
	public MaarsFluoAnalysis(AllMaarsParameters parameters, CellsBoundaries cB,
			double positionX, double positionY) {

		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
		this.pathToFluoDir = AllMaarsParameters.convertPath(parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.SAVING_PATH)
				.getAsString()
				+ "/movie_X"
				+ Math.round(this.positionX)
				+ "_Y"
				+ Math.round(this.positionY) + "_FLUO");
		File fluoDir = new File(pathToFluoDir);
		if (!fluoDir.exists()) {
			fluoDir.mkdirs();
		}

		soc = new SetOfCells(cB.getImageToAnalyze(),
				(int) Math.round(cB.getImageToAnalyze().getNSlices() / 2),
				cB.getPathDirField().getText()
						+ cB.getImageToAnalyze().getShortTitle() + "_ROI.zip",
				cB.getPathDirField().getText(), parameters
						.getParametersAsJsonObject()
						.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
						.getAsJsonObject()
						.get(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT)
						.getAsInt());

	}

	/**
	 * Constructor 2:
	 * 
	 * @param parameters
	 *            : parameters used for algorithm
	 * @param soc
	 *            : SetOfCells found by segmentation
	 */
	public MaarsFluoAnalysis(AllMaarsParameters parameters, SetOfCells soc,
			double positionX, double positionY) {

		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
		this.pathToFluoDir = AllMaarsParameters.convertPath(parameters
				.getParametersAsJsonObject()
				.get(AllMaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.SAVING_PATH)
				.getAsString()
				+ "/movie_X"
				+ Math.round(this.positionX)
				+ "_Y"
				+ Math.round(this.positionY) + "_FLUO");
		File fluoDir = new File(pathToFluoDir);
		if (!fluoDir.exists()) {
			fluoDir.mkdirs();
		}
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

//	/**
//	 * Method to check if the system should start to film mitosis (according to
//	 * criteria defined by the user)
//	 * 
//	 * @param sp
//	 *            : Spindle object identified from fluorescent image
//	 * @return true if the system should start to film
//	 */
//	public boolean checkStartConditions(Spindle sp) {
//		boolean conditions = false;
//
//		if (!sp.getFeature().equals(Spindle.NO_SPINDLE)
//				&& !sp.getFeature().equals(Spindle.NO_SPOT)) {
//			conditions = true;
//		}
//
//		if (sp.getNumberOfSpotDetected() > parameters
//				.getParametersAsJsonObject()
//				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
//				.getAsJsonObject()
//				.get(AllMaarsParameters.MAXIMUM_NUMBER_OF_SPOT).getAsInt()) {
//			conditions = false;
//		}
//
//		if (parameters.getParametersAsJsonObject()
//				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
//				.getAsJsonObject()
//				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
//				.getAsJsonObject().get(AllMaarsParameters.CONDITIONS)
//				.getAsJsonObject()
//				.get(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE)
//				.getAsBoolean()) {
//
//			conditions = conditions
//					&& sp.getLength() >= parameters
//							.getParametersAsJsonObject()
//							.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
//							.getAsJsonObject()
//							.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
//							.getAsJsonObject()
//							.get(AllMaarsParameters.VALUES)
//							.getAsJsonObject()
//							.get(AllMaarsParameters.ABSOLUTE_MINIMUM_SPINDLE_SIZE)
//							.getAsDouble();
//		}
//		if (parameters.getParametersAsJsonObject()
//				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
//				.getAsJsonObject()
//				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
//				.getAsJsonObject().get(AllMaarsParameters.CONDITIONS)
//				.getAsJsonObject()
//				.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
//				.getAsBoolean()) {
//
//			conditions = conditions
//					&& sp.getLength() <= parameters
//							.getParametersAsJsonObject()
//							.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
//							.getAsJsonObject()
//							.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
//							.getAsJsonObject()
//							.get(AllMaarsParameters.VALUES)
//							.getAsJsonObject()
//							.get(AllMaarsParameters.ABSOLUTE_MAXIMUM_SPINDLE_SIZE)
//							.getAsDouble();
//		}
//		if (parameters.getParametersAsJsonObject()
//				.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
//				.getAsJsonObject()
//				.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
//				.getAsJsonObject().get(AllMaarsParameters.CONDITIONS)
//				.getAsJsonObject()
//				.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE).getAsBoolean()) {
//
//			conditions = conditions
//					&& sp.getAngleToMajorAxis() >= parameters
//							.getParametersAsJsonObject()
//							.get(AllMaarsParameters.MITOSIS_MOVIE_PARAMETERS)
//							.getAsJsonObject()
//							.get(AllMaarsParameters.START_MOVIE_CONDITIONS)
//							.getAsJsonObject().get(AllMaarsParameters.VALUES)
//							.getAsJsonObject()
//							.get(AllMaarsParameters.RELATIVE_SPINDLE_ANGLE)
//							.getAsDouble();
//		}
//		return conditions;
//	}
//
	// /**
	// * Method to analyse an entire field and find the cell corresponding to
	// * criteria with the smallest spindle
	// *
	// * @param fieldWideImage
	// * : image of field
	// * @param pathToResults
	// * : path to save results
	// * @return cell corresponding to search (-1 if none of the cell are
	// * corresponding to criteria)
	// */
	// public Cell analyzeEntireField(ImagePlus fieldWideImage,
	// String pathToResults) {
	// int cellNumber = -1;
	// double smallerSp = 900000;
	// FileWriter spindleWriter = null;
	// ReportingUtils.logMessage("Open writer");
	// try {
	// spindleWriter = new FileWriter(pathToResults
	// + "_spindleAnalysis.txt");
	// } catch (IOException e) {
	// ReportingUtils.logMessage("Could not create " + pathToResults
	// + "_spindleAnalysis.txt");
	// e.printStackTrace();
	// }
	// try {
	// spindleWriter.write("[");
	// } catch (IOException e2) {
	// ReportingUtils.logError(e2);
	// }
	// for (int i = 0; i < soc.length(); i++) {
	// Cell cell = soc.getCell(i);
	// // TODO
	// cell.addFluoImage(fieldWideImage);
	// Spindle sp = soc.getCell(i).findFluoSpotTempFunction(
	// true,
	// parameters.getParametersAsJsonObject()
	// .get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
	// .getAsJsonObject()
	// .get(AllMaarsParameters.SPOT_RADIUS).getAsDouble());
	// try {
	// if (i != soc.length() - 1) {
	// spindleWriter.write(sp.toString(String.valueOf(cell
	// .getCellNumber())) + "\n,");
	// } else {
	// spindleWriter.write(sp.toString(String.valueOf(cell
	// .getCellNumber())) + "\n");
	// }
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// if (checkStartConditions(sp)) {
	// ReportingUtils.logMessage("Reset last spindle computed");
	// if (sp.getLength() < smallerSp) {
	// cellNumber = i;
	// smallerSp = sp.getLength();
	// }
	// }
	// cell = null;
	// }
	// try {
	// spindleWriter.write("]");
	// } catch (IOException e2) {
	// ReportingUtils.logError(e2);
	// }
	// ReportingUtils.logMessage("Close writer");
	// try {
	// spindleWriter.close();
	// } catch (IOException e) {
	// ReportingUtils.logMessage("Could not close writer");
	// e.printStackTrace();
	// }
	// if (cellNumber != -1) {
	// return soc.getCell(cellNumber);
	// } else {
	// return null;
	// }
	// }

	/**
	 * Method to analyse an entire field
	 * 
	 * @param fieldWideImage
	 *            : image of field
	 * @param pathToResultsdouble
	 *            positionX, double positionY : path to save results
	 * @param channel
	 *            : channel used for this fluoimage
	 */
	public List<String[]> analyzeEntireFieldReturnListSp(
			ImagePlus fieldWideImage, int frame, String channel) {

		List<String[]> cells = new ArrayList<String[]>();
		List<String[]> spotStrings = new ArrayList<String[]>();
		double timeInterval = parameters.getParametersAsJsonObject()
				.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(AllMaarsParameters.TIME_INTERVAL)
				.getAsDouble();
		int nbOfCells = soc.length();
		for (int i = 0; i < nbOfCells; i++) {
			Cell cell = soc.getCell(i);
			// TODO
			cell.addFluoImage(fieldWideImage);
			Spindle sp = cell.findFluoSpotTempFunction(
					true,
					parameters.getParametersAsJsonObject()
							.get(AllMaarsParameters.FLUO_ANALYSIS_PARAMETERS)
							.getAsJsonObject()
							.get(AllMaarsParameters.SPOT_RADIUS).getAsDouble());
			for (String[] s: cell.getSpotList()){
				spotStrings.add(s);
			}
			cell.addFluoSlice();
			cells.add(sp.toList(frame * timeInterval / 1000,
					Math.round(this.positionX), Math.round(this.positionY)));
			cell = null;
		}
		this.writeAnalysisRes(cells, frame, channel);
		this.writeSpotListForOneCell(spotStrings, frame, channel);
		return cells;
	}

	/**
	 * 
	 * @return Set of cells
	 */
	public SetOfCells getSetOfCells() {
		return soc;
	}

	public void saveCroppedImgs() {
		int nbCell = soc.length();
		for (int i = 0; i < nbCell; i++) {
			Cell cell = soc.getCell(i);
			cell.saveFluoImage(pathToFluoDir);
		}
	}

	public void writeAnalysisRes(List<String[]> cells, int frame, String channel) {
		FileWriter spindleWriter = null;
		CSVWriter writer = null;

		try {
			spindleWriter = new FileWriter(pathToFluoDir + "/" + frame + "_"
					+ channel + "_analysis.csv");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		writer = new CSVWriter(spindleWriter, '\t',
				CSVWriter.NO_QUOTE_CHARACTER);
		writer.writeNext(new String[] { "Cell", "Second", "Feature",
				"NbOfSpotDetected", "CellCenterX", "CellCenterY",
				"CellAbsoMajAng", "CellMajLength", "CellMinLength",
				"SpAbsoAng", "SpAngToMaj", "SpLength", "spb1X", "spb1Y",
				"spb1Z", "spb2X", "spb2Y", "spb2Z", "SpCenterX", "SpCenterY",
				"SpCenterZ", "CellCenterToSpCenterLen",
				"CellCenterToSpCenterAng", "fieldX", "fieldY" });
		writer.writeAll(cells);
		try {
			spindleWriter.close();
			writer.close();
		} catch (IOException e) {
			ReportingUtils.logError(e);
		}
	}

	public void writeSpotListForOneCell(List<String[]> spotListForOneCell, int frame, String channel) {
		FileWriter spotListWriter = null;
		CSVWriter writer = null;
		try {
			spotListWriter = new FileWriter(pathToFluoDir + "/" + frame + "_"
					+ channel + "_spotList.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer = new CSVWriter(spotListWriter, '\t',
				CSVWriter.NO_QUOTE_CHARACTER);
		writer.writeNext(new String[] { "VISIBILITY", "POSITION_T",
				"POSITION_Z", "POSITION_Y", "RADIUS", "FRAME", "POSITION_X" });

		writer.writeAll(spotListForOneCell);
		try {
			spotListWriter.close();
			writer.close();
		} catch (IOException e) {
			ReportingUtils.logError(e);
		}
	}
}
