package org.micromanager.cellstateanalysis;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.micromanager.utils.ImgUtils;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import ij.IJ;
import ij.gui.Roi;

/**
 *
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
	private SpotsContainer spotContainer;
	private GeometryContainer geoContainer;
	private AtomicInteger merotelyCounter = new AtomicInteger(0);;

	/**
	 * @param roiCellShape
	 *            : ROI that correspond to segmented cell
	 * @param cellNb
	 *            ：cell instance index in array
	 */
	public Cell(Roi roiCellShape, int cellNb) {
		this.cellShapeRoi = roiCellShape;
		this.cellNumber = cellNb;
		this.spotContainer = new SpotsContainer();
		this.geoContainer = new GeometryContainer();
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

	public void addChannel(String channel) {
		spotContainer.addChannel(channel);
		geoContainer.addChannel(channel);
	}

	public void putSpot(String channel, int frame, Spot s) {
		spotContainer.putSpot(channel, frame, s);
	}

	public int getNbOfSpots(String channel, int frame) {
		return spotContainer.getNbOfSpot(channel, frame);
	}

	public Iterable<Spot> getSpotsInFrame(String channel, int frame) {
		return spotContainer.getSpotsInFrame(channel, frame);
	}

	public void removeSpot(String channel, int frame, Spot s) {
		spotContainer.removeSpot(channel, frame, s);
	}

	public void setTrackmateModel(Model model) {
		spotContainer.setTrackmateModel(model);
	}

	public SpotsContainer getSpotContainer() {
		return this.spotContainer;
	}

	public void putGeometry(String channel, int frame, HashMap<String, Object> geometries) {
		geoContainer.putGeometry(channel, frame, geometries);
	}

	public GeometryContainer getGeometryContainer() {
		return this.geoContainer;
	}

	public void incrementMerotelyCount() {
		IJ.log("cell " + this.cellNumber + " merotely counter increased to " + merotelyCounter.incrementAndGet());
	}

	public int getMerotelyCount() {
		return merotelyCounter.get();
	}
}
