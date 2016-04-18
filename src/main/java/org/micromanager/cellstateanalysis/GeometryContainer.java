package org.micromanager.cellstateanalysis;

import java.util.HashMap;
import java.util.Set;

public class GeometryContainer {
	private HashMap<String, HashMap<Integer, HashMap<String, Object>>> geosOfCells;
	private Set<String> headerSet;
	private int geoHeaderLen = 0;

	public GeometryContainer() {
	}

	public Set<String> getHeader() {
		return this.headerSet;
	}

	/**
	 * 
	 * @param channel
	 */
	public void addChannel(String channel) {
		if (this.geosOfCells == null) {
			this.geosOfCells = new HashMap<String, HashMap<Integer, HashMap<String, Object>>>();
		}
		if (!geosOfCells.containsKey(channel)) {
			geosOfCells.put(channel, new HashMap<Integer, HashMap<String, Object>>());
		}
	}

	/**
	 * 
	 * @param channel
	 * @param cellNb
	 * @param frame
	 */
	public boolean geoFrameExists(String channel, int frame) {
		return getGeosInChannel(channel).containsKey(frame);
	}

	/**
	 * 
	 * @param channel
	 * @param cellNb
	 * @param frame
	 * @param geometries
	 */
	public void putGeometry(String channel, int frame, HashMap<String, Object> geometries) {
		if (!geoFrameExists(channel, frame)) {
			getGeosInChannel(channel).put(frame, new HashMap<String, Object>());
		}
		if (geometries.size() > geoHeaderLen) {
			geoHeaderLen = geometries.size();
			headerSet = geometries.keySet();
		}
		getGeosInChannel(channel).put(frame, geometries);
	}

	public Set<String> getUsingChannels() {
		return geosOfCells.keySet();
	}

	public HashMap<Integer, HashMap<String, Object>> getGeosInChannel(String channel) {
		return geosOfCells.get(channel);
	}

}
