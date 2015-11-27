package org.micromanager.cellstateanalysis;

import java.io.File;

import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.maars.MaarsParameters;
import org.micromanager.utils.ImgUtils;

import ij.process.ImageProcessor;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;

/**
 * Cell is a class containing information about cell image, including its
 * mitotic state, its shape, ...
 * 
 * @author Tong LI && marie
 *
 */
public class Cell {

	// tools
	private ImagePlus fluoImage;
	private ImagePlus focusImg;
	private ImageStack croppedFluoStack = null;
	private Roi cellShapeRoi;

	// informations
	private int cellNumber;
	private Measures measures;
	private ResultsTable rt;

	private CellFluoAnalysis fluoAnalysis;
	private CellChannelFactory factory;
	private SpotCollection gfpSpotCollection;
	private SpotCollection cfpSpotCollection;
	private SpotCollection dapiSpotCollection;
	private SpotCollection txredSpotCollection;
	private int currentFrame;

	/**
	 * @param focusImg
	 *            :the brightfield image used for segmentation
	 * @param roiCellShape
	 *            : ROI that correspond to segmented cell
	 * @param cellNb
	 * @param rt
	 *            : result table used to display result of analysis (measures on
	 *            cell)
	 */
	public Cell(Roi roiCellShape, int cellNb) {
		this.cellShapeRoi = roiCellShape;
		rt = new ResultsTable();
		this.cellNumber = cellNb;
	}

	/**
	 * Method to find fluorescent spots on cell image and create a Spindle
	 * object
	 * 
	 */
	public void findFluoSpotTempFunction() {
		// Roi related computation
		// measures;
		// Spot related computation
		Boolean visibleOnly = true;
		this.fluoAnalysis = new CellFluoAnalysis(this, factory);
		fluoAnalysis.doDetection();
		fluoAnalysis.filterOnlyInCell(visibleOnly);
		fluoAnalysis.findBestNSpotInCell(visibleOnly);
		SpotCollection currentCollection = getCollectionOf(factory.getChannel());
		for (Spot s : fluoAnalysis.getModel().getSpots().iterable(visibleOnly)) {
			currentCollection.add(s, currentFrame);

			int nSpotDetected = currentCollection.getNSpots(currentFrame, visibleOnly);
			if (nSpotDetected == 1) {
				// interphase
			} else if (nSpotDetected == 2) {
				// SPBs
			} else if (nSpotDetected > 2 && nSpotDetected <= 4) {
				// SPBs + Cen2 or SPBs + telomeres
			} else if (nSpotDetected > 4 && nSpotDetected <= 6) {
				// SPBs + Cen2 + telomeres or SPBs + NDC80 incomplete
			} else if (nSpotDetected > 6 && nSpotDetected <= 8) {
				// SPBs + NDC80 incomplete
			} else {
				// not manageable
			}
		}
		// ReportingUtils.logMessage("Create spindle using spots found");
		// Spindle spindle = new Spindle(spotCollection, measures, croppedRoi,
		// fluoImage.getCalibration(), cellShapeRoi);
	}

	public void setCurrentFrame(int frame) {
		this.currentFrame = frame;
	}

	public void setFocusImage(ImagePlus focusImg) {
		this.focusImg = focusImg;
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
		this.cellShapeRoi = fluoImage.getRoi();
	}

	/**
	 * 
	 * @return fluorescent image corresponding to cell
	 */
	public ImagePlus getFluoImage() {
		return fluoImage;
	}

	public void measureBfRoi() {
		this.measures = new Measures(focusImg, rt);
	}

	public Roi rescaleRoi(double[] factors) {
		return ImgUtils.rescaleRoi(cellShapeRoi, factors);
	}

	public int getCellNumber() {
		return cellNumber;
	}

	public void setCellShapeRoi(Roi cellShapeRoi) {
		this.cellShapeRoi = cellShapeRoi;
	}

	public void saveCroppedImage(String path) {
		String pathToCroppedImgDir = path + "/croppedImgs/";
		String pathToCroppedImg = pathToCroppedImgDir + "/" + String.valueOf(this.getCellNumber());
		if (!new File(pathToCroppedImgDir).exists()) {
			new File(pathToCroppedImgDir).mkdirs();
		}
		ImagePlus imp = new ImagePlus("cell_" + getCellNumber(), croppedFluoStack);
		imp.setCalibration(getFluoImage().getCalibration());
		IJ.saveAsTiff(imp, pathToCroppedImg);
	}

	public void addCroppedFluoSlice() {
		if (croppedFluoStack == null) {
			croppedFluoStack = new ImageStack(fluoImage.getWidth(), fluoImage.getHeight());
		}
		ImageProcessor ip = fluoImage.getStack().getProcessor(1);
		croppedFluoStack.addSlice(ip);
	}

	public void setChannelRelated(CellChannelFactory factory) {
		this.factory = factory;
		if (factory.getChannel().equals(MaarsParameters.GFP)) {
			if (gfpSpotCollection == null) {
				gfpSpotCollection = new SpotCollection();
			}
		} else if (factory.getChannel().equals(MaarsParameters.CFP)) {
			if (cfpSpotCollection == null) {
				cfpSpotCollection = new SpotCollection();
			}
		} else if (factory.getChannel().equals(MaarsParameters.DAPI)) {
			if (dapiSpotCollection == null) {
				dapiSpotCollection = new SpotCollection();
			}
		} else if (factory.getChannel().equals(MaarsParameters.TXRED)) {
			if (txredSpotCollection == null) {
				txredSpotCollection = new SpotCollection();
			}
		}
	}

	public SpotCollection getCollectionOf(String channel) {
		if (channel.equals(MaarsParameters.GFP)) {
			return gfpSpotCollection;
		} else if (channel.equals(MaarsParameters.CFP)) {
			return cfpSpotCollection;
		} else if (channel.equals(MaarsParameters.DAPI)) {
			return dapiSpotCollection;
		} else if (channel.equals(MaarsParameters.TXRED)) {
			return txredSpotCollection;
		} else {
			return null;
		}
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

	public boolean croppedRoiContains(Spot s) {
		Calibration cal = fluoImage.getCalibration();
		return cellShapeRoi.contains((int) Math.round(s.getFeature("POSITION_X") / cal.pixelWidth),
				(int) Math.round(s.getFeature("POSITION_Y") / cal.pixelHeight));
	}

	/**
	 * XML write of Trackmate need model instead of SpotCollection
	 * 
	 * @param :
	 *            channel name
	 * @return model of Trackmate (see @Model in @Trakmate)
	 */
	public Model getModelOf(String channel) {
		Model model = fluoAnalysis.getModel();
		model.setSpots(getCollectionOf(channel), true);
		return model;
	}
}
