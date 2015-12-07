package org.micromanager.cellstateanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.micromanager.internal.utils.ReportingUtils;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.io.TmXmlWriter;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;

/**
 * Class to manipulate a set of cells which corresponds to cells of one field
 * 
 * @author Tong LI
 *
 */
public class SetOfCells implements Iterable<Cell>, Iterator<Cell> {

	public static final String CELL_NUMBER = "Cell_Number";
	private RoiManager roiManager;
	private Roi[] roiArray;
	private int count = 0;
	private ResultsTable rt;
	private ArrayList<Cell> cellArray;
	private String rootSavingPath;
	private HashMap<String, HashMap<Integer, SpotCollection>> spotsInCells;
	private ArrayList<String[]> acqIDs;

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
		ReportingUtils.logMessage("Loading Cells");
		roiArray = getRoisAsArray(rootSavingPath + "/movie_X" + xPos + "_Y" + yPos + "/" + "ROI.zip");
		cellArray = new ArrayList<Cell>();
		for (int i = 0; i < roiArray.length; i++) {
			cellArray.add(i, new Cell(roiArray[i], i + 1));
		}
		ReportingUtils.logMessage("Done.");
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

	public void addChSpotContainer(String channel) {
		if (spotsInCells == null) {
			spotsInCells = new HashMap<String, HashMap<Integer, SpotCollection>>();
		}
		if (!spotsInCells.containsKey(channel)) {
			spotsInCells.put(channel, new HashMap<Integer, SpotCollection>());
		}
	}

	public void putSpot(String channel, int cellNb, int frame, Spot spot) {
		if (spotsInCells.get(channel).get(cellNb) == null) {
			spotsInCells.get(channel).put(cellNb, new SpotCollection());
		}
		spotsInCells.get(channel).get(cellNb).add(spot, frame);
	}

	public HashMap<Integer, SpotCollection> getChannelSpots(String channel) {
		return spotsInCells.get(channel);
	}

	public SpotCollection getCollectionOfSpotInChannel(String channel, int cellNb) {
		return getChannelSpots(channel).get(cellNb);
	}

	public Iterable<Spot> getSpotsInFrame(String channel, int cellNb, int frame) {
		return getChannelSpots(channel).get(cellNb).iterable(frame, true);
	}

	public int getNbOfSpot(String channel, int cellNb, int frame) {
		return getCollectionOfSpotInChannel(channel, cellNb).getNSpots(frame, true);
	}

	public Spot findLowestQualitySpot(String channel, int cellNb, int frame) {
		Iterable<Spot> spotsInFrame = getSpotsInFrame(channel, cellNb, frame);
		double min = 1000;
		Spot lowestQualitySpot = null;
		for (Spot s : spotsInFrame) {
			if (s.getFeature(Spot.QUALITY) < min) {
				min = s.getFeature(Spot.QUALITY);
				lowestQualitySpot = s;
			}
		}
		return lowestQualitySpot;
	}

	public void setRoiMeasurement(ResultsTable rt) {
		this.rt = rt;
	}

	public ResultsTable getRoiMeasurement() {
		return this.rt;
	}

	public void writeResults() {
		for (String[] id : acqIDs) {
			String fluoDir = rootSavingPath + "/movie_X" + id[0] + "_Y" + id[1] + "_FLUO";
			String croppedImgDir = fluoDir + "/croppedImgs/";
			String spotsXmlDir = fluoDir + "/spots/";
			if (!new File(croppedImgDir).exists()) {
				new File(croppedImgDir).mkdirs();
			}
			if (!new File(spotsXmlDir).exists()) {
				new File(spotsXmlDir).mkdirs();
			}
			// save cropped cells
			// cell.saveCroppedImage(croppedImgDir);
			// can be optional
			Model model = new Model();
			System.out.println(this.spotsInCells.size());
			System.out.println(this.spotsInCells.get("GFP").size());
			System.out.println(this.spotsInCells.get("GFP").get(1).getNSpots(false));
			for (String channel : spotsInCells.keySet()) {
				for (int cellNb : spotsInCells.get(channel).keySet()) {
					File newFile = new File(spotsXmlDir + String.valueOf(cellNb) + "_" + channel + ".xml");
					TmXmlWriter writer = new TmXmlWriter(newFile);
					model.setSpots(spotsInCells.get(channel).get(cellNb), true);
					writer.appendModel(model);
					System.out.println("Writing to " + newFile);
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
		}
	}

	public void addAcqIDs(String[] id) {
		if (acqIDs == null) {
			acqIDs = new ArrayList<String[]>();
		}
		acqIDs.add(id);
	}

	// iterator related
	@Override
	public Iterator<Cell> iterator() {
		resetCount();
		return this;
	}

	@Override
	public boolean hasNext() {
		return count < cellArray.size();
	}

	@Override
	public Cell next() {
		if (count >= cellArray.size())
			throw new NoSuchElementException();
		count++;
		return cellArray.get(count - 1);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}

	public void resetCount() {
		this.count = 0;
	}

}
