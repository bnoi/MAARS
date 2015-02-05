package fiji.plugin.maars.cellstateanalysis;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.measure.Calibration;

/**
 * Class to manipulate coordinates
 * @author marie
 *
 */
public class MyCoordinatesGeometry {
	
	/**
	 * Method to create an ROI corresponding to major axis of the object
	 * @param bfImage : image need to measure ROI length
	 * @param measures : measures (containing major axis length and angle and centroid coordinates)
	 * @param setWidth : true if Line is as wide as minor axis length
	 * @return Line object (linear ROI)
	 */
	public static Line computeCellLinearRoi(ImagePlus bfImage, Measures measures, boolean setWidth) {
		
		double[] coordinates = computeCellLinearRoiCoordinates(bfImage, measures);
		
		Line cellLineSelection;
		if (setWidth) {
			Line.setWidth((int) convertMinorAxisLengthToPixel(measures.getMinor(), measures.getAngle(), bfImage.getCalibration()));
		}
		else {
			Line.setWidth(1);
		}
		System.out.println("minor "+Math.round(measures.getMinor()));
		cellLineSelection = new Line(coordinates[0],
			coordinates[1],
			coordinates[2],
			coordinates[3]);
		
		return cellLineSelection;
	}
	
	/**
	 * Method to compute Linear ROI coordinates on an image
	 * @param bfImage : image on which coordinates are computed
	 * @param measures : Measure object containing informations on major axis (length, angle, centroid coordinates)
	 * @return double[] where 0 and 2 are x coordinates and 1 and 3 are y
	 */
	public static double[] computeCellLinearRoiCoordinates(ImagePlus bfImage, Measures measures) {
		
		double[] coordinates = computeCoordinatesOfMajorAxis(
				measures.getXCentroid(),
				measures.getYCentroid(),
				measures.getMajor(),
				measures.getAngle());
		
		if (bfImage.getCalibration().scaled()) {
			
			Line.setWidth((int) Math.round(convertMinorAxisLengthToPixel(measures.getMinor(),
					measures.getAngle(),
					bfImage.getCalibration())));
			coordinates[0] = coordinates[0]/bfImage.getCalibration().pixelWidth;
			coordinates[1] = coordinates[1]/bfImage.getCalibration().pixelHeight;
			coordinates[2] = coordinates[2]/bfImage.getCalibration().pixelWidth;
			coordinates[3] = coordinates[3]/bfImage.getCalibration().pixelHeight;
		}
		
		return coordinates;
	}
	
	/**
	 * Method to convert a length in micron to a length in pixel after converting angle to a value between 0 and 90°
	 * @param minorAxisLenght : length to convert
	 * @param angleMajorAxis : angle of line
	 * @param cal : Calibration object (needed for conversion)
	 * @return length in pixel
	 */
	public static double convertMinorAxisLengthToPixel(double minorAxisLenght, double angleMajorAxis, Calibration cal) {
		
		double newMinorAxisLength;
		double minorAxisAngle;
		
		if (angleMajorAxis == 0) {
			minorAxisAngle = angleRadiant(90);
		}
		else {
			if (angleMajorAxis == 90) {
				minorAxisAngle = angleRadiant(0);
			}
			else {
				if (angleMajorAxis < 90) {
					minorAxisAngle = angleRadiant(90 - angleMajorAxis);
				}
				else {
					minorAxisAngle = angleRadiant(angleMajorAxis - 90);
				}
			}
		}
		
		newMinorAxisLength = convertAxisLengthToPixel(minorAxisLenght, minorAxisAngle, cal);
		
		return newMinorAxisLength;
	}
	
	/**
	 * Method to convert a length in micron to a length in pixel
	 * @param length : length to convert
	 * @param angle : angle of line
	 * @param cal : Calibration object (needed for conversion)
	 * @return length in pixel
	 */
	public static double convertAxisLengthToPixel (double length, double angle, Calibration cal) {
		
		double adj = Math.cos(angle) * length;
		double op = Math.sin(angle) * length;
		
		double newLength = Math.sqrt(Math.pow(adj / cal.pixelWidth, 2 )+ Math.pow(op / cal.pixelHeight, 2 ));
		
		return newLength;
	}
	
	/**
	 * Method to convert a length in pixel to a length in micron
	 * @param length : length to convert
	 * @param angle : angle of line
	 * @param cal : Calibration object (needed for conversion)
	 * @return length in micron
	 */
	public static double convertAxisLengthToMicron (double length, double angle, Calibration cal) {
		
		double adj = Math.cos(angle) * length;
		double op = Math.sin(angle) * length;
		
		double newLength = Math.sqrt(Math.pow(adj * cal.pixelWidth, 2 )+ Math.pow(op * cal.pixelHeight, 2 ));
		
		return newLength;
	}

	/**
	 * Method to convert an angle in to degree to radiant
	 * @param angleDegree : angle in degree
	 * @return angle in radiant
	 */
	public static double angleRadiant(double angleDegree) {
		return Math.toRadians(angleDegree);
	}
	
	/**
	 * Method to compute coordinates of major axis using coordinates of its centre, its length, its angle
	 * @param xCentroid : x coordinate of it centre
	 * @param yCentroid : y coordinate of its centre
	 * @param majorAxisLenght : length
	 * @param angle : angle
	 * @return double[] where 0 and 1 are x and y coordinates of one extremity and 2 and 3 are coordinates of second extremity
	 */
	public static double[] computeCoordinatesOfMajorAxis(double xCentroid, double yCentroid, double majorAxisLenght, double angle) {
		
		double[] coordinates = new double[4]; // x1, y1, X2, y2
		double x1;
		double y1;
		double x2;
		double y2;
		double[] xy2;
		
		xy2 = convertPolarToCartesianCoor(xCentroid, yCentroid, majorAxisLenght/2, angle);
		y2 = xy2[1];
		x2 = xy2[0];
		y1 = 2 * yCentroid - y2;
		x1 = 2 * xCentroid - x2;
		
		if (y2 < y1) {
			coordinates[0] = x2;
			coordinates[1] = y2;
			coordinates[2] = x1;
			coordinates[3] = y1;
		}
		else {
			coordinates[0] = x1;
			coordinates[1] = y1;
			coordinates[2] = x2;
			coordinates[3] = y2;
		}
		return coordinates;
	}
	
	/**
	 * Method to compute the coordinates of line with maximum length possible given specific angle and centre coordinates
	 * @param cellShapeRoi : ROI of segmented cell
	 * @param xCentroid : x centre coordinate
	 * @param yCentroid : y centre coordinate
	 * @param angle : angle of line
	 * @return double[] where 0 and 1 are x and y coordinates of one extremity and 2 and 3 are coordinates of second extremity
	 */
	public static double[] computeCoordinatesOfAjutstedLengthAxis(Roi cellShapeRoi, double xCentroid, double yCentroid, double angle) {
		
		double[] coordinates = new double[4]; // x1, y1, X2, y2
		double x1;
		double y1;
		double x2;
		double y2;
		double[] xy2;
		double axisLength = 0;
		
		xy2 = convertPolarToCartesianCoor(xCentroid, yCentroid, axisLength, angle);
		while (cellShapeRoi.contains((int)xy2[0],(int)xy2[1])) {
			axisLength++;
			xy2 = convertPolarToCartesianCoor(xCentroid, yCentroid, axisLength, angle);
		}
		y2 = xy2[1];
		x2 = xy2[0];
		axisLength = 0;
		xy2 = convertPolarToCartesianCoor(xCentroid, yCentroid, axisLength, angle -180);
		while (cellShapeRoi.contains((int)xy2[0],(int)xy2[1])) {
			axisLength++;
			xy2 = convertPolarToCartesianCoor(xCentroid, yCentroid, axisLength, angle -180);
		}
		y1 = xy2[1];
		x1 = xy2[0];
		
		if (y2 < y1) {
			coordinates[0] = x2;
			coordinates[1] = y2;
			coordinates[2] = x1;
			coordinates[3] = y1;
		}
		else {
			coordinates[0] = x1;
			coordinates[1] = y1;
			coordinates[2] = x2;
			coordinates[3] = y2;
		}
		return coordinates;
	}
	
	/**
	 * Method to convert polar coordinates to Cartesian ones
	 * @param xRef : x coordinate of reference
	 * @param yRef : y coordinate of reference
	 * @param length
	 * @param angleDegree
	 * @return double[] where 0 is x and 1 is y
	 */
	public static double[] convertPolarToCartesianCoor(double xRef, double yRef, double length, double angleDegree) {
		
		double[] newXY = new double[2];
		double angle = angleRadiant(angleDegree);
		
		if (angle == Math.PI/2) {
			newXY[1] = yRef + (length);
			newXY[0] = xRef;
		}
		else {
			if (angle == 0) {
				newXY[1] = yRef;
				newXY[0] = xRef + (length);
			}
			else {
				
				newXY[1] = - Math.sin(angle) * (length) + yRef;
				newXY[0] = Math.cos(angle) * (length) + xRef;
			}
		}
		
		return newXY;
	}
	
	/**
	 * Method to get polar-like coordinates from Cartesian ones
	 * @param coordinates
	 * @param tridimentional
	 * @return double[] where 0 is angle, 1 is length, 2 and 3 are xy coordinates of reference
	 */
	public static double[] getAngleLengthXYCenterFromCoor (double[] coordinates, boolean tridimentional) {
		if (tridimentional) {
			// TODO Change that and compute 3D stuff
			double[] angleLengthXYCenter = new double[4];
			
			Line spindleLine = new Line(coordinates[0],
					coordinates[1],
					coordinates[3],
					coordinates[4]);
			
			angleLengthXYCenter[0] = spindleLine.getAngle((int)coordinates[0],
					(int) coordinates[1],
					(int) coordinates[3],
					(int )coordinates[4]);
			angleLengthXYCenter[1] = spindleLine.getLength();
			double[] XYCenter = new double[2];
			
			XYCenter = convertPolarToCartesianCoor(coordinates[0], coordinates[1], angleLengthXYCenter[1]/2, angleLengthXYCenter[0]);
			
			angleLengthXYCenter[2] = XYCenter[0];
			angleLengthXYCenter[3] = XYCenter[1];
			return angleLengthXYCenter;
		}
		else {
			double[] angleLengthXYCenter = new double[4];
			
			Line spindleLine = new Line(coordinates[0],
					coordinates[1],
					coordinates[2],
					coordinates[3]);
			
			angleLengthXYCenter[0] = spindleLine.getAngle((int)coordinates[0],
					(int) coordinates[1],
					(int) coordinates[2],
					(int )coordinates[3]);
			angleLengthXYCenter[1] = spindleLine.getLength();
			double[] XYCenter = new double[2];
			
			XYCenter = convertPolarToCartesianCoor(coordinates[0], coordinates[1], angleLengthXYCenter[1]/2, angleLengthXYCenter[0]);
			
			angleLengthXYCenter[2] = XYCenter[0];
			angleLengthXYCenter[3] = XYCenter[1];
			return angleLengthXYCenter;
		}
	}

	/**
	 * Method to get difference between 2 angles
	 * @param angleMajorAxis
	 * @param otherAngle
	 * @return
	 */
	public static double getAngleToAxis(double angleAxis, double otherAngle) {
		
		//return Math.abs(angleAxis - otherAngle);
		return (angleAxis - otherAngle);
	}
	
	/**
	 * Method to get length between 2 points
	 * @param coordinates
	 * @param tridimentional
	 * @return
	 */
	public static double getLengthBetween2Points(double[] coordinates, boolean tridimentional) {
		if (tridimentional) {
			// TODO Change that and compute 3D stuff
			return Math.sqrt(Math.pow(coordinates[0] - coordinates[3], 2)+Math.pow(coordinates[1] - coordinates[4], 2));
		}
		else {
			return Math.sqrt(Math.pow(coordinates[0] - coordinates[2], 2)+Math.pow(coordinates[1] - coordinates[3], 2));
		}
	}
	
}
