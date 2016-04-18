package org.micromanager.cellstateanalysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ij.IJ;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;

/**
 * Main object of MAARS, you got information about each cell (ROI measurement,
 * analysis related result)
 * 
 * @author Tong LI
 *
 */
public class SetOfCells implements Iterable<Cell>, Iterator<Cell> {
	private int iteratorCount = 0;
	private ArrayList<Cell> cellArray;

	public SetOfCells() {
	}

	/**
	 * @param parameters
	 *            : parameters that used in
	 */
	public void loadCells(String pathToSegDir) {
		IJ.log("Loading Cells");
		Roi[] roiArray = getRoisAsArray(pathToSegDir + "/ROI.zip");
		cellArray = new ArrayList<Cell>();
		for (int i = 1; i <= roiArray.length; i++) {
			cellArray.add(i-1, new Cell(roiArray[i-1], i));
		}
		IJ.log("Done.");
	}

	/**
	 * Method to open ROI file and get them as ROI array
	 * 
	 * @return
	 */
	public Roi[] getRoisAsArray(String pathToRois) {
		RoiManager roiManager = RoiManager.getInstance();
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
		return this.cellArray.get(index);
	}
	
	/**
	 * 
	 * @param index
	 * @param cell
	 */
	public void updateCell(int index, Cell cell){
		IJ.log("Reseted");
		this.cellArray.set(index, cell);
	}

	/**
	 * total number of cell
	 * 
	 * @return
	 */
	public int size() {
		return cellArray.size();
	}

	/**
	 * 
	 * @param rt
	 */
	public void setRoiMeasurementIntoCells(ResultsTable rt) {
		for (Cell c : cellArray) {
			c.setRoiMeasurement(rt.getRowAsString(c.getCellNumber()-1));
		}
	}

	public void reset() {
		this.iteratorCount = 0;
		this.cellArray = null;
	}

	// iterator related
	@Override
	public Iterator<Cell> iterator() {
		resetCount();
		return this;
	}

	@Override
	public boolean hasNext() {
		return iteratorCount < cellArray.size();
	}

	@Override
	public Cell next() {
		if (iteratorCount >= cellArray.size())
			throw new NoSuchElementException();
		iteratorCount++;
		return cellArray.get(iteratorCount - 1);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void resetCount() {
		this.iteratorCount = 0;
	}
}
