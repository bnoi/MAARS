package org.micromanager.cellstateanalysis;

import java.util.HashMap;

import fiji.plugin.trackmate.Spot;

public class GeometryCollection {
	public final static String PHASE = "phase";
	public final static String INTERPHASE = "interphase";
	public final static String MITOSIS = "mitosis";
	public final static String  = "mitosis";
	
	private  HashMap<Integer, HashMap<String, Object>> featuresOfFrame;
	public GeometryCollection(int frame, Spot spot){
		if (featuresOfFrame == null){
			featuresOfFrame = new HashMap<Integer, HashMap<String, Object>>();
		}
		HashMap<String, Object> feature = new HashMap<String, Object>();
		feature.put(PHASE, INTERPHASE);
		feature.
		featuresOfFrame.put(frame, value)
	}
}
