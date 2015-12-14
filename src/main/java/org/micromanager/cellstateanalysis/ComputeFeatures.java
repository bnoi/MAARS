package org.micromanager.cellstateanalysis;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Iterables;

import fiji.plugin.trackmate.Spot;

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
	private double fakeSpotQuality = 0;
	// z equals to 0 because fitting ellipse in Analyzer do not give z
	// position.
	private double fakeSpotZ = 0;
	private double fakeSpotRadius = 0.2;
	private double x, y, major, angle, xbase, ybase;

	public ComputeFeatures(Iterable<Spot> spotSet, double x, double y, double major, double angle, double xbase,
			double ybase) {
		this.spotSet = spotSet;
		this.x = x;
		this.y = y;
		this.major = major;
		this.angle = angle;
		this.xbase = xbase;
		this.ybase = ybase;
	}

	public HashMap<String, Object> getFeatures() {
		HashMap<String, Object> feature = new HashMap<String, Object>();
		if (spotSet == null || Iterables.size(spotSet) == 1) {
			feature.put(PHASE, INTERPHASE);
			feature.put(NbOfSpotDetected, 1);
		} else {
			feature.put(PHASE, MITOSIS);
			feature.put(NbOfSpotDetected, Iterables.size(spotSet));
			ArrayList<Spot> poles = findSpindle(spotSet);
			poles = centerPoles(poles);
			Vector3D polesVec = getSpAsVector(poles);
			feature.put(SpLength, polesVec.getNorm());
			Spot spCenter = getCenter(poles);
			feature.put(SpCenterX, spCenter.getFeature(Spot.POSITION_X));
			feature.put(SpCenterY, spCenter.getFeature(Spot.POSITION_Y));
			feature.put(SpCenterZ, spCenter.getFeature(Spot.POSITION_Z));
			feature.put(SpAngToMaj, getSpAngToMajAxis(polesVec));
			System.out.println(x + "_" + xbase);
			System.out.println(y + "_" + ybase);
			Spot cellCenter = new Spot(x - xbase, y - ybase, fakeSpotZ, fakeSpotRadius, fakeSpotQuality);
			feature.put(CellCenterToSpCenterLen, distance(spCenter, cellCenter));
			feature.put(CellCenterToSpCenterAng,
					Vector3D.angle(spot2Vector3D(spCenter).subtract(spot2Vector3D(cellCenter)), Vector3D.PLUS_I));
		}
		return feature;
	}

	public ArrayList<Spot> centerPoles(ArrayList<Spot> poles) {
		for (Spot s : poles) {
			s.putFeature(Spot.POSITION_X, s.getFeature(Spot.POSITION_X) - xbase);
			s.putFeature(Spot.POSITION_Y, s.getFeature(Spot.POSITION_Y) - ybase);
		}
		return poles;
	}

	public Vector3D getSpAsVector(ArrayList<Spot> poles) {
		Spot sp1 = poles.get(0);
		Spot sp2 = poles.get(1);
		Vector3D v1 = spot2Vector3D(sp1);
		Vector3D v2 = spot2Vector3D(sp2);
		return v1.subtract(v2);
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
		return FastMath.sqrt(dx * dx + dy * dy + dz * dz);
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
		double halfMajAxis = major / 2;
		double cellAngle = angle;
		if (cellAngle > 90) {
			cellAngle -= 90;
		}
		Vector3D cellMajAxisVec = new Vector3D(halfMajAxis * FastMath.cos(cellAngle),
				halfMajAxis * FastMath.sin(cellAngle), 0);
		return Vector3D.angle(polesVec, cellMajAxisVec);
	}

	public Vector3D spot2Vector3D(Spot s) {
		return new Vector3D(s.getFeature(Spot.POSITION_X), s.getFeature(Spot.POSITION_Y),
				s.getFeature(Spot.POSITION_Z));
	}
}
