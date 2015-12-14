package org.micromanager.cellstateanalysis;

import org.micromanager.utils.ImgUtils;

import ij.gui.Roi;

/**
 * Cell is a class containing information about cell ROI, including its index in
 * cell array, and its its corresponding measurements from Analyzer of ImageJ
 * 
 * @author Tong LI
 *
 */
public class Cell {

	private int cellNumber;
	private Roi cellShapeRoi;
	public static final int AREA = 1, MEAN = 2, STD_DEV = 3, MIN = 4, MAX = 5, X_CENTROID = 6, Y_CENTROID = 7,
			PERIMETER = 8, MAJOR = 9, MINOR = 10, ANGLE = 11, CIRCULARITY = 12, ASPECT_RATIO = 13, ROUNDNESS = 14,
			SOLIDITY = 15;
	private String[] measurements;

	/**
	 * @param roiCellShape
	 *            : ROI that correspond to segmented cell
	 * @param cellNb
	 *            ：cell instance index in array
	 */
	public Cell(Roi roiCellShape, int cellNb) {
		this.cellShapeRoi = roiCellShape;
		this.cellNumber = cellNb;
	}

	/**
	 * 
	 * @return ROI corresponding to segmented cell
	 */
	public Roi getCellShapeRoi() {
		return cellShapeRoi;
	}

	public Roi rescaleCellShapeRoi(double[] factors) {
		return ImgUtils.rescaleRoi(cellShapeRoi, factors);
	}

	public int getCellNumber() {
		return cellNumber;
	}

	public void setRoiMeasurement(String measurements) {
		this.measurements = measurements.split("\t", -1);
	}

	public double get(int headerIndex) {
		return Double.parseDouble(measurements[headerIndex]);
	}
}
