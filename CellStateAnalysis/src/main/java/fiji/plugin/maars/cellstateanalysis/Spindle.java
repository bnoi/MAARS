package fiji.plugin.maars.cellstateanalysis;

import ij.gui.Line;

import ij.gui.Roi;
import ij.measure.Calibration;

import java.util.ArrayList;
import java.util.Map;

import org.micromanager.utils.ReportingUtils;

import fiji.plugin.trackmate.Spot;

/**
 * Class compute, manipulate and store informations about mitotic spindle
 * 
 * @author marie
 *
 */
public class Spindle {
	private double angleSpCellCenter;
	private double lengthSpCellCenter;
	private double centerCellX;
	private double centerCellY;
	private double centerSpX;
	private double centerSpY;
	private double centerSpZ;
	private double length;
	private double lengthToMajorAxis;
	private double angleToMajorAxis;
	private double spAbsoAng;
	private String feature;
	private Roi cellShapeRoi;
	private Roi roiBeforeCrop;
	private Measures measures;
	private ArrayList<Spot> spotList;
	private double[] coordSPB;

	public static final String NO_SPOT = "NO_SPOT";
	public static final String NO_SPINDLE = "NO_SPINDLE";

	/**
	 * Constructor :
	 * 
	 * @param spotList
	 *            : list of Spot objects corresponding to spot detected by
	 *            SpotDetector (in CellFluoAnalysis)
	 * @param measures
	 *            : Measure object corresponding to cell
	 * @param cellShapeRoi
	 *            : ROI of segmented cell
	 * @param cal
	 *            : calibration of image on which spot has been found
	 */
	public Spindle(ArrayList<Spot> spotList, Measures measures,
			Roi cellShapeRoi, Calibration cal, Roi roiBeforeCrop) {
		this.spotList = spotList;
		this.cellShapeRoi = cellShapeRoi;
		this.roiBeforeCrop = roiBeforeCrop;
		this.measures = measures;
		int size = spotList.size();
		if (size == 0) {
			length = 0;
			lengthToMajorAxis = 0;
			angleToMajorAxis = 0;
			coordSPB = null;

			feature = "NO_SPOT";
		} else {
			if (size == 1) {
				length = 0;
				lengthToMajorAxis = 0;
				angleToMajorAxis = 0;
				coordSPB = null;

				feature = "NO_SPINDLE";
			} else {
				if (size == 2) {

					double[] coordinates = getCoord(0, 1);
					setFeatures(coordinates, measures, cellShapeRoi, cal);

				} else {
					double[] coordinates = findSPBCoordinates();
					setFeatures(coordinates, measures, cellShapeRoi, cal);
				}
			}
		}
	}

	/**
	 * Method to get coordinates of 2 Spots in the spot list
	 * 
	 * @param index1
	 *            : index of first spot
	 * @param index2
	 *            : index of second spot
	 * @return double[] where 0, 1 and 2 are x, y, z of spot1 and 3, 4 and 5 of
	 *         spot2
	 */
	public double[] getCoord(int index1, int index2) {

		Map<String, Double> features1 = spotList.get(index1).getFeatures();
		Map<String, Double> features2 = spotList.get(index2).getFeatures();

		double[] coordinates = new double[6];
		if (features1.get("POSITION_Y") > features2.get("POSITION_Y")) {
			coordinates[0] = features1.get("POSITION_X");
			coordinates[1] = features1.get("POSITION_Y");
			coordinates[2] = features1.get("POSITION_Z") + 1;
			coordinates[3] = features2.get("POSITION_X");
			coordinates[4] = features2.get("POSITION_Y");
			coordinates[5] = features2.get("POSITION_Z") + 1;
		}else{
			coordinates[3] = features1.get("POSITION_X");
			coordinates[4] = features1.get("POSITION_Y");
			coordinates[5] = features1.get("POSITION_Z") + 1;
			coordinates[0] = features2.get("POSITION_X");
			coordinates[1] = features2.get("POSITION_Y");
			coordinates[2] = features2.get("POSITION_Z") + 1;
		}
		centerSpZ = (coordinates[2] + coordinates[5])/2;

		return coordinates;
	}

	/**
	 * Method to find coordinates of the farthest spots
	 * 
	 * @return double[] where 0, 1 and 2 are x, y, z of spot1 and 3, 4 and 5 of
	 *         spot2
	 */
	public double[] findSPBCoordinates() {
		double lengthTemp = 0;
		double[] coordTemp = new double[6];
		for (int i = 0; i < spotList.size(); i++) {
			for (int j = 0; j < spotList.size(); j++) {
				if (j > i) {
					double[] coord = getCoord(i, j);
					double newLength = MyCoordinatesGeometry
							.getLengthBetween2Points(coord, true);
					if (newLength > lengthTemp) {
						coordTemp = coord;
						lengthTemp = newLength;
					}
				}
			}
		}
		return coordTemp;
	}

	/**
	 * Method to set spindle features
	 * 
	 * @param coordinates
	 * @param measures
	 * @param cellShapeRoi
	 * @param cal
	 */
	public void setFeatures(double[] coordinates, Measures measures,
			Roi cellShapeRoi, Calibration cal) {

		feature = "SPINDLE";
		double[] absoluteAngleLengthXYCenter = MyCoordinatesGeometry
				.getAngleLengthXYCenterFromCoor(coordinates, true);
		Line spindleLine = new Line(coordinates[0],coordinates[1],
				coordinates[3],coordinates[4]);
		length =  spindleLine.getLength();
		lengthToMajorAxis = measures.getMajor() / length;
		spAbsoAng = absoluteAngleLengthXYCenter[0];
		ReportingUtils.logMessage("cellAngle : "+measures.getAngle()+"\n"+ 
					"spindleAngle : " + spAbsoAng);
		angleToMajorAxis = MyCoordinatesGeometry.getAngleToAxis(
				measures.getAngle(), spAbsoAng);
		if(angleToMajorAxis>90){
			angleToMajorAxis -= 180;
			angleToMajorAxis = Math.abs(angleToMajorAxis);
		}
		coordSPB = coordinates;

		//center in um
		centerSpX = absoluteAngleLengthXYCenter[2];
		centerSpY = absoluteAngleLengthXYCenter[3];
		//center of Roi in um
		centerCellX = measures.getXCentroid() - roiBeforeCrop.getXBase() * cal.pixelWidth;
		centerCellY = measures.getYCentroid() - roiBeforeCrop.getYBase() * cal.pixelHeight;
		ReportingUtils.logMessage("xcentroid " + measures.getXCentroid()+ "\n"+
				"ycentroid " +measures.getYCentroid()+ "\n"+
				"xbase " + +roiBeforeCrop.getXBase()+ "\n"+
				"ybase " + roiBeforeCrop.getYBase()
				);
		//pixel
		Line tempLine = new Line(centerCellX, centerCellY, centerSpX,
				centerSpY);
		
		angleSpCellCenter = tempLine.getAngle();
		//pixel
		lengthSpCellCenter = tempLine.getLength();

	}

//	/**
//	 * Method to see what was detected by the program : it returns a duplicate
//	 * of image entered where cell boundaries are drawn in pink, major and minor
//	 * axis in yellow and spindle in blue
//	 * 
//	 * @param croppedFluoImg
//	 * @return
//	 */
//	public ImagePlus testFunction(ImagePlus croppedFluoImg) {
//
//		croppedFluoImg.deleteRoi();
//
//		double[] coorMaj = MyCoordinatesGeometry.computeCoordinatesOfMajorAxis(
//				measures.getXCentroid(), measures.getYCentroid(),
//				measures.getMajor(), measures.getAngle());
//
//		Line maj = new Line(
//				(coorMaj[0] / croppedFluoImg.getCalibration().pixelWidth)
//						- cellShapeRoi.getXBase(),
//				(coorMaj[1] / croppedFluoImg.getCalibration().pixelHeight)
//						- cellShapeRoi.getYBase(),
//				(coorMaj[2] / croppedFluoImg.getCalibration().pixelWidth)
//						- cellShapeRoi.getXBase(),
//				(coorMaj[3] / croppedFluoImg.getCalibration().pixelHeight)
//						- cellShapeRoi.getYBase());
//
//		double[] coorMin = MyCoordinatesGeometry.computeCoordinatesOfMajorAxis(
//				measures.getXCentroid(), measures.getYCentroid(),
//				measures.getMinor(), measures.getAngle() + 90);
//
//		Line min = new Line(
//				(coorMin[0] / croppedFluoImg.getCalibration().pixelWidth)
//						- cellShapeRoi.getXBase(),
//				(coorMin[1] / croppedFluoImg.getCalibration().pixelHeight)
//						- cellShapeRoi.getYBase(),
//				(coorMin[2] / croppedFluoImg.getCalibration().pixelWidth)
//						- cellShapeRoi.getXBase(),
//				(coorMin[3] / croppedFluoImg.getCalibration().pixelHeight)
//						- cellShapeRoi.getYBase());
//
//		Line spindleLine;
//
//		if (spotList.size() == 2) {
//			spindleLine = new Line(coordSPB[0], coordSPB[1], coordSPB[3],
//					coordSPB[4]);
//		} else {
//			spindleLine = null;
//		}
//
//		cellShapeRoi.setLocation(0, 0);
//
//		ImageStack ims = new ImageStack(croppedFluoImg.getWidth(),
//				croppedFluoImg.getHeight());
//
//		for (int i = 1; i <= croppedFluoImg.getNSlices(); i++) {
//			croppedFluoImg.setSlice(i);
//			ColorProcessor newIp = croppedFluoImg.getProcessor().duplicate()
//					.convertToColorProcessor();
//			newIp.setColor(Color.PINK);
//			cellShapeRoi.drawPixels(newIp);
//			newIp.setColor(Color.YELLOW);
//			maj.drawPixels(newIp);
//			min.drawPixels(newIp);
//			if (spotList.size() == 2) {
//				newIp.setColor(Color.BLUE);
//				spindleLine.drawPixels(newIp);
//			}
//
//			ims.addSlice(newIp);
//
//		}
//
//		ImagePlus im = new ImagePlus("Cell " + cellShapeRoi.getName(), ims);
//
//		return im;
//	}
//
//	/**
//	 * Method to get a schematic representation of the spindle according to the
//	 * cell dimensions
//	 * 
//	 * @param width
//	 *            : cell dimension for representation
//	 * @param heigth
//	 *            : cell dimension for representation
//	 * @param cal
//	 * @param absoluteSpindleLength
//	 * @param absoluteXYCenterCoord
//	 * @return an image Processor
//	 */
//	public ColorProcessor getProcessorReferencedSpindle(int width, int heigth,
//			Calibration cal, boolean absoluteSpindleLength,
//			boolean absoluteXYCenterCoord) {
//
//		double spindleLength;
//		int x;
//		int y;
//		if (absoluteSpindleLength) {
//			spindleLength = MyCoordinatesGeometry.convertAxisLengthToPixel(
//					length, angleToMajorAxis + 90, cal);
//		} else {
//			spindleLength = heigth / lengthToMajorAxis;
//		}
//
//		if (absoluteXYCenterCoord) {
//			x = (int) Math
//					.round((width / 2)
//							+ (XYCenterAbsolutePositionToMajorMinorAxis[0] / cal.pixelWidth));
//			y = (int) Math
//					.round((heigth / 2)
//							+ (XYCenterAbsolutePositionToMajorMinorAxis[1] / cal.pixelHeight));
//		} else {
//			x = (int) Math.round((width / 2)
//					+ (width / XYCenterRelativePositionToMajorMinorAxis[0]));
//			y = (int) Math.round((heigth / 2)
//					+ (heigth / XYCenterRelativePositionToMajorMinorAxis[1]));
//		}
//
//		ColorProcessor cp = new ColorProcessor(width, heigth);
//
//		Line maj = new Line(width / 2, 0, width / 2, heigth);
//
//		Line min = new Line(0, heigth / 2, width, heigth / 2);
//
//		double[] spindleCoord = MyCoordinatesGeometry
//				.computeCoordinatesOfMajorAxis(x, y, spindleLength,
//						angleToMajorAxis + 90);
//
//		Line spindle = new Line(spindleCoord[0], spindleCoord[1],
//				spindleCoord[2], spindleCoord[3]);
//
//		OvalRoi center = new OvalRoi(x, y, 1, 1);
//
//		cp.setColor(Color.YELLOW);
//		maj.drawPixels(cp);
//		min.drawPixels(cp);
//
//		cp.setColor(Color.BLUE);
//		spindle.drawPixels(cp);
//
//		cp.setColor(Color.RED);
//		center.drawPixels(cp);
//
//		System.out.println("x center : " + x);
//		System.out.println("y center : " + y);
//
//		return cp;
//	}

	/**
	 * 
	 * @return spindle absolute length
	 */
	public double getLength() {
		return length;
	}

	/**
	 * 
	 * @return spindle relative length
	 */
	public double getLengthRatioToMajorAxis() {
		return lengthToMajorAxis;
	}

	/**
	 * 
	 * @return spindle feature
	 */
	public String getFeature() {
		return feature;
	}

	/**
	 * 
	 * @return Spindle pole bodies coordinates
	 */
	public double[] getSPBCoordinates() {
		return coordSPB;
	}

	/**
	 * 
	 * @return relative spindle angle
	 */
	public double getAngleToMajorAxis() {
		return angleToMajorAxis;
	}

	/**
	 * 
	 * @return total number of spot detected
	 */
	public int getNumberOfSpotDetected() {
		return spotList.size();
	}

	/**
	 * 
	 * @return feature of this spindle
	 */
	public String getSpindleFeature() {
		return this.feature;
	}

	/**
	 * Method to write to JSON format features of spindle
	 * 
	 * @param name
	 * @return
	 */
	public String[] toList(int frame) {
		String[] spindle = new String[23];
		if (!feature.equals(NO_SPINDLE) && !feature.equals(NO_SPOT)) 
		{
			spindle[0] = cellShapeRoi.getName();
			spindle[1] = String.valueOf(frame);
			spindle[2] = feature;
			spindle[3] = String.valueOf(getNumberOfSpotDetected());
			spindle[4] = String.valueOf(centerCellX);
			spindle[5] = String.valueOf(centerCellY);
			spindle[6] = String.valueOf(measures.getAngle());
			spindle[7] = String.valueOf(measures.getMajor());
			spindle[8] = String.valueOf(measures.getMinor());
			spindle[9] = String.valueOf(spAbsoAng);
			spindle[10] = String.valueOf(angleToMajorAxis);
			spindle[11] = String.valueOf(length);
			spindle[12] = String.valueOf(coordSPB[0]);
			spindle[13] = String.valueOf(coordSPB[1]);
			spindle[14] = String.valueOf(coordSPB[2]);
			spindle[15] = String.valueOf(coordSPB[3]);
			spindle[16] = String.valueOf(coordSPB[4]);
			spindle[17] = String.valueOf(coordSPB[5]);
			spindle[18] = String.valueOf(centerSpX);
			spindle[19] = String.valueOf(centerSpY);
			spindle[20] = String.valueOf(centerSpZ);
			spindle[21] = String.valueOf(lengthSpCellCenter);
			spindle[22] = String.valueOf(angleSpCellCenter);
		}else{
			spindle[0] = cellShapeRoi.getName();
			spindle[1] = String.valueOf(frame);
			spindle[2] = feature;
			spindle[3] = String.valueOf(getNumberOfSpotDetected());
			spindle[4] = "";
			spindle[5] = "";
			spindle[6] = "";
			spindle[7] = "";
			spindle[8] = "";
			spindle[9] = "";
			spindle[10] = "";
			spindle[11] = "";
			spindle[12] = "";
			spindle[13] = "";
			spindle[14] = "";
			spindle[15] = "";
			spindle[16] = "";
			spindle[17] = "";
			spindle[18] = "";
			spindle[19] = "";
			spindle[20] = "";
			spindle[21] = "";
			spindle[22] = "";
		}

		return spindle;
	}
}
