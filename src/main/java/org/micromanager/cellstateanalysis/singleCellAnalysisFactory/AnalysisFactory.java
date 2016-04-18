package org.micromanager.cellstateanalysis.singleCellAnalysisFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.micromanager.cellstateanalysis.GeometryContainer;
import org.micromanager.cellstateanalysis.SpotsContainer;
import org.micromanager.maars.MaarsParameters;

public class AnalysisFactory {
	private SpotsContainer spotContainer;
	private GeometryContainer geoContainer = null;
	private ConcurrentHashMap<Integer, Integer> merotelyCandidates = null;
	private ArrayList<String> toDoOptions;

	public AnalysisFactory(ArrayList<String> options) {
		this.toDoOptions = options;
		this.spotContainer = new SpotsContainer();
		if (toDoOptions.contains(MaarsParameters.DO_MITOSIS_RATIO)) {
			this.geoContainer = new GeometryContainer();
		}
		this.merotelyCandidates = new ConcurrentHashMap<Integer, Integer>();
	}

	public ConcurrentHashMap<Integer, Integer> getMerotelyCandidates() {
		return merotelyCandidates;
	}

	public void setMerotelyCandidates(ConcurrentHashMap<Integer, Integer> merotelyCandidates) {
		this.merotelyCandidates = merotelyCandidates;
	}

	public GeometryContainer getGeoContainer() {
		return geoContainer;
	}

	public SpotsContainer getSpotContainer() {
		return spotContainer;
	}

	public SpotsContainer getSpotsContainer() {
		return this.spotContainer;
	}

	public void addChannel(String channel) {
		spotContainer.addChannel(channel);
		geoContainer.addChannel(channel);
	}
}
