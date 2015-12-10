package org.micromanager.cellstateanalysis;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Iterables;

import fiji.plugin.trackmate.Spot;
import ij.measure.ResultsTable;

public class ComputeFeatures {
	// Names of parameters
	public final static String PHASE = "phase";
	public final static String NbOfSpotDetected = "NbOfSpotDetected";
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
	private Iterable<Spot> spotSet;
	private ResultsTable roiMeasurements;
	private int cellNb;
	private double fakeSpotQuality = 1;
	private double fakeSpotZ = 0;
	private double fakeSpotRadius = 0.2;

	public ComputeFeatures(Iterable<Spot> spotSet, ResultsTable roiMeasurements, int cellNb) {
		this.spotSet = spotSet;
		this.roiMeasurements = roiMeasurements;
		this.cellNb = cellNb;
	}

	public HashMap<String, Object> getFeatures() {
		HashMap<String, Object> feature = new HashMap<String, Object>();
		if (Iterables.size(spotSet) < 2) {
			feature.put(PHASE, INTERPHASE);
			feature.put(NbOfSpotDetected, 1);
		} else {
			feature.put(PHASE, MITOSIS);
			feature.put(NbOfSpotDetected, Iterables.size(spotSet));
			ArrayList<Spot> poles = findSpindle(spotSet);
			Vector3D polesVec = getSpAsVector(poles);
			feature.put(SpLength, polesVec.getNorm());
			Spot spCenter = getCenter(poles);
			feature.put(SpCenterX, spCenter.getFeature(Spot.POSITION_X));
			feature.put(SpCenterY, spCenter.getFeature(Spot.POSITION_Y));
			feature.put(SpCenterZ, spCenter.getFeature(Spot.POSITION_Z));
			feature.put(SpAngToMaj, getSpAngToMajAxis(polesVec));
			double cellCenterX = Double.parseDouble(roiMeasurements.getStringValue(ResultsTable.X_CENTROID, cellNb));
			double cellCenterY = Double.parseDouble(roiMeasurements.getStringValue(ResultsTable.Y_CENTROID, cellNb));
			Spot cellCenter = new Spot(cellCenterX, cellCenterY, fakeSpotZ, fakeSpotRadius, fakeSpotQuality);
			feature.put(CellCenterToSpCenterLen, distance(spCenter, cellCenter));
			feature.put(CellCenterToSpCenterAng, spot2Vector3D(spCenter).subtract(spot2Vector3D(cellCenter)));
		}
		return feature;
	}

	public Vector3D getSpAsVector(ArrayList<Spot> poles) {
		Spot sp1 = poles.get(0);
		Spot sp2 = poles.get(1);
		Vector3D v1 = spot2Vector3D(sp1);
		Vector3D v2 = spot2Vector3D(sp2);
		return new Vector3D(1, v1, 1, v2);
	}

	public ArrayList<Spot> findSpindle(Iterable<Spot> spotSet) {
		ArrayList<Spot> poles = new ArrayList<Spot>();
		for (Spot s0 : spotSet) {
			if (poles.size() < 2) {
				poles.add(s0);
			} else {
				Spot s1 = poles.get(0);
				Spot s2 = poles.get(1);
				double tmpDis12 = distance(s1, s2);
				double tmpDis01 = distance(s0, s1);
				double tmpDis02 = distance(s0, s2);
				if (tmpDis01 > tmpDis12) {
					if (tmpDis02 > tmpDis01) {
						poles.remove(1);
					} else {
						poles.remove(2);
					}
					poles.add(s0);
				} else if (tmpDis02 > tmpDis12) {
					if (tmpDis01 > tmpDis02) {
						poles.remove(2);
					} else {
						poles.remove(1);
					}
					poles.add(s0);
				}
			}
		}
		return poles;
	}

	private double distance(Spot s1, Spot s2) {
		double dx = s1.getFeature(Spot.POSITION_X) - s2.getFeature(Spot.POSITION_X);
		double dy = s1.getFeature(Spot.POSITION_Y) - s2.getFeature(Spot.POSITION_Y);
		double dz = s1.getFeature(Spot.POSITION_Z) - s2.getFeature(Spot.POSITION_Z);
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public Spot getCenter(ArrayList<Spot> poles) {
		Spot s1 = poles.get(0);
		Spot s2 = poles.get(1);
		double centerx = (s1.getFeature(Spot.POSITION_X) + s2.getFeature(Spot.POSITION_X)) / 2;
		double centery = (s1.getFeature(Spot.POSITION_Y) + s2.getFeature(Spot.POSITION_Y)) / 2;
		double centerz = (s1.getFeature(Spot.POSITION_Z) + s2.getFeature(Spot.POSITION_Z)) / 2;
		return new Spot(centerx, centery, centerz, fakeSpotRadius, fakeSpotQuality);
	}

	public double getSpAngToMajAxis(Vector3D polesVec) {
		double angle;
		double halfMajAxis = Double.parseDouble(roiMeasurements.getStringValue(ResultsTable.MAJOR, cellNb)) / 2;
		double cellAngle = Double.parseDouble(roiMeasurements.getStringValue(ResultsTable.ANGLE, cellNb));
		if (cellAngle > 90) {
			cellAngle -= 90;
		}
		// z equals to 0 because fitting ellipse in Analyzer do not give z
		// position.
		Vector3D cellMajAxisVec = new Vector3D(halfMajAxis * Math.cos(cellAngle), halfMajAxis * Math.sin(cellAngle), 0);
		angle = Vector3D.angle(polesVec, cellMajAxisVec);
		return angle;
	}

	public Vector3D spot2Vector3D(Spot s) {
		return new Vector3D(s.getFeature(Spot.POSITION_X), s.getFeature(Spot.POSITION_Y),
				s.getFeature(Spot.POSITION_Z));
	}
}
