package org.micromanager.cellstateanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.segmentPombe.SegPombeParameters;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

/**
 * Class to manipulate a set of cells which corresponds to cells of one field
 * 
 * @author Tong LI && marie
 *
 */
public class SetOfCells implements Iterable<Cell>, Iterator<Cell> {
	// tools to get results
	private RoiManager roiManager;
	private Roi[] roiArray;
	private int count = 0;

	private ArrayList<Cell> cellArray;
	private String rootSavingPath;

	public SetOfCells(String savingPath) {
		this.rootSavingPath = savingPath;
	}

	/**
	 * 
	 * @param parameters
	 *            : parameters that used in
	 */
	public void loadCells(SegPombeParameters parameters) {
		ReportingUtils.logMessage("Get ROIs as array");
		roiArray = getRoisAsArray(rootSavingPath + parameters.getImageToAnalyze().getShortTitle() + "_ROI.zip");
		cellArray = new ArrayList<Cell>();
		ReportingUtils.logMessage("Initialize Cells in array");
		for (int i = 0; i < roiArray.length; i++) {
			cellArray.add(i, new Cell(roiArray[i], i + 1));
		}
		ReportingUtils.logMessage("Done.");
	}

	public ArrayList<Cell> getSubArray(int begin, int end) {
		ArrayList<Cell> subSet = new ArrayList<Cell>();
		for (int i = begin; i < end; i++) {
			subSet.add((Cell) cellArray.get(i));
		}
		return subSet;
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

	public RoiManager getROIManager() {
		return this.roiManager;
	}

	public void saveResults() {
		String pathToCroppedImgDir = rootSavingPath + "/croppedImgs/";
		if (!new File(pathToCroppedImgDir).exists()) {
			new File(pathToCroppedImgDir).mkdirs();
		}
		for (Cell cell : this) {
			cell.saveCroppedImage(pathToCroppedImgDir);
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
