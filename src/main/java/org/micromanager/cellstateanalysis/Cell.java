package org.micromanager.cellstateanalysis;

import org.micromanager.utils.ImgUtils;

import ij.gui.Roi;

/**
 * Cell is a class containing information about cell image, including its
 * mitotic state, its shape, ... TODO
 * 
 * @author Tong LI
 *
 */
public class Cell {

	private int cellNumber;
	private Roi cellShapeRoi;

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
}
