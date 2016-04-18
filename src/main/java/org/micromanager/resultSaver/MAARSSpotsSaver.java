package org.micromanager.resultSaver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.micromanager.cellstateanalysis.Cell;
import org.micromanager.cellstateanalysis.SpotsContainer;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.io.TmXmlWriter;
import ij.IJ;

public class MAARSSpotsSaver {
	private String spotsXmlDir;
	private SpotsContainer container;

	public MAARSSpotsSaver(String pathToFluoDir, Cell cell) {
		this.container = cell.getSpotContainer();
		spotsXmlDir = pathToFluoDir + "/spots/";
		if (!new File(spotsXmlDir).exists()) {
			new File(spotsXmlDir).mkdirs();
		}
	}

	public void saveSpots(String channel, SpotCollection spotsInChannel) {
		Model trackmateModel = container.getTrackmateModel();
		IJ.log("Find " + spotsInChannel.getNSpots(false) + " cell(s) with spots in channel " + channel);
		// for each cell
		File newFile = null;
		for (int cellNb : spotsInChannel.keySet()) {
			// save spots detected
			newFile = new File(spotsXmlDir + String.valueOf(cellNb) + "_" + channel + ".xml");
			TmXmlWriter spotsWriter = new TmXmlWriter(newFile);
			trackmateModel.setSpots(spotsInChannel, false);
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

	public void save() {
		for (String channel : container.getUsingChannels()) {
			saveSpots(channel, container.getSpots(channel));
		}
	}
}
