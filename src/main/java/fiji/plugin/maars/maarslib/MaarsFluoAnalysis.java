package fiji.plugin.maars.maarslib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.micromanager.internal.utils.ReportingUtils;

import au.com.bytecode.opencsv.CSVWriter;
import fiji.plugin.maars.cellstateanalysis.Cell;
import fiji.plugin.maars.cellstateanalysis.SetOfCells;
import fiji.plugin.maars.cellstateanalysis.Spindle;
import fiji.plugin.maars.segmentPombe.SegPombeParameters;
import fiji.plugin.maars.utils.FileUtils;
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

	/**
	 * Constructor 1:
	 * 
	 * @param parameters
	 *            : parameters used for algorithm
	 * @param cB
	 *            : CellsBoundaries object (used for segmentation)
	 */
	public MaarsFluoAnalysis(MaarsParameters parameters,
			SegPombeParameters segParam, double positionX, double positionY) {

		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
		this.pathToFluoDir = FileUtils.convertPath(parameters
				.getParametersAsJsonObject()
				.get(MaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject().get(MaarsParameters.SAVING_PATH)
				.getAsString()
				+ "/movie_X"
				+ Math.round(this.positionX)
				+ "_Y"
				+ Math.round(this.positionY) + "_FLUO");
		File fluoDir = new File(pathToFluoDir);
		if (!fluoDir.exists()) {
			fluoDir.mkdirs();
		}

		soc = new SetOfCells(
				segParam.getImageToAnalyze(),
				(int) Math.round(segParam.getImageToAnalyze().getNSlices() / 2),
				segParam.getSavingPath()
						+ segParam.getImageToAnalyze().getShortTitle()
						+ "_ROI.zip", segParam.getSavingPath(), parameters
						.getParametersAsJsonObject()
						.get(MaarsParameters.FLUO_ANALYSIS_PARAMETERS)
						.getAsJsonObject()
						.get(MaarsParameters.MAXIMUM_NUMBER_OF_SPOT)
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
	public MaarsFluoAnalysis(MaarsParameters parameters, SetOfCells soc,
			double positionX, double positionY) {

		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
		this.pathToFluoDir = FileUtils.convertPath(parameters
				.getParametersAsJsonObject()
				.get(MaarsParameters.GENERAL_ACQUISITION_PARAMETERS)
				.getAsJsonObject().get(MaarsParameters.SAVING_PATH)
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
	 * Method to analyse an entire field
	 * 
	 * @param pathToResultsdouble
	 *            positionX, double positionY : path to save results
	 * @param channel
	 *            : channel used for this fluoimage
	 */
	public List<String[]> analyzeEntireFieldReturnListSp(int frame,
			String channel) {
		List<String[]> cells = new ArrayList<String[]>();
		List<String[]> spotStrings = new ArrayList<String[]>();
		double timeInterval = parameters.getParametersAsJsonObject()
				.get(MaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(MaarsParameters.TIME_INTERVAL)
				.getAsDouble();
		Iterator<Cell> itrCells = soc.iterator();
		ReportingUtils.logMessage("Detecting spots...");
		while (itrCells.hasNext()) {
			Cell cell = itrCells.next();
			Spindle sp = cell.findFluoSpotTempFunction(parameters
					.getParametersAsJsonObject()
					.get(MaarsParameters.FLUO_ANALYSIS_PARAMETERS)
					.getAsJsonObject().get(MaarsParameters.SPOT_RADIUS)
					.getAsDouble());
			for (String[] s : cell.getSpotList()) {
				spotStrings.add(s);
			}
			cell.addCroppedFluoSlice();
			cells.add(sp.toList(frame * timeInterval / 1000,
					Math.round(this.positionX), Math.round(this.positionY)));
			cell = null;
		}
		soc.resetCount();
		ReportingUtils.logMessage("Spots detection done...");
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

	/**
	 * set fluo image
	 */
	public void setFluoImage(ImagePlus fluoImg) {
		this.fluoImg = fluoImg;
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
		Iterator<Cell> itrCells = soc.iterator();
		ReportingUtils.logMessage("Cropping cell");
		while (itrCells.hasNext()) {
			Cell cell = itrCells.next();
			cell.setFluoImage(this.fluoImg);
			cell.setBfFluocalibFactor();
			cell.setRescaledFluoRoi();
			cell.cropFluoImage();
		}
		soc.resetCount();
	}

	public void saveCroppedImgs() {
		for (Cell cell : soc) {
			cell.saveCroppedImage(pathToFluoDir);
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

	public void writeSpotListForOneCell(List<String[]> spotListForOneCell,
			int frame, String channel) {
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
				"POSITION_Z", "POSITION_Y", "RADIUS", "FRAME", "POSITION_X",
				"cellNumber" });

		writer.writeAll(spotListForOneCell);
		try {
			spotListWriter.close();
			writer.close();
		} catch (IOException e) {
			ReportingUtils.logError(e);
		}
	}
}
