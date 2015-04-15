package fiji.plugin.maars.cellstateanalysis;

import java.awt.Rectangle;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.micromanager.utils.ReportingUtils;

import ij.process.ImageProcessor;
import ij.gui.ProfilePlot;
import fiji.plugin.trackmate.Spot;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.measure.ResultsTable;
import ij.plugin.RoiScaler;
import ij.plugin.Selection;


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
	private double[] scaleFactorForRoiFromBfToFluo;
	private ImagePlus correlationImage;
	private Roi cellShapeRoi;
	private Line cellLinearRoi;
	private ResultsTable rt;
	private int direction;
	// -1 -> cell bounds are black then white
	// 1 -> cell bounds are white then black

	// informations
	private int cellNumber;
	private int septumNumber;
	private ArrayList<Septum> septumArray;
	private Measures measures;
	private Spindle lastSpindleComputed;

	private CellFluoAnalysis fluoAnalysis;

	private boolean isAlive;

	public static final String CELLSHAPE = "cellShapeROI";
	public static final String CELLLINE = "cellLinearROI";

	/**
	 * Constructor :
	 * 
	 * @param bfImage
	 *            : the brightfield image used for segmentation
	 * @param correlationImage
	 *            : the correlation image generated by segmentation
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
	 */
	public Cell(ImagePlus bfImage, ImagePlus correlationImage,
			ImagePlus fluoImage, int focusSlice, int direction,
			Roi roiCellShape, int cellNb, ResultsTable rt) {

		this.bfImage = bfImage;
		lastSpindleComputed = null;
		this.cellShapeRoi = roiCellShape;
		this.correlationImage = correlationImage;
		rt.reset();
		this.rt = rt;
		this.direction = direction;
		measures = new Measures(bfImage, focusSlice, roiCellShape, rt);
		this.cellNumber = cellNb;
		scaleFactorForRoiFromBfToFluo = new double[2];
		addFluoImage(fluoImage);
		// ReportingUtils.logMessage("scale factor for roi : x "+scaleFactorForRoiFromBfToFluo[0]+" y "+scaleFactorForRoiFromBfToFluo[1]);
	}

	/**
	 * 
	 * @param bfImage
	 *            : the brightfield image used for segmentation
	 * @param correlationImage
	 *            : the correlation image generated by segmentation
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
	 */
	public Cell(ImagePlus bfImage, ImagePlus correlationImage, int focusSlice,
			int direction, Roi roiCellShape, int cellNb, ResultsTable rt) {

		ReportingUtils.logMessage("Cell " + roiCellShape.getName());
		ReportingUtils.logMessage("Get parameters");
		this.bfImage = bfImage;
		this.correlationImage = correlationImage;
		this.cellShapeRoi = roiCellShape;
		ReportingUtils.logMessage("Done");
		ReportingUtils.logMessage("Reset result table");
		rt.reset();
		this.rt = rt;
		ReportingUtils.logMessage("Done.");

		this.direction = direction;
		lastSpindleComputed = null;
		this.cellNumber = cellNb;
		ReportingUtils.logMessage("Create Measure object");
		measures = new Measures(bfImage, focusSlice, roiCellShape, rt);
		ReportingUtils.logMessage("done");
		scaleFactorForRoiFromBfToFluo = new double[2];
		// just for test
		// cellLinearRoi = computeCellLinearRoi(measures);
	}

//	/**
//	 * Method used to find if the cell has a septum (DOES NOT WORK WELL): the
//	 * method is based on transversal intensity plot analysis of the cell.
//	 * 
//	 * @param threshold
//	 */
//	public void findSeptum(double threshold) {
//		septumArray = new ArrayList<Septum>();
//		septumNumber = 0;
//		cellLinearRoi = MyCoordinatesGeometry.computeCellLinearRoi(bfImage,
//				measures, true);
//		cellLinearRoi.setName("Linear_" + cellShapeRoi.getName());
//
//		correlationImage.setRoi(cellLinearRoi);
//		ProfilePlot profilePlot = new ProfilePlot(correlationImage);
//		double[] profileArray = profilePlot.getProfile();
//
//		/*
//		 * if (cellShapeRoi.getName().equals("106")) {
//		 * profilePlot.createWindow(); }
//		 */
//
//		// slopeImage.show();
//		// profilePlot.createWindow();
//
//		double[][] peaks = findBoundariesPeaks(profileArray);
//		ReportingUtils.logMessage("\n...........................\ncell : "
//				+ cellShapeRoi.getName() + "\n...........................\n");
//		ReportingUtils.logMessage("peak 1 = " + peaks[0][0] + " peak 2 = "
//				+ peaks[1][0]);
//
//		for (int i = (int) peaks[0][0] + 1; i < peaks[1][0]; i++) {
//			if (profileArray[i] > profileArray[i + 1]
//					&& profileArray[i] > profileArray[i - 1]
//					&& profileArray[i] > 0) {
//				// ReportingUtils.logMessage("i = "+i);
//				boolean isSeptum = false;
//				for (int newAngle = 180; newAngle > 0; newAngle = newAngle - 3) {
//					if (!isSeptum) {
//						int peakWidth = getPeakWidth(profileArray, i);
//						double[] plotNewAngledegres = getNewAngleDegresPlot(
//								cellLinearRoi, cellShapeRoi, measures,
//								correlationImage, i, peakWidth, newAngle);
//						double[][] peaksNewAngledegrees = findBoundariesPeaks(plotNewAngledegres);
//						double[] variableNewAngledegreeThreshold = findVariableThreshold(peaksNewAngledegrees);
//						int j = (int) peaksNewAngledegrees[0][0];
//						// IJ.wait(500);
//						// int peakNumber = 0;
//
//						// ReportingUtils.logMessage("peak 1 = "+peaksNewAngledegrees[0][0]+" peak 2 = "+peaksNewAngledegrees[1][0]);
//
//						while (j < (int) peaksNewAngledegrees[1][0]
//								&& (variableNewAngledegreeThreshold[0] + variableNewAngledegreeThreshold[1]
//										* j)
//										/ plotNewAngledegres[j] < threshold
//								&& plotNewAngledegres[j] > 0) {
//							// ReportingUtils.logMessage((variableNewAngledegreeThreshold[0]
//							// + variableNewAngledegreeThreshold[1] * j)/
//							// plotNewAngledegres[j]);
//							/*
//							 * if (plotNewAngledegres[j] >
//							 * plotNewAngledegres[j+1] && plotNewAngledegres[j]
//							 * > plotNewAngledegres[j-1]) { peakNumber++; }
//							 */
//							j++;
//						}
//						if (j == (int) peaksNewAngledegrees[1][0]) {// &&
//																	// peakNumber
//																	// < 2) {
//							ReportingUtils.logMessage("\n---\nseptum pos : " + i
//									+ "\n---\n");
//							isSeptum = true;
//							septumNumber += 1;
//
//						}
//					}
//				}
//			}
//
//		}
//		// TODO finish that
//	}
//
//	/**
//	 * Method which return an array of double containing plot intensity values
//	 * according to a line defined by a specific angle and position relative to
//	 * cell major axis.
//	 * 
//	 * @param cellLinearRoi
//	 *            : linear ROI corresponding to major axis
//	 * @param cellShapeRoi
//	 *            : ROI corresponding to segmented cell
//	 * @param measures
//	 *            : Measures object of cell
//	 * @param correlationImage
//	 *            : correlation image of cell
//	 * @param peakPosition
//	 *            : position of centre of new line
//	 * @param peakWidth
//	 *            : width of new line
//	 * @param newAngleDegree
//	 *            : angle in degree of new line
//	 * @return double[]
//	 */
//	public double[] getNewAngleDegresPlot(Line cellLinearRoi, Roi cellShapeRoi,
//			Measures measures, ImagePlus correlationImage, int peakPosition,
//			int peakWidth, int newAngleDegree) {
//
//		ProfilePlot ppNewAngle;
//		double[] plotNewAngleDegres;
//		double[] majorAxisCoordinates = MyCoordinatesGeometry
//				.computeCellLinearRoiCoordinates(bfImage, measures);
//		Line lineNewAngledegres;
//		double[] newCoordinates;
//		double[] xyCentroidNewSelection = MyCoordinatesGeometry
//				.convertPolarToCartesianCoor(majorAxisCoordinates[0],
//						majorAxisCoordinates[1], peakPosition,
//						measures.getAngle() + 180);
//		// ReportingUtils.logMessage("supposed 0 : x = "+majorAxisCoordinates[0]+" y = "+majorAxisCoordinates[1]);
//		// ReportingUtils.logMessage("centroid new selection : x = "+xyCentroidNewSelection[0]+" y = "+xyCentroidNewSelection[1]);
//		/*
//		 * double minorAxis; if(bfImage.getCalibration().scaled()) { minorAxis =
//		 * convertMinorAxisLength(measures.getMinor(), measures.getMajor(),
//		 * bfImage.getCalibration()); } else { minorAxis = measures.getMinor();
//		 * }
//		 * 
//		 * newCoordinates =
//		 * computeCoordinatesOfMajorAxis(xyCentroidNewSelection[0],
//		 * xyCentroidNewSelection[1], minorAxis,
//		 * measures.getAngle()-newAngleDegree);
//		 */
//		newCoordinates = MyCoordinatesGeometry
//				.computeCoordinatesOfAjutstedLengthAxis(cellShapeRoi,
//						xyCentroidNewSelection[0], xyCentroidNewSelection[1],
//						measures.getAngle() - newAngleDegree);
//
//		Line.setWidth(peakWidth);
//		lineNewAngledegres = new Line(newCoordinates[0], newCoordinates[1],
//				newCoordinates[2], newCoordinates[3]);
//
//		correlationImage.setRoi(lineNewAngledegres);
//		ppNewAngle = new ProfilePlot(correlationImage);
//
//		// test
//		/*
//		 * if (cellShapeRoi.getName().equals("26")) { ppNewAngle.createWindow();
//		 * }
//		 */
//		plotNewAngleDegres = ppNewAngle.getProfile();
//
//		return plotNewAngleDegres;
//	}
//
//	/**
//	 * Method to get peak width :
//	 *    peak position
//	 *         |
//	 *         v
//	 *         ..
//	 *        .  .
//	 *       .   .
//	 *      .     .
//	 * .....      .......... plot
//	 *      <---->
//	 *     peak width
//	 *     
//	 * @param plot
//	 * @param peakPosition : peak position
//	 * @return
//	 */
//	public int getPeakWidth(double[] plot, int peakPosition) {
//
//		// ReportingUtils.logMessage("peak position : "+peakPosition);
//		// ReportingUtils.logMessage("length plot : "+plot.length);
//
//		boolean firstBottomFound = false;
//		int firstBottom = 0;
//		boolean lastBottomFound = false;
//		int lastBottom = plot.length - 1;
//		int i = 1;
//		while (!firstBottomFound && i < peakPosition) {
//
//			if (plot[peakPosition - (i + 1)] > plot[peakPosition - i]) {
//				firstBottom = i;
//				firstBottomFound = true;
//			}
//			i++;
//		}
//		i = 1;
//		while (!lastBottomFound && i < plot.length - peakPosition) {
//			if (plot[peakPosition + (i + 1)] > plot[peakPosition + i]) {
//				lastBottom = i;
//				lastBottomFound = true;
//			}
//			i++;
//		}
//		return (lastBottom - firstBottom);
//	}
//
//	/**
//	 * Method to find most external peaks
//	 * 
//	 *         ..
//	 *        .  .           . .
//	 *       .   .          .   .
//	 *      .     .        .     .
//	 * .....      .........       .... plot
//	 *         ^              ^
//	 *         |              |
//	 *       peak            peak
//	 *       
//	 * @param plot
//	 * @return
//	 */
//	public double[][] findBoundariesPeaks(double[] plot) {
//		double[][] peaks = new double[2][2];
//		int i = 1;
//		boolean firstPeakFound = false;
//		boolean lastPeakFound = false;
//
//		while ((!firstPeakFound || !lastPeakFound) && i < plot.length) {
//			if (!firstPeakFound) {
//				if (plot[i + 1] < plot[i] && plot[i - 1] < plot[i]) {
//					peaks[0][0] = i;
//					peaks[0][1] = plot[i];
//					firstPeakFound = true;
//				}
//			}
//			if (!lastPeakFound) {
//				if (plot[(plot.length - 1) - (i + 1)] < plot[(plot.length - 1)
//						- i]
//						&& plot[(plot.length - 1) - (i - 1)] < plot[(plot.length - 1)
//								- i]) {
//					peaks[1][0] = (plot.length - 1) - i;
//					peaks[1][1] = plot[(plot.length - 1) - i];
//					lastPeakFound = true;
//				}
//			}
//
//			i++;
//		}
//		return peaks;
//	}
//
//	/**
//	 * find parameters of a linear equation y = ax + b (a and b) that go through
//	 * 2 peaks
//	 * 
//	 * @param peaks
//	 * @return double[] where parameters[1] = a and parameters[0] = b
//	 */
//	public double[] findVariableThreshold(double[][] peaks) {
//		double[] parameters = new double[2];
//		parameters[1] = (peaks[1][1] - peaks[0][1])
//				/ (peaks[1][0] - peaks[0][0]);
//		parameters[0] = peaks[0][1] - (parameters[1] * peaks[0][0]);
//		return parameters;
//	}
//
//	/**
//	 * Return number of septa predicted
//	 * 
//	 * @return
//	 */
//	public int septumNumber() {
//		return septumNumber;
//	}

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
	public Spindle findFluoSpotTempFunction(boolean crop, double spotRadius) {

		scaleRoiForFluoImage(scaleFactorForRoiFromBfToFluo);
		ReportingUtils.logMessage("set ROI on fluo image");
		fluoImage.setRoi(cellShapeRoi);
		if (crop) {
			/*
			 * ReportingUtils.logMessage("crop fluo image");
			 * ReportingUtils.logMessage("bounds of Original image x = "
			 * +fluoImage.getWidth()+" y = "+fluoImage.getHeight());
			 * ReportingUtils.logMessage
			 * ("bounds of cropped image x = "+cellShapeRoi.getBounds
			 * ().width+" y = "+cellShapeRoi.getBounds().height);
			 * ReportingUtils.logMessage
			 * ("origin of cropping x = "+cellShapeRoi.getXBase
			 * ()+" y = "+cellShapeRoi.getYBase());
			 */
			//TODO
			ImageProcessor imgProcessor = fluoImage.getProcessor();
			Rectangle newRectangle = new Rectangle((int)cellShapeRoi.getXBase(), (int)cellShapeRoi.getYBase(),
					(int)cellShapeRoi.getBounds().width, (int)cellShapeRoi.getBounds().height);
			imgProcessor.setRoi(newRectangle);
			ImagePlus newImage = 
					new ImagePlus("CroppedFluoImage", imgProcessor.crop());

			ReportingUtils.logMessage("Done.");
			ReportingUtils.logMessage("Put new calibration newly cropped image");
			newImage.setCalibration(fluoImage.getCalibration());
			ReportingUtils.logMessage("Done.");
			ReportingUtils.logMessage("Set newly cropped image as fluorescent image");
			
			centerTheRoi();
			
//			fluoImage.show();
			setFluoImage(newImage);
			fluoImage.setRoi(cellShapeRoi);
			newImage = null;
			ReportingUtils.logMessage("Done");
		}
		
		ReportingUtils.logMessage("Create CellFluoAnalysis object");
		try {
			this.fluoAnalysis = new CellFluoAnalysis(this, spotRadius);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ReportingUtils.logMessage("Done.");
		ReportingUtils.logMessage("Find fluorescent spot on image");
		ArrayList<Spot> spotList = fluoAnalysis.findSpots();
		ReportingUtils.logMessage("Done.");
		ReportingUtils.logMessage("Create spindle using spots found");
		Spindle spindle = new Spindle(spotList, measures, cellShapeRoi,
				fluoImage.getCalibration());
		ReportingUtils.logMessage("Done.");

		ReportingUtils.logMessage("Cell : " + cellShapeRoi.getName() + " spots nb : "
				+ spotList.size());
		ReportingUtils.logMessage("Back to initial ROI scale");
		rescaleRoiForBFImage(scaleFactorForRoiFromBfToFluo);
		ReportingUtils.logMessage("Done.");
		ReportingUtils.logMessage("Return spindle");
		lastSpindleComputed = spindle;
		return spindle;
	}

	/**
	 * Method to add or update fluorescent image corresponding to cell
	 * 
	 * @param fluoImg
	 */
	public void addFluoImage(ImagePlus fluoImg) {
		fluoImage = null;
		if (fluoImg.getCalibration().getUnit().equals("cm")) {
			fluoImg.getCalibration().setUnit("micron");
			fluoImg.getCalibration().pixelWidth = fluoImg.getCalibration().pixelWidth * 10000;
			fluoImg.getCalibration().pixelHeight = fluoImg.getCalibration().pixelHeight * 10000;
		}

		fluoImage = fluoImg;

		if (bfImage.getCalibration().equals(fluoImage.getCalibration())) {
			setScaleFactorForRoi(1, 1);
		} else {
			setScaleFactorForRoi(
					bfImage.getCalibration().pixelWidth
							/ fluoImage.getCalibration().pixelWidth,
					bfImage.getCalibration().pixelHeight
							/ fluoImage.getCalibration().pixelHeight);
		}
	}

	/**
	 * Method to set a scale factor for the ROI of segmented cell (in case
	 * correlation image and fluo image have different scale)
	 * 
	 * @param width
	 * @param height
	 */
	public void setScaleFactorForRoi(double width, double height) {
		scaleFactorForRoiFromBfToFluo[0] = width;
		scaleFactorForRoiFromBfToFluo[1] = height;
	}

	/**
	 * Method to change scale of ROI (segmented cell)
	 * 
	 * @param scaleFactorForRoiFromBfToFluo
	 *            : double[] where first one is a factor to change width and
	 *            second one is a factor to change height
	 */
	public void scaleRoiForFluoImage(double[] scaleFactorForRoiFromBfToFluo) {
		String name = cellShapeRoi.getName();
		ReportingUtils.logMessage("change ROI scale");
		cellShapeRoi = RoiScaler.scale(cellShapeRoi,
				scaleFactorForRoiFromBfToFluo[0],
				scaleFactorForRoiFromBfToFluo[1], false);
		cellShapeRoi.setName(name);
	}

	/**
	 * Method to change scale back to initial one (method opposite to
	 * scaleRoiForFluoImage())
	 * 
	 * @param scaleFactorForRoiFromBfToFluo
	 */
	public void rescaleRoiForBFImage(double[] scaleFactorForRoiFromBfToFluo) {
		String name = cellShapeRoi.getName();
		cellShapeRoi = RoiScaler.scale(cellShapeRoi,
				1 / scaleFactorForRoiFromBfToFluo[0],
				1 / scaleFactorForRoiFromBfToFluo[1], false);
		cellShapeRoi.setName(name);
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
	public Line getLinearRoi() {
		return cellLinearRoi;
	}

	/**
	 * 
	 * @return ROI corresponding to segmented cell
	 */
	public Roi getCellShapeRoi() {
		return cellShapeRoi;
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
	 * @return Last Spindle object computed
	 */
	public Spindle getLastSpindleComputed() {
		return lastSpindleComputed;
	}
	
	public int getCellNumber(){
		return cellNumber;
	}
	
	public void setCellShapeRoi(Roi cellShapeRoi){
		this.cellShapeRoi = cellShapeRoi;
	}

	public void centerTheRoi(){
		String name = cellShapeRoi.getName();
		int[] newXs =cellShapeRoi.getPolygon().xpoints;
		int[] newYs =cellShapeRoi.getPolygon().ypoints;
		int nbPoints = cellShapeRoi.getPolygon().npoints;
		for(int i=0 ; i < nbPoints;i++){
			newXs[i] = newXs[i] - (int)cellShapeRoi.getXBase();
			newYs[i] = newYs[i] - (int)cellShapeRoi.getYBase();
		};
		float[] newXsF = new float[nbPoints];
		float[] newYsF = new float[nbPoints];
		for(int i=0 ; i < nbPoints ;i++){
			newXsF[i] = (float) newXs[i];
			newYsF[i] = (float) newYs[i];
		};
		PolygonRoi newRoi = new PolygonRoi(newXsF, newYsF, Roi.POLYGON);
		setCellShapeRoi(newRoi);
		cellShapeRoi.setName(name);
		
	}
	public void saveFluoImage(String path){
		IJ.saveAsTiff(fluoImage, path);
	}

}
