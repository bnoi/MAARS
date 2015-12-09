package org.micromanager.cellstateanalysis;

import java.util.HashMap;

import com.google.common.collect.Iterables;

import fiji.plugin.trackmate.Spot;
import ij.measure.ResultsTable;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */

public class FeatureCollection {

	private HashMap<Integer, HashMap<String, Object>> collection;

	public FeatureCollection() {
		this.collection = new HashMap<Integer, HashMap<String, Object>>();
	}

	public void putFeatures(int frame, HashMap<String, Object> features) {
		this.collection.put(frame, features);
	}
}
//
// // feature related to spindle
// private double angleSpCellCenter;
// private double lengthSpCellCenter;
// private double centerSpX;
// private double centerSpY;
// private double centerSpZ;
// private double length;
// private double lengthToMajorAxis;
// private double angleToMajorAxis;
// private double spAbsoAng;
//
// // Cell roi level parameters
// private Measures measures;
// private Roi cellShapeRoi;
// private Roi roiBeforeCrop;
// private double centerCellX;
// private double centerCellY;
//
// //
// public static final String NO_SPOT = "NO_SPOT";
//
// /**
// * Constructor :
// *
// * @param spotList
// * : list of Spot objects corresponding to spot detected by
// * SpotDetector (in CellFluoAnalysis)
// * @param measures
// * : Measure object corresponding to cell
// * @param cellShapeRoi
// * : ROI of segmented cell
// * @param cal
// * : calibration of image on which spot has been found
// */
// public SpotCollectionAnalysis(SpotCollection spotList, Measures measures,
// Roi cellShapeRoi, Calibration cal, Roi roiBeforeCrop) {
// this.cellShapeRoi = cellShapeRoi;
// this.roiBeforeCrop = roiBeforeCrop;
// this.measures = measures;
// int size = spotList.getNSpots(true);
// if (size == 1) {
// ReportingUtils.logMessage("Cell at interphase");
// length = 0;
// lengthToMajorAxis = 0;
// angleToMajorAxis = 0;
// } else if (size == 2) {
// ReportingUtils.logMessage("Cell at mitosis");
// double[] coordinates = getCoord(0, 1);
// setFeatures(coordinates, measures, cellShapeRoi, cal);
// } else {
// ReportingUtils.logMessage("Cell with multiple spots");
// double[] coordinates = findSPBCoordinates();
// setFeatures(coordinates, measures, cellShapeRoi, cal);
// }
// }
//
// /**
// * Method to get coordinates of 2 Spots in the spot list
// *
// * @param index1
// * : index of first spot
// * @param index2
// * : index of second spot
// * @return double[] where 0, 1 and 2 are x, y, z of spot1 and 3, 4 and 5 of
// * spot2
// */
// public double[] getCoord(int index1, int index2) {
//
// Map<String, Double> features1 = spotList.get(index1).getFeatures();
// Map<String, Double> features2 = spotList.get(index2).getFeatures();
//
// double[] coordinates = new double[6];
// if (features1.get("POSITION_Y") > features2.get("POSITION_Y")) {
// coordinates[0] = features1.get("POSITION_X");
// coordinates[1] = features1.get("POSITION_Y");
// coordinates[2] = features1.get("POSITION_Z") + 1;
// coordinates[3] = features2.get("POSITION_X");
// coordinates[4] = features2.get("POSITION_Y");
// coordinates[5] = features2.get("POSITION_Z") + 1;
// } else {
// coordinates[3] = features1.get("POSITION_X");
// coordinates[4] = features1.get("POSITION_Y");
// coordinates[5] = features1.get("POSITION_Z") + 1;
// coordinates[0] = features2.get("POSITION_X");
// coordinates[1] = features2.get("POSITION_Y");
// coordinates[2] = features2.get("POSITION_Z") + 1;
// }
// centerSpZ = (coordinates[2] + coordinates[5]) / 2;
//
// return coordinates;
// }
//
// /**
// * Method to find coordinates of the farthest spots
// *
// * @return double[] where 0, 1 and 2 are x, y, z of spot1 and 3, 4 and 5 of
// * spot2
// */
// public double[] findSPBCoordinates() {
// double lengthTemp = 0;
// double[] coordTemp = new double[6];
// for (int i = 0; i < spotList.size(); i++) {
// for (int j = 0; j < spotList.size(); j++) {
// if (j > i) {
// double[] coord = getCoord(i, j);
// double newLength = MyCoordinatesGeometry
// .getLengthBetween2Points(coord, true);
// if (newLength > lengthTemp) {
// coordTemp = coord;
// lengthTemp = newLength;
// }
// }
// }
// }
// return coordTemp;
// }
//
// /**
// * Method to set spindle features
// *
// * @param coordinates
// * @param measures
// * @param cellShapeRoi
// * @param cal
// */
// public void setFeatures(double[] coordinates, Measures measures,
// Roi cellShapeRoi, Calibration cal) {
//
// feature = "SPINDLE";
// double[] absoluteAngleLengthXYCenter = MyCoordinatesGeometry
// .getAngleLengthXYCenterFromCoor(coordinates, true);
// Line spindleLine = new Line(coordinates[0], coordinates[1],
// coordinates[3], coordinates[4]);
// length = spindleLine.getLength();
// lengthToMajorAxis = measures.getMajor() / length;
// spAbsoAng = absoluteAngleLengthXYCenter[0];
// ReportingUtils.logMessage("cellAngle : " + measures.getAngle() + "\n"
// + "spindleAngle : " + spAbsoAng);
// angleToMajorAxis = MyCoordinatesGeometry.getAngleToAxis(
// measures.getAngle(), spAbsoAng);
// if (angleToMajorAxis > 90) {
// angleToMajorAxis -= 180;
// angleToMajorAxis = Math.abs(angleToMajorAxis);
// }
// coordSPB = coordinates;
//
// // center in um
// centerSpX = absoluteAngleLengthXYCenter[2];
// centerSpY = absoluteAngleLengthXYCenter[3];
// // center of Roi in um
// centerCellX = measures.getXCentroid() - roiBeforeCrop.getXBase()
// * cal.pixelWidth;
// centerCellY = measures.getYCentroid() - roiBeforeCrop.getYBase()
// * cal.pixelHeight;
// ReportingUtils.logMessage("xcentroid " + measures.getXCentroid() + "\n"
// + "ycentroid " + measures.getYCentroid() + "\n" + "xbase "
// + +roiBeforeCrop.getXBase() + "\n" + "ybase "
// + roiBeforeCrop.getYBase());
// // pixel
// Line tempLine = new Line(centerCellX, centerCellY, centerSpX, centerSpY);
//
// angleSpCellCenter = tempLine.getAngle();
// // pixel
// lengthSpCellCenter = tempLine.getLength();
//
// }
//
// /**
// *
// * @return spindle absolute length
// */
// public double getLength() {
// return length;
// }
//
// /**
// *
// * @return spindle relative length
// */
// public double getLengthRatioToMajorAxis() {
// return lengthToMajorAxis;
// }
//
// /**
// *
// * @return spindle feature
// */
// public String getFeature() {
// return feature;
// }
//
// /**
// *
// * @return Spindle pole bodies coordinates
// */
// public double[] getSPBCoordinates() {
// return coordSPB;
// }
//
// /**
// *
// * @return relative spindle angle
// */
// public double getAngleToMajorAxis() {
// return angleToMajorAxis;
// }
//
// /**
// *
// * @return total number of spot detected
// */
// public int getNumberOfSpotDetected() {
// return spotList.size();
// }
//
// /**
// *
// * @return feature of this spindle
// */
// public String getSpindleFeature() {
// return this.feature;
// }
//
// /**
// * Method to write to JSON format features of spindle
// *
// * @param frame
// * (second)
// * @return
// */
// public String[] toList(double frame, double fieldX, double fieldY) {
// String[] spindleCoord = new String[25];
// spindleCoord[0] = cellShapeRoi.getName();
// spindleCoord[1] = String.valueOf(frame);
// spindleCoord[2] = feature;
// spindleCoord[3] = String.valueOf(getNumberOfSpotDetected());
// if (!feature.equals(NO_SPINDLE) && !feature.equals(NO_SPOT)) {
// spindleCoord[4] = String.valueOf(centerCellX);
// spindleCoord[5] = String.valueOf(centerCellY);
// spindleCoord[6] = String.valueOf(measures.getAngle());
// spindleCoord[7] = String.valueOf(measures.getMajor());
// spindleCoord[8] = String.valueOf(measures.getMinor());
// spindleCoord[9] = String.valueOf(spAbsoAng);
// spindleCoord[10] = String.valueOf(angleToMajorAxis);
// spindleCoord[11] = String.valueOf(length);
// spindleCoord[12] = String.valueOf(coordSPB[0]);
// spindleCoord[13] = String.valueOf(coordSPB[1]);
// spindleCoord[14] = String.valueOf(coordSPB[2]);
// spindleCoord[15] = String.valueOf(coordSPB[3]);
// spindleCoord[16] = String.valueOf(coordSPB[4]);
// spindleCoord[17] = String.valueOf(coordSPB[5]);
// spindleCoord[18] = String.valueOf(centerSpX);
// spindleCoord[19] = String.valueOf(centerSpY);
// spindleCoord[20] = String.valueOf(centerSpZ);
// spindleCoord[21] = String.valueOf(lengthSpCellCenter);
// spindleCoord[22] = String.valueOf(angleSpCellCenter);
// spindleCoord[23] = String.valueOf(fieldX);
// spindleCoord[24] = String.valueOf(fieldY);
// }
// return spindleCoord;
// }
// }