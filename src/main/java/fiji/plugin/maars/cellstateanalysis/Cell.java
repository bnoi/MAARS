package fiji.plugin.maars.cellstateanalysis;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.micromanager.internal.utils.ReportingUtils;

import ij.process.ImageProcessor;
import fiji.plugin.trackmate.Spot;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.RoiScaler;

/**
 * Cell is a class containing information about cell image, including its
 * mitotic state, its shape, ...
 * 
 * @author marie
 *
 */
public class Cell {

	// tools
	private ImagePlus bfImage;
	private ImagePlus fluoImage;
	private ImagePlus croppedfluoImage;
	public double bf2FluoWidthFac;
	public double bf2FluoHeightFac;
	private ImageStack croppedFluoStack = new ImageStack();
	private Roi cellShapeRoi;
	private Roi rescaledCellShapeRoi;
	private Roi croppedRoi;
	// private Line cellLinearRoi;

	// informations
	private int cellNumber;
	private int maxNbSpotPerCell;
	// private int septumNumber;
	// private ArrayList<Septum> septumArray;
	private Measures measures;
	private Spindle lastSpindleComputed;

	private CellFluoAnalysis fluoAnalysis;
	private ArrayList<Spot> spotList;

	private boolean isAlive;

	public static final String CELLSHAPE = "cellShapeROI";
	public static final String CELLLINE = "cellLinearROI";

	/**
	 * Constructor :
	 * 
	 * @param bfImage
	 *            : the brightfield image used for segmentation
	 * @param fluoImage
	 *            : the fluorescent image to determine mitotic state of the cell
	 * @param focusSlice
	 *            : focus slice of brightfield image
	 * @param direction
	 *            : direction : -1 -> cell bounds are black then white 1 -> cell
	 *            bounds are white then black
	 * @param roiCellShape
	 *            : ROI that correspond to segmented cell
	 * @param rt
	 *            : result table used to display result of analysis (measures on
	 *            cell)
	 * @param maxNbSpotPerCell
	 *            : maximum number of spots in each cell.
	 */
	public Cell(ImagePlus bfImage, ImagePlus fluoImage, int focusSlice, Roi roiCellShape, int cellNb, ResultsTable rt,
			int maxNbSpotPerCell) {

		this.bfImage = bfImage;
		lastSpindleComputed = null;
		this.cellShapeRoi = roiCellShape;
		rt.reset();
		measures = new Measures(bfImage, focusSlice, roiCellShape, rt);
		this.cellNumber = cellNb;
		this.maxNbSpotPerCell = maxNbSpotPerCell;
		updateFluoImage(fluoImage);
	}

	/**
	 * 
	 * @param bfImage
	 *            : the brightfield image used for segmentation
	 * @param focusSlice
	 *            : focus slice of brightfield image
	 * @param direction
	 *            direction : -1 -> cell bounds are black then white 1 -> cell
	 *            bounds are white then black
	 * @param roiCellShape
	 *            : ROI that correspond to segmented cell
	 * @param rt
	 *            : result table used to display result of analysis (measures on
	 *            cell)
	 * @param maxNbSpotPerCell
	 *            : maximum number of spots in each cell.
	 */
	public Cell(ImagePlus bfImage, int focusSlice, Roi roiCellShape, int cellNb, ResultsTable rt,
			int maxNbSpotPerCell) {

		ReportingUtils.logMessage("Cell " + roiCellShape.getName());
		ReportingUtils.logMessage("Get parameters");
		this.bfImage = bfImage;
		this.cellShapeRoi = roiCellShape;
		ReportingUtils.logMessage("Done");
		ReportingUtils.logMessage("Reset result table");
		rt.reset();
		ReportingUtils.logMessage("Done.");

		lastSpindleComputed = null;
		this.cellNumber = cellNb;
		ReportingUtils.logMessage("Create Measure object");
		measures = new Measures(bfImage, focusSlice, roiCellShape, rt);
		ReportingUtils.logMessage("done");
		this.maxNbSpotPerCell = maxNbSpotPerCell;
	}

	// /**
	// * Method used to find if the cell has a septum (DOES NOT WORK WELL): the
	// * method is based on transversal intensity plot analysis of the cell.
	// *
	// * @param threshold
	// */
	// public void findSeptum(double threshold) {
	// septumArray = new ArrayList<Septum>();
	// septumNumber = 0;
	// cellLinearRoi = MyCoordinatesGeometry.computeCellLinearRoi(bfImage,
	// measures, true);
	// cellLinearRoi.setName("Linear_" + cellShapeRoi.getName());
	//
	// correlationImage.setRoi(cellLinearRoi);
	// ProfilePlot profilePlot = new ProfilePlot(correlationImage);
	// double[] profileArray = profilePlot.getProfile();
	//
	// /*
	// * if (cellShapeRoi.getName().equals("106")) {
	// * profilePlot.createWindow(); }
	// */
	//
	// // slopeImage.show();
	// // profilePlot.createWindow();
	//
	// double[][] peaks = findBoundariesPeaks(profileArray);
	// ReportingUtils.logMessage("\n...........................\ncell : "
	// + cellShapeRoi.getName() + "\n...........................\n");
	// ReportingUtils.logMessage("peak 1 = " + peaks[0][0] + " peak 2 = "
	// + peaks[1][0]);
	//
	// for (int i = (int) peaks[0][0] + 1; i < peaks[1][0]; i++) {
	// if (profileArray[i] > profileArray[i + 1]
	// && profileArray[i] > profileArray[i - 1]
	// && profileArray[i] > 0) {
	// // ReportingUtils.logMessage("i = "+i);
	// boolean isSeptum = false;
	// for (int newAngle = 180; newAngle > 0; newAngle = newAngle - 3) {
	// if (!isSeptum) {
	// int peakWidth = getPeakWidth(profileArray, i);
	// double[] plotNewAngledegres = getNewAngleDegresPlot(
	// cellLinearRoi, cellShapeRoi, measures,
	// correlationImage, i, peakWidth, newAngle);
	// double[][] peaksNewAngledegrees =
	// findBoundariesPeaks(plotNewAngledegres);
	// double[] variableNewAngledegreeThreshold =
	// findVariableThreshold(peaksNewAngledegrees);
	// int j = (int) peaksNewAngledegrees[0][0];
	// // IJ.wait(500);
	// // int peakNumber = 0;
	//
	// //
	// ReportingUtils.logMessage("peak 1 = "+peaksNewAngledegrees[0][0]+" peak 2
	// = "+peaksNewAngledegrees[1][0]);
	//
	// while (j < (int) peaksNewAngledegrees[1][0]
	// && (variableNewAngledegreeThreshold[0] +
	// variableNewAngledegreeThreshold[1]
	// * j)
	// / plotNewAngledegres[j] < threshold
	// && plotNewAngledegres[j] > 0) {
	// // ReportingUtils.logMessage((variableNewAngledegreeThreshold[0]
	// // + variableNewAngledegreeThreshold[1] * j)/
	// // plotNewAngledegres[j]);
	// /*
	// * if (plotNewAngledegres[j] >
	// * plotNewAngledegres[j+1] && plotNewAngledegres[j]
	// * > plotNewAngledegres[j-1]) { peakNumber++; }
	// */
	// j++;
	// }
	// if (j == (int) peaksNewAngledegrees[1][0]) {// &&
	// // peakNumber
	// // < 2) {
	// ReportingUtils.logMessage("\n---\nseptum pos : " + i
	// + "\n---\n");
	// isSeptum = true;
	// septumNumber += 1;
	//
	// }
	// }
	// }
	// }
	//
	// }
	// // TODO finish that
	// }
	//
	// /**
	// * Method which return an array of double containing plot intensity values
	// * according to a line defined by a specific angle and position relative
	// to
	// * cell major axis.
	// *
	// * @param cellLinearRoi
	// * : linear ROI corresponding to major axis
	// * @param cellShapeRoi
	// * : ROI corresponding to segmented cell
	// * @param measures
	// * : Measures object of cell
	// * @param correlationImage
	// * : correlation image of cell
	// * @param peakPosition
	// * : position of centre of new line
	// * @param peakWidth
	// * : width of new line
	// * @param newAngleDegree
	// * : angle in degree of new line
	// * @return double[]
	// */
	// public double[] getNewAngleDegresPlot(Line cellLinearRoi, Roi
	// cellShapeRoi,
	// Measures measures, ImagePlus correlationImage, int peakPosition,
	// int peakWidth, int newAngleDegree) {
	//
	// ProfilePlot ppNewAngle;
	// double[] plotNewAngleDegres;
	// double[] majorAxisCoordinates = MyCoordinatesGeometry
	// .computeCellLinearRoiCoordinates(bfImage, measures);
	// Line lineNewAngledegres;
	// double[] newCoordinates;
	// double[] xyCentroidNewSelection = MyCoordinatesGeometry
	// .convertPolarToCartesianCoor(majorAxisCoordinates[0],
	// majorAxisCoordinates[1], peakPosition,
	// measures.getAngle() + 180);
	// //
	// ReportingUtils.logMessage("supposed 0 : x = "+majorAxisCoordinates[0]+" y
	// = "+majorAxisCoordinates[1]);
	// //
	// ReportingUtils.logMessage("centroid new selection : x =
	// "+xyCentroidNewSelection[0]+" y = "+xyCentroidNewSelection[1]);
	// /*
	// * double minorAxis; if(bfImage.getCalibration().scaled()) { minorAxis =
	// * convertMinorAxisLength(measures.getMinor(), measures.getMajor(),
	// * bfImage.getCalibration()); } else { minorAxis = measures.getMinor();
	// * }
	// *
	// * newCoordinates =
	// * computeCoordinatesOfMajorAxis(xyCentroidNewSelection[0],
	// * xyCentroidNewSelection[1], minorAxis,
	// * measures.getAngle()-newAngleDegree);
	// */
	// newCoordinates = MyCoordinatesGeometry
	// .computeCoordinatesOfAjutstedLengthAxis(cellShapeRoi,
	// xyCentroidNewSelection[0], xyCentroidNewSelection[1],
	// measures.getAngle() - newAngleDegree);
	//
	// Line.setWidth(peakWidth);
	// lineNewAngledegres = new Line(newCoordinates[0], newCoordinates[1],
	// newCoordinates[2], newCoordinates[3]);
	//
	// correlationImage.setRoi(lineNewAngledegres);
	// ppNewAngle = new ProfilePlot(correlationImage);
	//
	// // test
	// /*
	// * if (cellShapeRoi.getName().equals("26")) { ppNewAngle.createWindow();
	// * }
	// */
	// plotNewAngleDegres = ppNewAngle.getProfile();
	//
	// return plotNewAngleDegres;
	// }
	//
	// /**
	// * Method to get peak width :
	// * peak position
	// * |
	// * v
	// * ..
	// * . .
	// * . .
	// * . .
	// * ..... .......... plot
	// * <---->
	// * peak width
	// *
	// * @param plot
	// * @param peakPosition : peak position
	// * @return
	// */
	// public int getPeakWidth(double[] plot, int peakPosition) {
	//
	// // ReportingUtils.logMessage("peak position : "+peakPosition);
	// // ReportingUtils.logMessage("length plot : "+plot.length);
	//
	// boolean firstBottomFound = false;
	// int firstBottom = 0;
	// boolean lastBottomFound = false;
	// int lastBottom = plot.length - 1;
	// int i = 1;
	// while (!firstBottomFound && i < peakPosition) {
	//
	// if (plot[peakPosition - (i + 1)] > plot[peakPosition - i]) {
	// firstBottom = i;
	// firstBottomFound = true;
	// }
	// i++;
	// }
	// i = 1;
	// while (!lastBottomFound && i < plot.length - peakPosition) {
	// if (plot[peakPosition + (i + 1)] > plot[peakPosition + i]) {
	// lastBottom = i;
	// lastBottomFound = true;
	// }
	// i++;
	// }
	// return (lastBottom - firstBottom);
	// }
	//
	// /**
	// * Method to find most external peaks
	// *
	// * ..
	// * . . . .
	// * . . . .
	// * . . . .
	// * ..... ......... .... plot
	// * ^ ^
	// * | |
	// * peak peak
	// *
	// * @param plot
	// * @return
	// */
	// public double[][] findBoundariesPeaks(double[] plot) {
	// double[][] peaks = new double[2][2];
	// int i = 1;
	// boolean firstPeakFound = false;
	// boolean lastPeakFound = false;
	//
	// while ((!firstPeakFound || !lastPeakFound) && i < plot.length) {
	// if (!firstPeakFound) {
	// if (plot[i + 1] < plot[i] && plot[i - 1] < plot[i]) {
	// peaks[0][0] = i;
	// peaks[0][1] = plot[i];
	// firstPeakFound = true;
	// }
	// }
	// if (!lastPeakFound) {
	// if (plot[(plot.length - 1) - (i + 1)] < plot[(plot.length - 1)
	// - i]
	// && plot[(plot.length - 1) - (i - 1)] < plot[(plot.length - 1)
	// - i]) {
	// peaks[1][0] = (plot.length - 1) - i;
	// peaks[1][1] = plot[(plot.length - 1) - i];
	// lastPeakFound = true;
	// }
	// }
	//
	// i++;
	// }
	// return peaks;
	// }
	//
	// /**
	// * find parameters of a linear equation y = ax + b (a and b) that go
	// through
	// * 2 peaks
	// *
	// * @param peaks
	// * @return double[] where parameters[1] = a and parameters[0] = b
	// */
	// public double[] findVariableThreshold(double[][] peaks) {
	// double[] parameters = new double[2];
	// parameters[1] = (peaks[1][1] - peaks[0][1])
	// / (peaks[1][0] - peaks[0][0]);
	// parameters[0] = peaks[0][1] - (parameters[1] * peaks[0][0]);
	// return parameters;
	// }
	//
	// /**
	// * Return number of septa predicted
	// *
	// * @return
	// */
	// public int septumNumber() {
	// return septumNumber;
	// }

	/**
	 * Method to find fluorescent spots on cell image and create a Spindle
	 * object
	 * 
	 * @param crop
	 *            : if the image contains only the cell, set false, if the image
	 *            contains the whole field, set true
	 * @param spotRadius
	 *            : typical spot radius
	 * @return Spindle object
	 */
	public Spindle findFluoSpotTempFunction(double spotRadius) {

		ReportingUtils.logMessage("Create CellFluoAnalysis object");
		try {
			this.fluoAnalysis = new CellFluoAnalysis(this, spotRadius);
		} catch (InterruptedException e) {
			ReportingUtils.logMessage("Can't create CellFluoAnalysis object");
		}
		ReportingUtils.logMessage("Get fluorescent spot on image");
		spotList = fluoAnalysis.getSpots();
		//TODO
		ReportingUtils.logMessage("Create spindle using spots found");
		Spindle spindle = new Spindle(spotList, measures, croppedRoi, fluoImage.getCalibration(), cellShapeRoi);

		ReportingUtils.logMessage("Cell : " + croppedRoi.getName() + " spots nb : " + spotList.size());
		ReportingUtils.logMessage("Done.");
		ReportingUtils.logMessage("Return spindle");
		lastSpindleComputed = spindle;
		return spindle;
	}

	/**
	 * Method to add or update fluorescent image corresponding to cell
	 * 
	 * @param fluoImage
	 */
	public void updateFluoImage(ImagePlus fluoImg) {
		fluoImage = null;
		fluoImage = fluoImg;
	}

	/**
	 * calibrate Fluo Image and compute a scale factor
	 * 
	 * @param fluoImg
	 */
	public void setBfFluocalibFactor() {
		if (fluoImage.getCalibration().getUnit().equals("cm")) {
			fluoImage.getCalibration().setUnit("micron");
			fluoImage.getCalibration().pixelWidth = fluoImage.getCalibration().pixelWidth * 10000;
			fluoImage.getCalibration().pixelHeight = fluoImage.getCalibration().pixelHeight * 10000;
		}
		if (bfImage.getCalibration().equals(fluoImage.getCalibration())) {
			bf2FluoWidthFac = 1;
			bf2FluoHeightFac = 1;
		} else {
			bf2FluoWidthFac = bfImage.getCalibration().pixelWidth / fluoImage.getCalibration().pixelWidth;
			bf2FluoHeightFac = bfImage.getCalibration().pixelHeight / fluoImage.getCalibration().pixelHeight;
		}
	}

	/**
	 * 
	 * Crop filed-wide image with cell roi
	 * 
	 */

	public void cropFluoImage() {
		ImageProcessor imgProcessor = fluoImage.getProcessor();
		imgProcessor.setInterpolationMethod(ImageProcessor.BILINEAR);
		Rectangle newRectangle = new Rectangle((int) rescaledCellShapeRoi.getXBase(),
				(int) rescaledCellShapeRoi.getYBase(), (int) rescaledCellShapeRoi.getBounds().width,
				(int) rescaledCellShapeRoi.getBounds().height);
		imgProcessor.setRoi(newRectangle);

		ReportingUtils.logMessage("Create cropped fluo image");
		croppedfluoImage = new ImagePlus("croppedImage", imgProcessor.crop());

		ReportingUtils.logMessage("Put new calibration newly cropped image");
		croppedfluoImage.setCalibration(fluoImage.getCalibration());
		ReportingUtils.logMessage("Done.");

		centerCroppedRoi();

//		croppedfluoImage.show();
		croppedfluoImage.setRoi(croppedRoi);
		ReportingUtils.logMessage("Done");

	}

	/**
	 * Method to change scale of ROI (segmented cell)
	 * 
	 * @param scaleFactorForRoiFromBfToFluo
	 *            : double[] where first one is a factor to change width and
	 *            second one is a factor to change height
	 */
	public void setRescaledFluoRoi() {
		ReportingUtils.logMessage("change ROI scale");
		rescaledCellShapeRoi = RoiScaler.scale(cellShapeRoi, bf2FluoWidthFac, bf2FluoHeightFac, false);
		rescaledCellShapeRoi.setName("rescaledCellShapeRoi");
	}

	/**
	 * 
	 * @return Measure object
	 */
	public Measures getMeasures() {
		return measures;
	}

	/**
	 * 
	 * @return true if the cell is alive
	 */
	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * 
	 * @return linear ROI of cell (major axis of cell)
	 */
	public int getMaxNbSpotPerCell() {
		return maxNbSpotPerCell;
	}

	/**
	 * 
	 * @return ROI corresponding to segmented cell
	 */
	public Roi getCellShapeRoi() {
		return cellShapeRoi;
	}

	/**
	 * 
	 * @return ROI corresponding to segmented cell
	 */
	public Roi getCroppedRoi() {
		return croppedRoi;
	}

	/**
	 * Method to set fluorescent image corresponding to cell
	 * 
	 * @param fluoImage
	 */
	public void setFluoImage(ImagePlus fluoImage) {
		this.fluoImage = fluoImage;
	}

	/**
	 * 
	 * @return fluorescent image corresponding to cell
	 */
	public ImagePlus getFluoImage() {
		return fluoImage;
	}

	/**
	 * 
	 * @return cropped fluorescent image corresponding to cell
	 */
	public ImagePlus getCroppedFluoImage() {
		return croppedfluoImage;
	}

	/**
	 * 
	 * @return Last Spindle object computed
	 */
	public Spindle getLastSpindleComputed() {
		return lastSpindleComputed;
	}

	public int getCellNumber() {
		return cellNumber;
	}

	public void setCellShapeRoi(Roi cellShapeRoi) {
		this.cellShapeRoi = cellShapeRoi;
	}

	public void centerCroppedRoi() {
		int[] newXs = rescaledCellShapeRoi.getPolygon().xpoints;
		int[] newYs = rescaledCellShapeRoi.getPolygon().ypoints;
		int nbPoints = rescaledCellShapeRoi.getPolygon().npoints;
		for (int i = 0; i < nbPoints; i++) {
			newXs[i] = newXs[i] - (int) rescaledCellShapeRoi.getXBase();
			newYs[i] = newYs[i] - (int) rescaledCellShapeRoi.getYBase();
		}
		;
		float[] newXsF = new float[nbPoints];
		float[] newYsF = new float[nbPoints];
		for (int i = 0; i < nbPoints; i++) {
			newXsF[i] = (float) newXs[i];
			newYsF[i] = (float) newYs[i];
		}
		;
		croppedRoi = new PolygonRoi(newXsF, newYsF, Roi.POLYGON);
	}

	public void saveCroppedImage(String path) {
		// TODO no path convert??
		String pathToCroppedImgDir = path + "/croppedImgs/";
		String pathToCroppedImg = pathToCroppedImgDir + "/" + String.valueOf(this.getCellNumber());
		if (!new File(pathToCroppedImgDir).exists()) {
			new File(pathToCroppedImgDir).mkdirs();
		}
		ImagePlus imp = new ImagePlus("cell" + getCellNumber(), croppedFluoStack);
		imp.setCalibration(getFluoImage().getCalibration());
		IJ.saveAsTiff(imp, pathToCroppedImg);
	}

	public void addCroppedFluoSlice() {
		if (croppedFluoStack.getSize() == 0) {
			croppedFluoStack = new ImageStack(croppedfluoImage.getWidth(), croppedfluoImage.getHeight());
		}
		ImageProcessor ip = croppedfluoImage.getImageStack().getProcessor(1);
		croppedFluoStack.addSlice(ip);
	}

	public List<String[]> getSpotList() {
		List<String[]> spotsListString = new ArrayList<String[]>();
		for (Spot spot : spotList) {
			Map<String, Double> features = spot.getFeatures();

			String[] featuresString = new String[8];
			featuresString[0] = String.valueOf(features.get("VISIBILITY"));
			featuresString[1] = String.valueOf(features.get("POSITION_T"));
			featuresString[2] = String.valueOf(features.get("POSITION_Z"));
			featuresString[3] = String.valueOf(features.get("POSITION_Y"));
			featuresString[4] = String.valueOf(features.get("RADIUS"));
			featuresString[5] = String.valueOf(features.get("FRAME"));
			featuresString[6] = String.valueOf(features.get("POSITION_X"));
			featuresString[7] = String.valueOf(this.getCellNumber());
			spotsListString.add(featuresString);
		}
		return spotsListString;
	}
}
