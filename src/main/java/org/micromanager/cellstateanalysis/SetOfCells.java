package org.micromanager.cellstateanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.maars.MaarsParameters;
import org.micromanager.segmentPombe.SegPombeParameters;

import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;

/**
 * Class to manipulate a set of cells which corresponds to cells of one field
 * 
 * @author Tong LI && marie
 *
 */
public class SetOfCells implements Iterable<Cell>, Iterator<Cell> {
	
	public static final String CELL_NUMBER = "Cell_Number";
	private RoiManager roiManager;
	private Roi[] roiArray;
	private int count = 0;
	private ResultsTable rt;
	private ArrayList<Cell> cellArray;
	private String rootSavingPath;
	private ConcurrentHashMap<String, Object> acquisitionMeta;

	/**
	 * Constructor
	 * 
	 * @param savingPath
	 *            :root directory of acquisitions
	 */
	public SetOfCells(String savingPath) {
		this.rootSavingPath = savingPath;
	}

	/**
	 * @param acquisitionMeta
	 */
	public void setAcquisitionMeta(ConcurrentHashMap<String, Object> acquisitionMeta) {
		this.acquisitionMeta = acquisitionMeta;
	}

	/**
	 * @param parameters
	 *            : parameters that used in
	 */
	public void loadCells(SegPombeParameters parameters) {
		ReportingUtils.logMessage("Loading Cells");
		roiArray = getRoisAsArray(rootSavingPath + "/movie_X" + acquisitionMeta.get(MaarsParameters.X_POS) + "_Y"
				+ acquisitionMeta.get(MaarsParameters.Y_POS) + "/" + parameters.getImageToAnalyze().getShortTitle()
				+ "_ROI.zip");
		cellArray = new ArrayList<Cell>();
		for (int i = 0; i < roiArray.length; i++) {
			cellArray.add(i, new Cell(roiArray[i], i + 1));
		}
		ReportingUtils.logMessage("Done.");
	}

	/**
	 * Method to open ROI file and get them as ROI array
	 * 
	 * @return
	 */
	public Roi[] getRoisAsArray(String pathToRois) {

		roiManager = RoiManager.getInstance();
		if (roiManager == null) {
			roiManager = new RoiManager();
		}
		if (roiManager.getCount() == 0) {
			roiManager.runCommand("Open", pathToRois);
		}
		return roiManager.getRoisAsArray();
	}

	/**
	 * Method to get Cell corresponding to index
	 * 
	 * @param index
	 * @return Cell corresponding to index
	 */
	public Cell getCell(int index) {
		return cellArray.get(index);
	}

	/**
	 * total number of cell
	 * 
	 * @return
	 */
	public int size() {
		return cellArray.size();
	}

	public void setRoiMeasurement(ResultsTable rt) {
		this.rt = rt;
	}

	public ResultsTable getRoiMeasurement() {
		return this.rt;
	}

	public void writeResults() {
		String fluoDir = rootSavingPath + "/movie_X" + acquisitionMeta.get(MaarsParameters.X_POS) + "_Y"
				+ acquisitionMeta.get(MaarsParameters.Y_POS) + "_FLUO";
		String croppedImgDir = fluoDir + "/croppedImgs/";
		String spotsXmlDir = fluoDir + "/spots/";
		if (!new File(croppedImgDir).exists()) {
			new File(croppedImgDir).mkdirs();
		}
		if (!new File(spotsXmlDir).exists()) {
			new File(spotsXmlDir).mkdirs();
		}
		for (Cell cell : this) {
			// save cropped cells
			cell.saveCroppedImage(croppedImgDir);
			// can be optional
			cell.writeSpotFeatures(spotsXmlDir);
		}
	}

	// iterator related
	@Override
	public Iterator<Cell> iterator() {
		resetCount();
		return this;
	}

	@Override
	public boolean hasNext() {
		return count < cellArray.size();
	}

	@Override
	public Cell next() {
		if (count >= cellArray.size())
			throw new NoSuchElementException();
		count++;
		return cellArray.get(count - 1);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}

	public void resetCount() {
		this.count = 0;
	}

}
