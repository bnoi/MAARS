package fiji.plugin.maars.cellstateanalysis;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.micromanager.internal.utils.ReportingUtils;

import ij.process.ImageProcessor;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
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

	// informations
	private int cellNumber;
	private int maxNbSpotPerCell;
	private Measures measures;
	private Spindle lastSpindleComputed;

	private CellFluoAnalysis fluoAnalysis;
	private SpotCollection spotCollection;

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
	public Cell(ImagePlus bfImage, ImagePlus fluoImage, int focusSlice,
			Roi roiCellShape, int cellNb, ResultsTable rt, int maxNbSpotPerCell) {

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
	public Cell(ImagePlus bfImage, int focusSlice, Roi roiCellShape,
			int cellNb, ResultsTable rt, int maxNbSpotPerCell) {

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

	/**
	 * Method to find fluorescent spots on cell image and create a Spindle
	 * object
	 * 
	 * @param spotRadius
	 *            : typical spot radius
	 * @return Spindle object
	 */
	public Spindle findFluoSpotTempFunction(double spotRadius) {

		ReportingUtils.logMessage("Create CellFluoAnalysis object");
		this.fluoAnalysis = new CellFluoAnalysis(this, spotRadius);
		ReportingUtils.logMessage("Can't create CellFluoAnalysis object");
		ReportingUtils.logMessage("Get fluorescent spot on image");
		spotCollection = fluoAnalysis.getSpots();
		// TODO
		ReportingUtils.logMessage("Create spindle using spots found");
		Spindle spindle = new Spindle(spotCollection, measures, croppedRoi,
				fluoImage.getCalibration(), cellShapeRoi);

		ReportingUtils.logMessage("Cell : " + croppedRoi.getName()
				+ " spots nb : " + spotCollection.getNSpots(true));
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
			bf2FluoWidthFac = bfImage.getCalibration().pixelWidth
					/ fluoImage.getCalibration().pixelWidth;
			bf2FluoHeightFac = bfImage.getCalibration().pixelHeight
					/ fluoImage.getCalibration().pixelHeight;
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
		Rectangle newRectangle = new Rectangle(
				(int) rescaledCellShapeRoi.getXBase(),
				(int) rescaledCellShapeRoi.getYBase(),
				(int) rescaledCellShapeRoi.getBounds().width,
				(int) rescaledCellShapeRoi.getBounds().height);
		imgProcessor.setRoi(newRectangle);

		ReportingUtils.logMessage("Create cropped fluo image");
		croppedfluoImage = new ImagePlus("croppedImage", imgProcessor.crop());

		ReportingUtils.logMessage("Put new calibration newly cropped image");
		croppedfluoImage.setCalibration(fluoImage.getCalibration());
		ReportingUtils.logMessage("Done.");

		centerCroppedRoi();

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
		rescaledCellShapeRoi = RoiScaler.scale(cellShapeRoi, bf2FluoWidthFac,
				bf2FluoHeightFac, false);
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
		String pathToCroppedImg = pathToCroppedImgDir + "/"
				+ String.valueOf(this.getCellNumber());
		if (!new File(pathToCroppedImgDir).exists()) {
			new File(pathToCroppedImgDir).mkdirs();
		}
		ImagePlus imp = new ImagePlus("cell" + getCellNumber(),
				croppedFluoStack);
		imp.setCalibration(getFluoImage().getCalibration());
		IJ.saveAsTiff(imp, pathToCroppedImg);
	}

	public void addCroppedFluoSlice() {
		if (croppedFluoStack.getSize() == 0) {
			croppedFluoStack = new ImageStack(croppedfluoImage.getWidth(),
					croppedfluoImage.getHeight());
		}
		ImageProcessor ip = croppedfluoImage.getImageStack().getProcessor(1);
		croppedFluoStack.addSlice(ip);
	}

	// public List<String[]> getSpotList() {
	// List<String[]> spotsListString = new ArrayList<String[]>();
	// for (Spot spot : spotCollection) {
	// Map<String, Double> features = spot.getFeatures();
	//
	// String[] featuresString = new String[8];
	// featuresString[0] = String.valueOf(features.get("VISIBILITY"));
	// featuresString[1] = String.valueOf(features.get("POSITION_T"));
	// featuresString[2] = String.valueOf(features.get("POSITION_Z"));
	// featuresString[3] = String.valueOf(features.get("POSITION_Y"));
	// featuresString[4] = String.valueOf(features.get("RADIUS"));
	// featuresString[5] = String.valueOf(features.get("FRAME"));
	// featuresString[6] = String.valueOf(features.get("POSITION_X"));
	// featuresString[7] = String.valueOf(this.getCellNumber());
	// spotsListString.add(featuresString);
	// }
	// return spotsListString;
	// }

	public SpotCollection getNBestOfFeature(SpotCollection collection, String feature, int n){
		SpotCollection newCollection;
		double max = 0;
		for (int i=0; i<n;i++){
			if (inCell(s)) {
				Spot bestSpot = getTheBestOfFeature(collection, feature);
			}
		}
		return 
	}

	public Spot getTheBestOfFeature(SpotCollection collection, String feature) {
		double max = 0;
		Spot best = null;
		for (Spot s : collection.iterable(false)) {
			if (s.getFeature(feature) > max) {
				max = s.getFeature(feature);
				best = s;
			}
		}
		return best;
	}

	public boolean inCell(Spot s) {
		return this.croppedRoi.contains(
				(int) Math.round(s.getFeature("POSITION_X")),
				(int) Math.round(s.getFeature("POSITION_Y")));
	}
}
