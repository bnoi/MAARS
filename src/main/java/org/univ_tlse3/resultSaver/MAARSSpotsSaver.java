package org.univ_tlse3.resultSaver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.univ_tlse3.cellstateanalysis.Cell;
import org.univ_tlse3.cellstateanalysis.SpotsContainer;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.io.TmXmlWriter;

public class MAARSSpotsSaver {
	private String spotsXmlDir;
	private SpotsContainer container;

	public MAARSSpotsSaver(String pathToFluoDir) {
		spotsXmlDir = pathToFluoDir + File.separator + "spots" + File.separator;
		if (!new File(spotsXmlDir).exists()) {
			new File(spotsXmlDir).mkdirs();
		}
	}

	private void saveSpots(String channel, SpotCollection spotsInChannel, String cellNb) {
		Model trackmateModel = this.container.getTrackmateModel();
		// for each cell
		File newFile = new File(spotsXmlDir + String.valueOf(cellNb) + "_" + channel + ".xml");
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

	public void save(Cell cell) {
		this.container = cell.getSpotContainer();
		for (String channel : this.container.getUsingChannels()) {
			saveSpots(channel, this.container.getSpots(channel), String.valueOf(cell.getCellNumber()));
		}
	}
}
