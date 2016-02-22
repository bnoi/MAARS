package org.micromanager.cellstateanalysis;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import fiji.plugin.trackmate.Spot;
//import weka.core.DenseInstance;

public class ComputeGeometry {
	// Names of parameters
	public final static String PHASE = "Phase";
	public final static String NbOfSpotDetected = "NbOfSpotDetected";
	public final static String SpAngToMaj = "SpAngToMaj";
	public final static String SpLength = "SpLength";
	public final static String SpCenterX = "SpCenterX";
	public final static String SpCenterY = "SpCenterY";
	public final static String SpCenterZ = "SpCenterZ";
	public final static String CellCenterToSpCenterLen = "CellCenterToSpCenterLen";
	public final static String CellCenterToSpCenterAng = "CellCenterToSpCenterAng";
	// Velocities
	public final static String SpElongRate = "SpElongRate";
	public final static String SpOrientationRate = "SpOrientationRate";
	// values
	public final static String INTERPHASE = "Interphase";
	public final static String MITOSIS = "Mitosis";

	private double fakeSpotQuality = 0;
	// z equals to 0 because fitting ellipse in Analyzer do not give z
	// position.
	private double fakeSpotZ = 0;
	private double fakeSpotRadius = 0.2;
	private double x, y, major, angle, calibratedXBase, calibratedYBase;

	/**
	 * 
	 * @param x
	 *            x_centroid of cell in origin fluo image
	 * @param y
	 *            y_centroid of cell in origin fluo image
	 * @param major
	 *            cell major axis length
	 * @param angle
	 *            cell major axis absolut angle
	 */
	public ComputeGeometry(double x, double y, double major, double angle, double calibratedXBase,
			double calibratedYBase) {
		this.x = x;
		this.y = y;
		this.major = major;
		this.angle = angle;
		this.calibratedXBase = calibratedXBase;
		this.calibratedYBase = calibratedYBase;
	}

	/**
	 * Analyse spotset and return a hashmap of features
	 * 
	 * @param spotSet
	 *            set of spots to analyze
	 * @return
	 */
	public HashMap<String, Object> compute(HashMap<String, Object> geometry, ArrayList<Spot> poles) {
		// DenseInstance intstance = new DenseInstance(5);
		Vector3D polesVec = getSpAsVector(poles);
		geometry.put(SpLength, polesVec.getNorm());
		Spot spCenter = getCenter(poles);
		geometry.put(SpCenterX, spCenter.getFeature(Spot.POSITION_X));
		geometry.put(SpCenterY, spCenter.getFeature(Spot.POSITION_Y));
		geometry.put(SpCenterZ, spCenter.getFeature(Spot.POSITION_Z));
		geometry.put(SpAngToMaj, getSpAngToMajAxis(polesVec));
		Spot cellCenter = new Spot(x - calibratedXBase, y - calibratedYBase, fakeSpotZ, fakeSpotRadius,
				fakeSpotQuality);
		geometry.put(CellCenterToSpCenterLen, distance(spCenter, cellCenter));
		geometry.put(CellCenterToSpCenterAng, rad2AngLessThan90(
				Vector3D.angle(spot2Vector3D(spCenter).subtract(spot2Vector3D(cellCenter)), Vector3D.PLUS_I)));
		return geometry;
	}

	public HashMap<String, Object> addVariations(HashMap<String, Object> currentGeo, HashMap<String, Object> lastGeo,
			double timeInterval) {
		if (lastGeo.get(PHASE) == MITOSIS && currentGeo.get(PHASE) == MITOSIS) {
			currentGeo.put(SpElongRate, String.format("%.12f", (new Double(currentGeo.get(SpLength).toString())
					- new Double(lastGeo.get(SpLength).toString()) / (timeInterval / 1000))));
			currentGeo.put(SpOrientationRate, String.format("%.12f", (new Double(currentGeo.get(SpAngToMaj).toString())
					- new Double(lastGeo.get(SpAngToMaj).toString()) / (timeInterval / 1000))));
		}
		return currentGeo;
	}

	/**
	 * return a vector from two given spots
	 * 
	 * @param poles
	 * @return the vector
	 */
	public Vector3D getSpAsVector(ArrayList<Spot> poles) {
		Spot sp1 = poles.get(0);
		Spot sp2 = poles.get(1);
		Vector3D v1 = spot2Vector3D(sp1);
		Vector3D v2 = spot2Vector3D(sp2);
		return v1.subtract(v2);
	}

	/**
	 * re-calculate the position of poles. Newly returned coordinates
	 * corresponding the one in cropped image
	 */
	public Iterable<Spot> centerSpots(Iterable<Spot> spotSet) {
		for (Spot s : spotSet) {
			s.putFeature(Spot.POSITION_X, s.getFeature(Spot.POSITION_X) - this.calibratedXBase);
			s.putFeature(Spot.POSITION_Y, s.getFeature(Spot.POSITION_Y) - this.calibratedYBase);
		}
		return spotSet;
	}

	/**
	 * find the poles ( find the two most distant spots)
	 * 
	 * @param spotSet
	 * @return
	 */
	public ArrayList<Spot> findMostDistant2Spots(Iterable<Spot> spotSet) {
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
						poles.remove(0);
					} else {
						poles.remove(1);
					}
					poles.add(s0);
				} else if (tmpDis02 > tmpDis12) {
					if (tmpDis01 > tmpDis02) {
						poles.remove(1);
					} else {
						poles.remove(0);
					}
					poles.add(s0);
				}
			}
		}
		return poles;
	}

	/**
	 * Calculate the distance between spots
	 * 
	 * @param s1
	 * @param s2
	 * @return distance
	 */
	private double distance(Spot s1, Spot s2) {
		double dx = s1.getFeature(Spot.POSITION_X) - s2.getFeature(Spot.POSITION_X);
		double dy = s1.getFeature(Spot.POSITION_Y) - s2.getFeature(Spot.POSITION_Y);
		double dz = s1.getFeature(Spot.POSITION_Z) - s2.getFeature(Spot.POSITION_Z);
		return FastMath.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * get the center spot of poles
	 * 
	 * @param poles
	 * @return center spot
	 */
	public Spot getCenter(ArrayList<Spot> poles) {
		Spot s1 = poles.get(0);
		Spot s2 = poles.get(1);
		double centerx = (s1.getFeature(Spot.POSITION_X) + s2.getFeature(Spot.POSITION_X)) / 2;
		double centery = (s1.getFeature(Spot.POSITION_Y) + s2.getFeature(Spot.POSITION_Y)) / 2;
		double centerz = (s1.getFeature(Spot.POSITION_Z) + s2.getFeature(Spot.POSITION_Z)) / 2;
		return new Spot(centerx, centery, centerz, fakeSpotRadius, fakeSpotQuality);
	}

	/**
	 * Get the angle between the spindle and the cell major axis
	 * 
	 * @param polesVec
	 * @return
	 */
	public double getSpAngToMajAxis(Vector3D polesVec) {
		Vector3D cellMajAxisVec = new Vector3D(major * FastMath.cos(angle), major * FastMath.sin(angle), 0);
		return rad2AngLessThan90(Vector3D.angle(cellMajAxisVec, polesVec));
	}

	/**
	 * Create a vector from a spot (so [0,0,0] to [x,-y,z])
	 * 
	 * @param s
	 * @return
	 */
	public Vector3D spot2Vector3D(Spot s) {
		return new Vector3D(s.getFeature(Spot.POSITION_X), -s.getFeature(Spot.POSITION_Y),
				s.getFeature(Spot.POSITION_Z));
	}

	public double rad2AngLessThan90(double radiant) {
		double ang = FastMath.toDegrees(radiant);
		while (ang >= 90){
			ang -= 180;
		}
		return FastMath.abs(ang);
	}
}
