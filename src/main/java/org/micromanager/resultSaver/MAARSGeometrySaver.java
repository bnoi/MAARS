package org.micromanager.resultSaver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import org.micromanager.cellstateanalysis.Cell;
import org.micromanager.cellstateanalysis.ComputeGeometry;
import org.micromanager.cellstateanalysis.GeometryContainer;

import ij.IJ;
import util.opencsv.CSVWriter;

public class MAARSGeometrySaver {
	private GeometryContainer container;
	private String geometryCSVDir;

	public MAARSGeometrySaver(String pathToFluoDir, Cell cell) {
		this.container = cell.getGeometryContainer();
		this.geometryCSVDir = pathToFluoDir + "/features/";
		if (!new File(geometryCSVDir).exists()) {
			new File(geometryCSVDir).mkdirs();
		}
	}

	public void saveGeometries(String channel, HashMap<Integer, HashMap<String, Object>> geosInChannel) {
		Set<String> headerSet = container.getHeader();
		IJ.log("Saving features of channel " + channel);
		ArrayList<String[]> outLines = null;
		HashMap<String, Integer> headerIndex = new HashMap<String, Integer>();
		String[] headerList = new String[headerSet.size() + 1];
		headerList[0] = "Frame";
		headerList[1] = ComputeGeometry.NbOfSpotDetected;
		int index = 2;
		for (String att : headerSet) {
			if (!att.equals(ComputeGeometry.NbOfSpotDetected)) {
				headerIndex.put(att, index);
				headerList[index] = att;
				index++;
			}
		}
		FileWriter cellGeoWriter = null;
		for (int cellNb : geosInChannel.keySet()) {
			outLines = new ArrayList<String[]>();
			String[] geoOfFrame = null;
			try {
				cellGeoWriter = new FileWriter(geometryCSVDir + String.valueOf(cellNb) + "_" + channel + ".csv");
			} catch (IOException e) {
				e.printStackTrace();
			}
			HashMap<Integer, HashMap<String, Object>> geosInCell = geosInChannel;
			for (int frame : geosInCell.keySet()) {
				if (geosInCell.containsKey(frame)) {
					geoOfFrame = new String[headerList.length];
					geoOfFrame[0] = String.valueOf(frame);
					geoOfFrame[1] = String.valueOf(geosInCell.get(frame).get(ComputeGeometry.NbOfSpotDetected));
					for (String att : geosInCell.get(frame).keySet()) {
						if (!att.equals(ComputeGeometry.NbOfSpotDetected)) {
							geoOfFrame[headerIndex.get(att)] = new String(geosInCell.get(frame).get(att).toString());
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

	public void save() {
		for (String channel : container.getUsingChannels()) {
			saveGeometries(channel, container.getGeosInChannel(channel));
		}
	}
}
