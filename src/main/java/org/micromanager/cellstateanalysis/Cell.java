package org.micromanager.cellstateanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.micromanager.maars.MaarsParameters;
import org.micromanager.utils.ImgUtils;

import ij.process.ImageProcessor;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.io.TmXmlWriter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;

/**
 * Cell is a class containing information about cell image, including its
 * mitotic state, its shape, ... TODO
 * 
 * @author Tong LI && marie
 *
 */
public class Cell {

	private int cellNumber;
	private int currentFrame;
	private Boolean visibleOnly;

	// image related
	private ImagePlus fluoImage;
	private ImagePlus focusImg;
	private ImageStack croppedFluoStack = null;
	private Roi cellShapeRoi;
	private Roi croppedCellRoi;

	// analysis informations
	private CellFluoAnalysis fluoAnalysis;
	private Measures measures;

	// Data containers
	private ArrayList<String> channelUsed;
	private Map<String, Object> acquisitionMeta;
	private SpotCollection gfpSpotCollection;
	private SpotCollection cfpSpotCollection;
	private SpotCollection dapiSpotCollection;
	private SpotCollection txredSpotCollection;

	/**
	 * @param roiCellShape
	 *            : ROI that correspond to segmented cell
	 * @param cellNb
	 *            ：cell instance index in array
	 */
	public Cell(Roi roiCellShape, int cellNb) {
		this.cellShapeRoi = roiCellShape;
		this.cellNumber = cellNb;
		this.channelUsed = new ArrayList<String>();
		visibleOnly = true;
	}

	/**
	 * Find fluorescent spots on cell images
	 */
	public synchronized void detectSpots() {
		this.fluoAnalysis = new CellFluoAnalysis(this, acquisitionMeta);
		fluoAnalysis.doDetection();
		fluoAnalysis.filterOnlyInCell(visibleOnly);
		fluoAnalysis.findBestNSpotInCell(visibleOnly);
		for (Spot s : fluoAnalysis.getModel().getSpots().iterable(visibleOnly)) {
			getCollectionOf((String) acquisitionMeta.get(MaarsParameters.CUR_CHANNEL)).add(s, currentFrame);
		}
	}

	//TODO
	public void analyzeSpots() {
		int nSpotDetected = getCollectionOf((String) acquisitionMeta.get(MaarsParameters.CUR_CHANNEL))
				.getNSpots(currentFrame, visibleOnly);
		if (nSpotDetected == 1) {
			// interphase
		} else if (nSpotDetected == 2) {
			// SPBs or cen2
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
		this.croppedCellRoi = fluoImage.getRoi();
	}

	/**
	 * 
	 * @return fluorescent image corresponding to cell
	 */
	public ImagePlus getFluoImage() {
		return fluoImage;
	}

	public void measureBfRoi() {
		this.measures = new Measures(focusImg);
	}

	public Roi rescaleCellShapeRoi(double[] factors) {
		return ImgUtils.rescaleRoi(cellShapeRoi, factors);
	}

	public int getCellNumber() {
		return cellNumber;
	}

	public void saveCroppedImage(String croppedImgDir) {
		String pathToCroppedImg = croppedImgDir + String.valueOf(this.getCellNumber());
		ImagePlus imp = new ImagePlus("cell_" + getCellNumber(), croppedFluoStack);
		imp.setCalibration(getFluoImage().getCalibration());
		IJ.saveAsTiff(imp, pathToCroppedImg);
	}

	public void writeSpotFeatures(String path) {
		Model model = fluoAnalysis.getModel();
		for (String channel : channelUsed) {
			File newFile = new File(path + String.valueOf(this.getCellNumber()) + "_" + channel + ".xml");
			TmXmlWriter writer = new TmXmlWriter(newFile);
			model.setSpots(getCollectionOf(channel), true);
			writer.appendModel(model);
			try {
				writer.writeToFile();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void addCroppedFluoSlice() {
		if (croppedFluoStack == null) {
			croppedFluoStack = new ImageStack(fluoImage.getWidth(), fluoImage.getHeight());
		}
		ImageProcessor ip = fluoImage.getStack().getProcessor(1);
		croppedFluoStack.addSlice(ip);
	}

	public void createContainers() {
		String currentChannel = getCurrentChannel();
		if (currentChannel.equals(MaarsParameters.GFP)) {
			if (gfpSpotCollection == null) {
				gfpSpotCollection = new SpotCollection();
			}
		} else if (currentChannel.equals(MaarsParameters.CFP)) {
			if (cfpSpotCollection == null) {
				cfpSpotCollection = new SpotCollection();
			}
		} else if (currentChannel.equals(MaarsParameters.DAPI)) {
			if (dapiSpotCollection == null) {
				dapiSpotCollection = new SpotCollection();
			}
		} else if (currentChannel.equals(MaarsParameters.TXRED)) {
			if (txredSpotCollection == null) {
				txredSpotCollection = new SpotCollection();
			}
		}
	}

	public String getCurrentChannel() {
		return (String) acquisitionMeta.get(MaarsParameters.CHANNEL);
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
		return croppedCellRoi.contains((int) Math.round(s.getFeature("POSITION_X") / cal.pixelWidth),
				(int) Math.round(s.getFeature("POSITION_Y") / cal.pixelHeight));
	}

	public void setCurrentMetadata(ConcurrentMap<String, Object> acquisitionMeta) {
		this.acquisitionMeta = acquisitionMeta;
		channelUsed.add((String) acquisitionMeta.get(MaarsParameters.CUR_CHANNEL));
	}
}
