package org.micromanager.cellstateanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.micromanager.utils.ImgUtils;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.io.TmXmlWriter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;
import loci.plugins.LociExporter;
import util.opencsv.CSVWriter;

/**
 * Main object of MAARS, you got information about each cell (ROI measurement,
 * analysis related result)
 * 
 * @author Tong LI
 *
 */
public class SetOfCells implements Iterable<Cell>, Iterator<Cell> {

	private RoiManager roiManager;
	private int iteratorCount = 0;
	private int geoHeaderLen = 0;
	private ArrayList<Cell> cellArray;
	private String rootSavingPath;
	private HashMap<String, HashMap<Integer, SpotCollection>> spotsInCells;
	private HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<String, Object>>>> geosOfCells;
	private ArrayList<String[]> acqIDs;
	private Model trackmateModel;
	private HashMap<Integer, HashMap<String, ImagePlus>> croppedImps;
	private Calibration fluoImgCalib;
	private Set<String> headerSet;

	/**
	 * Constructor
	 * 
	 * @param savingPath
	 *            :root directory of acquisitions
	 */
	public SetOfCells(String savingPath) {
		this.rootSavingPath = savingPath;
	}

	/**
	 * @param parameters
	 *            : parameters that used in
	 */
	public void loadCells(String xPos, String yPos) {
		IJ.log("Loading Cells");
		Roi[] roiArray = getRoisAsArray(rootSavingPath + "/movie_X" + xPos + "_Y" + yPos + "/" + "ROI.zip");
		cellArray = new ArrayList<Cell>();
		for (int i = 0; i < roiArray.length; i++) {
			cellArray.add(i, new Cell(roiArray[i], i));
		}
		IJ.log("Done.");
	}

	/**
	 * Method to open ROI file and get them as ROI array
	 * 
	 * @return
	 */
	public Roi[] getRoisAsArray(String pathToRois) {

		roiManager = RoiManager.getInstance();
		if (roiManager == null) {
			roiManager = new RoiManager();
		}
		if (roiManager.getCount() == 0) {
			roiManager.runCommand("Open", pathToRois);
		}
		return roiManager.getRoisAsArray();
	}

	/**
	 * Method to get Cell corresponding to index
	 * 
	 * @param index
	 * @return Cell corresponding to index
	 */
	public Cell getCell(int index) {
		return cellArray.get(index);
	}

	/**
	 * total number of cell
	 * 
	 * @return
	 */
	public int size() {
		return cellArray.size();
	}

	public void addSpotContainerOf(String channel) {
		if (spotsInCells == null) {
			spotsInCells = new HashMap<String, HashMap<Integer, SpotCollection>>();
		}
		if (!spotsInCells.containsKey(channel)) {
			spotsInCells.put(channel, new HashMap<Integer, SpotCollection>());
		}
	}

	public void putSpot(String channel, int cellNb, int frame, Spot spot) {
		if (!spotsInCells.get(channel).containsKey(cellNb)) {
			spotsInCells.get(channel).put(cellNb, new SpotCollection());
		}
		spotsInCells.get(channel).get(cellNb).add(spot, frame);
	}

	/**
	 * Get all cells in the channel
	 * 
	 * @param channel
	 * @return
	 */
	public HashMap<Integer, SpotCollection> getCells(String channel) {
		return spotsInCells.get(channel);
	}

	/**
	 * Get the spot collection of cell
	 * 
	 * @param channel
	 * @param cellNb
	 * @return
	 */
	public SpotCollection getCollections(String channel, int cellNb) {
		return getCells(channel).get(cellNb);
	}

	/**
	 * Get the set of cell in spot collection of frame...
	 * 
	 * @param channel
	 * @param cellNb
	 * @param frame
	 * @return
	 */
	public Iterable<Spot> getSpotsInFrame(String channel, int cellNb, int frame) {
		if (!getCells(channel).containsKey(cellNb)) {
			return null;
		}
		return getCollections(channel, cellNb).iterable(frame, false);
	}

	public void removeSpot(String channel, int cellNb, int frame, Spot spToRemove) {
		spotsInCells.get(channel).get(cellNb).remove(spToRemove, frame);
	}

	/**
	 * Get the number of spot in spotCollection of frame ...
	 * 
	 * @param channel
	 * @param cellNb
	 * @param frame
	 * @return
	 */
	public int getNbOfSpot(String channel, int cellNb, int frame) {
		return getCollections(channel, cellNb).getNSpots(frame, false);
	}

	/**
	 * 
	 * @param channel
	 */
	public void addFeatureContainerOf(String channel) {
		if (this.geosOfCells == null) {
			this.geosOfCells = new HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<String, Object>>>>();
		}
		if (!geosOfCells.containsKey(channel)) {
			geosOfCells.put(channel, new HashMap<Integer, HashMap<Integer, HashMap<String, Object>>>());
		}
	}

	/**
	 * 
	 * @param channel
	 * @param cellNb
	 * @param frame
	 * @param features
	 */
	public void putGeometry(String channel, int cellNb, int frame, HashMap<String, Object> features) {
		if (!geosOfCells.get(channel).containsKey(cellNb)) {
			geosOfCells.get(channel).put(cellNb, new HashMap<Integer, HashMap<String, Object>>());
		}
		if (!geoFrameExists(channel, cellNb, frame)) {
			geosOfCells.get(channel).get(cellNb).put(frame, new HashMap<String, Object>());
		}
		if (features.size() > geoHeaderLen) {
			geoHeaderLen = features.size();
			headerSet = features.keySet();
		}
		geosOfCells.get(channel).get(cellNb).put(frame, features);
	}

	/**
	 * 
	 * @param channel
	 * @param cellNb
	 * @param frame
	 */
	public HashMap<String, Object> getGeometry(String channel, int cellNb, int frame) {
		return geosOfCells.get(channel).get(cellNb).get(frame);
	}

	/**
	 * 
	 * @param channel
	 * @param cellNb
	 * @param frame
	 */
	public boolean geoFrameExists(String channel, int cellNb, int frame) {
		return geosOfCells.get(channel).get(cellNb).containsKey(frame);
	}

	/**
	 * 
	 * @param rt
	 */
	public void setRoiMeasurementIntoCells(ResultsTable rt) {
		for (Cell c : cellArray) {
			c.setRoiMeasurement(rt.getRowAsString(c.getCellNumber()));
		}
	}

	/**
	 * Save header of Trackmate output and spot collection
	 * 
	 * @param model
	 */
	public void setTrackmateModel(Model model) {
		if (this.trackmateModel == null)
			this.trackmateModel = model;
	}

	public ImagePlus stackFluoImgs(String totalNbFrame, String fluoDir, String channel) {
		ImagePlus fluoImg;
		ImagePlus zprojectImg;
		ImageStack fieldStack = null;
		for (int f = 0; f <= Integer.parseInt(totalNbFrame); f++) {
			fluoImg = IJ.openImage(fluoDir + f + "_" + channel + "/MMStack.ome.tif");
			zprojectImg = ImgUtils.zProject(fluoImg);
			if (fieldStack == null) {
				fieldStack = new ImageStack(zprojectImg.getWidth(), zprojectImg.getHeight(),
						Integer.parseInt(totalNbFrame) + 1);
			}
			fieldStack.setProcessor(zprojectImg.getStack().getProcessor(1), f + 1);
		}
		ImagePlus fieldImg = new ImagePlus(channel, fieldStack);
		fieldImg.setCalibration(fluoImgCalib);
		return fieldImg;
	}

	/**
	 * crop ROIs from merged field-wide image
	 * 
	 * @param mergedImg
	 * @return HashMap<cell NB, corresponding cropped img>
	 */
	public HashMap<Integer, HashMap<String, ImagePlus>> cropRois(ImagePlus mergedImg, Boolean splitChannel) {
		HashMap<Integer, HashMap<String, ImagePlus>> croppedImgs = new HashMap<Integer, HashMap<String, ImagePlus>>();
		if (splitChannel) {
			for (int i = 0; i < cellArray.size(); i++) {
				ImagePlus croppedImg = ImgUtils.cropImgWithRoi(mergedImg, cellArray.get(i).getCellShapeRoi());
				HashMap<String, ImageStack> channelStacks = new HashMap<String, ImageStack>();
				for (int j = 1; j <= croppedImg.getImageStack().size(); j++) {
					String currentLabel = croppedImg.getImageStack().getSliceLabel(j);
					if (!channelStacks.containsKey(currentLabel)){
						channelStacks.put(currentLabel, new ImageStack(croppedImg.getWidth(), croppedImg.getHeight()));
					}
					channelStacks.get(currentLabel).addSlice(croppedImg.getStack().getProcessor(j).convertToFloatProcessor());
				}
				HashMap<String, ImagePlus> croppedImgInChannel = new HashMap<String, ImagePlus>();
				for (String channel : channelStacks.keySet()){
					croppedImgInChannel.put(channel, new ImagePlus(channel, channelStacks.get(channel)));
				}
				croppedImgs.put(i, croppedImgInChannel);
			}
		} else {
			for (int i = 0; i < cellArray.size(); i++) {
				ImagePlus croppedImg = ImgUtils.cropImgWithRoi(mergedImg, cellArray.get(i).getCellShapeRoi());
				HashMap<String, ImagePlus> croppedImgInChannel = new HashMap<String, ImagePlus>();
				croppedImgInChannel.put("merged", croppedImg);
				croppedImgs.put(i, croppedImgInChannel);
			}
		}
		return croppedImgs;
	}

	/**
	 * 
	 * @param croppedImgSet
	 * @return cropped images base directory
	 */
	public void saveCroppedImgs(HashMap<Integer, HashMap<String, ImagePlus>> croppedImgSet, String dir2save) {
		if (!new File(dir2save).exists()) {
			new File(dir2save).mkdirs();
		}
		for (int cellNb : croppedImgSet.keySet()) {
			for (String s : croppedImgSet.get(cellNb).keySet()) {
				String pathToCroppedImg = dir2save + String.valueOf(cellNb) + "_" + s;
				ImagePlus imp = croppedImgSet.get(cellNb).get(s);
				IJ.run(imp, "Enhance Contrast", "saturated=0.35");
				imp.setCalibration(fluoImgCalib);
				IJ.saveAsTiff(imp, pathToCroppedImg);
			}
		}
	}

	/**
	 * crop and save images
	 */
	public String saveCroppedImgs(Boolean splitChannel) {
		String[] id = acqIDs.get(acqIDs.size() - 1);
		String xPos = id[0];
		String yPos = id[1];
		String totalFrameNb = id[2];
		String fluoDir = rootSavingPath + "/movie_X" + xPos + "_Y" + yPos + "_FLUO/";
		String croppedImgDir = fluoDir + "croppedImgs/";

		for (String channel : spotsInCells.keySet()) {
			ImagePlus fieldImg = stackFluoImgs(totalFrameNb, fluoDir, channel);
			// save cropped cells
			croppedImps = cropRois(fieldImg, splitChannel);
			saveCroppedImgs(croppedImps, croppedImgDir);
			final String file = fluoDir + channel + ".ome.btf";
			final String macroOpts = "outfile=[" + file
					+ "] splitz=[0] splitc=[0] splitt=[0] compression=[Uncompressed]";
			LociExporter lociExporter = new LociExporter();
			lociExporter.setup(macroOpts, fieldImg);
			lociExporter.run(null);
			IJ.log(channel + " channel cropped images saved");
		}
		return croppedImgDir;
	}

	public void saveSpots() {
		String[] id = acqIDs.get(0);
		String xPos = id[0];
		String yPos = id[1];
		String fluoDir = rootSavingPath + "/movie_X" + xPos + "_Y" + yPos + "_FLUO/";
		String spotsXmlDir = fluoDir + "spots/";
		if (!new File(spotsXmlDir).exists()) {
			new File(spotsXmlDir).mkdirs();
		}
		for (String channel : spotsInCells.keySet()) {
			HashMap<Integer, SpotCollection> currentChannel = spotsInCells.get(channel);
			IJ.log("Find " + currentChannel.size() + " cell(s) with spots in channel " + channel);
			// for each cell
			File newFile = null;
			for (int cellNb : currentChannel.keySet()) {
				// save spots detected
				newFile = new File(spotsXmlDir + String.valueOf(cellNb) + "_" + channel + ".xml");
				TmXmlWriter spotsWriter = new TmXmlWriter(newFile);
				trackmateModel.setSpots(currentChannel.get(cellNb), false);
				spotsWriter.appendModel(trackmateModel);
				try {
					spotsWriter.writeToFile();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void saveGeometries() {
		String[] id = acqIDs.get(0);
		String xPos = id[0];
		String yPos = id[1];
		String fluoDir = rootSavingPath + "/movie_X" + xPos + "_Y" + yPos + "_FLUO/";
		String featuresXmlDir = fluoDir + "features/";
		if (!new File(featuresXmlDir).exists()) {
			new File(featuresXmlDir).mkdirs();
		}
		for (String channel : geosOfCells.keySet()) {
			IJ.log("Saving features of channel " + channel);
			ArrayList<String[]> outLines = null;
			HashMap<String, Integer> headerIndex = new HashMap<String, Integer>();
			String[] headerList = new String[headerSet.size() + 1];
			headerList[0] = "Frame";
			headerList[1] = ComputeGeometry.PHASE;
			headerList[2] = ComputeGeometry.NbOfSpotDetected;
			int index = 3;
			for (String att : headerSet) {
				if (!att.equals(ComputeGeometry.PHASE) && !att.equals(ComputeGeometry.NbOfSpotDetected)) {
					headerIndex.put(att, index);
					headerList[index] = att;
					index++;
				}
			}
			FileWriter cellGeoWriter = null;
			for (int cellNb : geosOfCells.get(channel).keySet()) {
				outLines = new ArrayList<String[]>();
				String[] geoOfFrame = null;
				try {
					cellGeoWriter = new FileWriter(featuresXmlDir + String.valueOf(cellNb) + "_" + channel + ".csv");
				} catch (IOException e) {
					e.printStackTrace();
				}
				HashMap<Integer, HashMap<String, Object>> geosInCell = geosOfCells.get(channel).get(cellNb);
				for (int frame : geosInCell.keySet()) {
					if (geosInCell.containsKey(frame)) {
						geoOfFrame = new String[headerList.length];
						geoOfFrame[0] = String.valueOf(frame);
						geoOfFrame[1] = String.valueOf(geosInCell.get(frame).get(ComputeGeometry.PHASE));
						geoOfFrame[2] = String.valueOf(geosInCell.get(frame).get(ComputeGeometry.NbOfSpotDetected));
						for (String att : geosInCell.get(frame).keySet()) {
							if (!att.equals(ComputeGeometry.PHASE) && !att.equals(ComputeGeometry.NbOfSpotDetected)) {
								geoOfFrame[headerIndex.get(att)] = new String(
										geosInCell.get(frame).get(att).toString());
							}
						}
						outLines.add(geoOfFrame);
					}
				}
				Collections.sort(outLines, new Comparator<String[]>() {
					@Override
					public int compare(String[] o1, String[] o2) {
						return Integer.valueOf(o1[0]).compareTo(Integer.valueOf(o2[0]));
					}
				});
				outLines.add(0, headerList);
				CSVWriter writer = new CSVWriter(cellGeoWriter, ',', CSVWriter.NO_QUOTE_CHARACTER);
				writer.writeAll(outLines);
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void addAcqID(String[] id) {
		if (acqIDs == null) {
			acqIDs = new ArrayList<String[]>();
		}
		this.acqIDs.add(id);
	}

	public void setFluoImgCalib(Calibration fluoImgCalib) {
		this.fluoImgCalib = fluoImgCalib;
	}

	public void reset() {
		this.iteratorCount = 0;
		this.cellArray = null;
		this.spotsInCells = null;
		this.geosOfCells = null;
		this.acqIDs = null;
		this.trackmateModel = null;
		this.croppedImps = null;
		this.fluoImgCalib = null;
	}

	public String getRootSavingPath() {
		return this.rootSavingPath;
	}

	// iterator related
	@Override
	public Iterator<Cell> iterator() {
		resetCount();
		return this;
	}

	@Override
	public boolean hasNext() {
		return iteratorCount < cellArray.size();
	}

	@Override
	public Cell next() {
		if (iteratorCount >= cellArray.size())
			throw new NoSuchElementException();
		iteratorCount++;
		return cellArray.get(iteratorCount - 1);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void resetCount() {
		this.iteratorCount = 0;
	}
}
