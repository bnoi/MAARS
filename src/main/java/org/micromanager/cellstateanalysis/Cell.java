package org.micromanager.cellstateanalysis;

import org.micromanager.utils.ImgUtils;

import ij.gui.Roi;

/**
 * Cell is a class containing information about cell image, including its
 * mitotic state, its shape, ... TODO
 * 
 * @author Tong LI && marie
 *
 */
public class Cell {

	private int cellNumber;
	private Roi cellShapeRoi;

	/**
	 * @param roiCellShape
	 *            : ROI that correspond to segmented cell
	 * @param cellNb
	 *            ：cell instance index in array
	 */
	public Cell(Roi roiCellShape, int cellNb) {
		this.cellShapeRoi = roiCellShape;
		this.cellNumber = cellNb;
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

	// public void saveCroppedImage(String croppedImgDir) {
	// String pathToCroppedImg = croppedImgDir +
	// String.valueOf(this.getCellNumber());
	// ImagePlus imp = new ImagePlus("cell_" + getCellNumber(),
	// croppedFluoStack);
	// imp.setCalibration(getFluoImage().getCalibration());
	// IJ.saveAsTiff(imp, pathToCroppedImg);
	// }
	//
	// public void writeSpotFeatures(String path) {
	// Model model = fluoAnalysis.getModel();
	// for (String channel : channelUsed) {
	// File newFile = new File(path + String.valueOf(this.getCellNumber()) + "_"
	// + channel + ".xml");
	// TmXmlWriter writer = new TmXmlWriter(newFile);
	// model.setSpots(getCollectionOf(channel), true);
	// writer.appendModel(model);
	// try {
	// writer.writeToFile();
	// } catch (FileNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	// public void addCroppedFluoSlice() {
	// if (croppedFluoStack == null) {
	// croppedFluoStack = new ImageStack(fluoImage.getWidth(),
	// fluoImage.getHeight());
	// }
	// ImageProcessor ip = fluoImage.getStack().getProcessor(1);
	// croppedFluoStack.addSlice(ip);
	// }
	// public Spot getTheBestOfFeature(SpotCollection collection, String
	// feature) {
	// double max = 0;
	// Spot best = null;
	// for (Spot s : collection.iterable(false)) {
	// if (s.getFeature(feature) > max) {
	// max = s.getFeature(feature);
	// best = s;
	// }
	// }
	// return best;
	// }
}
