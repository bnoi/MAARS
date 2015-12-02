package org.micromanager.cellstateanalysis;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;

/**
 * Class containing measures realised on an image containing a ROI: AREA =
 * object area STD_DEV = object standard-deviation MIN = object minimum pixel
 * value MAX = object maximum pixel value X_CENTROID = object centroid
 * coordinates : x Y_CENTROID = object centroid coordinates : y PERIMETER =
 * object perimeter MAJOR = object major axis length MINOR = object minor axis
 * length ANGLE = object major axis angle CIRCULARITY = object circularity
 * ASPECT_RATIO = object aspect ration (see imageJ documentation) ROUND = object
 * round (see imageJ documentation) SOLIDITY = object solidity (see imageJ
 * documentation)
 * 
 * @author marie
 *
 */
public class Measures {

	private Analyzer bfAnalyzer;

	private double[] measures;
	public static final int AREA = 0;
	public static final int STD_DEV = 1;
	public static final int MIN = 2;
	public static final int MAX = 3;
	public static final int X_CENTROID = 4;
	public static final int Y_CENTROID = 5;
	public static final int PERIMETER = 6;
	public static final int MAJOR = 7;
	public static final int MINOR = 8;
	public static final int ANGLE = 9;
	public static final int CIRCULARITY = 10;
	public static final int ASPECT_RATIO = 11;
	public static final int ROUND = 12;
	public static final int SOLIDITY = 13;

	/**
	 * Constructor :
	 * 
	 * @param focusImg
	 *            : image the measures are taken from (with Roi on it)
	 * @param rt
	 *            : result table (containing results of analysis)
	 */
	public Measures(ImagePlus focusImg) {
		ResultsTable rt = new ResultsTable();
		bfAnalyzer = new Analyzer(focusImg,
				Measurements.AREA + Measurements.STD_DEV + Measurements.MIN_MAX + Measurements.SHAPE_DESCRIPTORS
						+ Measurements.CENTROID + Measurements.PERIMETER + Measurements.ELLIPSE,
				rt);

		bfAnalyzer.measure();

		measures = new double[14];
		measures[AREA] = rt.getValue("Area", 0);
		measures[STD_DEV] = rt.getValue("StdDev", 0);
		measures[MIN] = rt.getValue("Min", 0);
		measures[MAX] = rt.getValue("Max", 0);
		measures[X_CENTROID] = rt.getValue("X", 0);
		measures[Y_CENTROID] = rt.getValue("Y", 0);
		measures[PERIMETER] = rt.getValue("Perim.", 0);
		measures[MAJOR] = rt.getValue("Major", 0);
		measures[MINOR] = rt.getValue("Minor", 0);
		measures[ANGLE] = rt.getValue("Angle", 0);
		measures[CIRCULARITY] = rt.getValue("Circ.", 0);
		measures[ASPECT_RATIO] = rt.getValue("AR", 0);
		measures[ROUND] = rt.getValue("Round", 0);
		measures[SOLIDITY] = rt.getValue("Solidity", 0);
	}

	public double getArea() {
		return measures[AREA];
	}

	public double getStdDev() {
		return measures[STD_DEV];
	}

	public double getMin() {
		return measures[MIN];
	}

	public double getMax() {
		return measures[MAX];
	}

	public double getXCentroid() {
		return measures[X_CENTROID];
	}

	public double getYCentroid() {
		return measures[Y_CENTROID];
	}

	public double getPerimeter() {
		return measures[PERIMETER];
	}

	public double getMajor() {
		return measures[MAJOR];
	}

	public double getMinor() {
		return measures[MINOR];
	}

	public double getAngle() {
		return measures[ANGLE];
	}

	public double getCircularity() {
		return measures[CIRCULARITY];
	}

	public double getAspectRatio() {
		return measures[ASPECT_RATIO];
	}

	public double getRound() {
		return measures[ROUND];
	}

	public double getSolidity() {
		return measures[SOLIDITY];
	}

	public void setXCentroid(double centroX) {
		measures[X_CENTROID] = centroX;
	}

	public void setYCentroid(double centroY) {
		measures[Y_CENTROID] = centroY;
	}
}
