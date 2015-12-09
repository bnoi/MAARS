package org.micromanager.cellstateanalysis;

import java.util.ArrayList;
import java.util.HashMap;

import fiji.plugin.trackmate.Spot;

public class ComputeFeatures {
	// Names of parameters
	public final static String PHASE = "phase";
	public final static String NbOfSpotDetected = "NbOfSpotDetected";
	public final static String SpAbsoAng = "SpAbsoAng";
	public final static String SpAngToMaj = "SpAngToMaj";
	public final static String SpLength = "SpLength";
	public final static String SpCenterX = "SpCenterX";
	public final static String SpCenterY = "SpCenterY";
	public final static String SpCenterZ = "SpCenterZ";
	public final static String CellCenterToSpCenterLen = "CellCenterToSpCenterLen";
	public final static String CellCenterToSpCenterAng = "CellCenterToSpCenterAng";
	// values
	public final static String INTERPHASE = "interphase";
	public final static String MITOSIS = "mitosis";
	public final static int NbOfSpotDetected_VAL = (Integer) null;
	public final static double SpAbsoAng_VAL = (Double) null;
	public final static double SpAngToMaj_VAL = (Double) null;
	public final static double SpLength_VAL = (Double) null;
	public final static double SpCenterX_VAL = (Double) null;
	public final static double SpCenterY_VAL = (Double) null;
	public final static double SpCenterZ_VAL = (Double) null;
	public final static double CellCenterToSpCenterLen_VAL = (Double) null;
	public final static double CellCenterToSpCenterAng_VAL = (Double) null;
	private Iterable<Spot> spotSet;
	private String[] measures;

	public ComputeFeatures() {
		this.spotSet = null;
	}

	public ComputeFeatures(Iterable<Spot> spotSet, String[] measures) {
		this.spotSet = spotSet;
		this.measures = measures;
	}

	public HashMap<String, Object> getFeatures() {
		if (spotSet == null) {
			HashMap<String, Object> feature = new HashMap<String, Object>();
			feature.put(PHASE, INTERPHASE);
			feature.put(NbOfSpotDetected, 1);
			return feature;
		} else {
			ArrayList<Spot> poles = findSpindle(spotSet);
//			getSpCenter(poles);
//			getSpAbsAng(poles, measures);
//			getSpAngToMaj(poles, measures);
//			getCellCenterToSpCenterLen(poles, measures);
//			getCellCenterToSpCenterAng(poles, measures);
			return null;
		}
	}

	public ArrayList<Spot> findSpindle(Iterable<Spot> spotSet) {
		ArrayList<Spot> poles = new ArrayList<Spot>();
		
		return poles;
	}
}
