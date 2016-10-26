package edu.univ_tlse3.cellstateanalysis;

import com.google.common.collect.Iterables;
import fiji.plugin.trackmate.Spot;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.HashMap;

public class SpotSetAnalyzor {
	// Names of parameters
	public final static String NbOfSpotDetected = "NbOfSpotDetected";
	public final static String SpAngToMaj = "SpAngToMaj";
	public final static String SpLength = "SpLength";
	public final static String SpCenterX = "SpCenterX";
	public final static String SpCenterY = "SpCenterY";
	public final static String SpCenterZ = "SpCenterZ";
	public final static String CellCenterToSpCenterLen = "CellCenterToSpCenterLen";
	public final static String CellCenterToSpCenterAng = "CellCenterToSpCenterAng";
	public final static String[] GeoParamSet = {NbOfSpotDetected, SpAngToMaj, SpLength, CellCenterToSpCenterLen, CellCenterToSpCenterAng};
	private double fakeSpotQuality = 0;
	// z equals to 0 because fitting ellipse in Analyzer do not give z
	// position.
	private double fakeSpotZ = 0;
	private double fakeSpotRadius = 0.25;
	private double x, y, major, angle, calibratedXBase, calibratedYBase;
	private ArrayList<Spot> poles;

	/**
	 * 
	 * @param x
	 *            x_centroid of cell in origin fluo image
	 * 
	 * @param y
	 *            y_centroid of cell in origin fluo image
	 * @param major
	 *            cell major axis length
	 * @param angle
	 *            cell major axis absolut angle
	 * @param calibratedXBase
	 *            x base in micron
	 * @param calibratedYBase
	 *            y base in micron
	 */
	SpotSetAnalyzor(double x, double y, double major, double angle, double calibratedXBase,
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
	 */
	void compute(HashMap<String, Object> geometry, Iterable<Spot> spotSet) {
		// this functions modify directly coordinates of spot in
		// soc, because it's back-up
		// cptgeometry.centerSpots(spotSet);
		int setSize = Iterables.size(spotSet);
		geometry.put(NbOfSpotDetected, setSize);
		if (setSize > 1) {
			poles = findMostDistant2Spots(spotSet);
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
		} else {
			geometry.put(SpLength, "");
			geometry.put(SpCenterX, "");
			geometry.put(SpCenterY, "");
			geometry.put(SpCenterZ, "");
			geometry.put(SpAngToMaj, "");
			geometry.put(CellCenterToSpCenterLen, "");
			geometry.put(CellCenterToSpCenterAng, "");
		}
	}

	ArrayList<Spot> getPoles() {
		return this.poles;
	}

	/**
	 * return a vector from two given spots
	 * 
	 * @param poles
     *      the 2 spots corresponding to each poles
	 * @return the vector
	 */
    private Vector3D getSpAsVector(ArrayList<Spot> poles) {
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
     * a set of spots
	 * @return
     * the SPBs
	 */
    private ArrayList<Spot> findMostDistant2Spots(Iterable<Spot> spotSet) {
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
     * the first spot
	 * @param s2
     * the second spot
	 * @return distance
	 */
	public static double distance(Spot s1, Spot s2) {
		double dx = s1.getFeature(Spot.POSITION_X) - s2.getFeature(Spot.POSITION_X);
		double dy = s1.getFeature(Spot.POSITION_Y) - s2.getFeature(Spot.POSITION_Y);
		double dz = s1.getFeature(Spot.POSITION_Z) - s2.getFeature(Spot.POSITION_Z);
		return FastMath.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * get the center spot of poles
	 * 
	 * @param poles
     * the 2 SPBs
	 * @return center spot
	 */
    private Spot getCenter(ArrayList<Spot> poles) {
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
     * the vector of SPBs
	 * @return
     * the angle between polesVec and cell major axe
	 */
	private double getSpAngToMajAxis(Vector3D polesVec) {
		Vector3D cellMajAxisVec = new Vector3D(major * FastMath.cos(FastMath.toRadians(angle)),
				major * FastMath.sin(FastMath.toRadians(angle)), fakeSpotZ);
		//TODO zero norm error but i do not know why
		return rad2AngLessThan90(Vector3D.angle(cellMajAxisVec, polesVec));
	}

	/**
	 * Create a vector from a spot (so [0,0,0] to [x,-y,z])
	 * 
	 * @param s
     * a spot
	 * @return
     * a vector
	 */
    private Vector3D spot2Vector3D(Spot s) {
		return new Vector3D(s.getFeature(Spot.POSITION_X), -s.getFeature(Spot.POSITION_Y),
				s.getFeature(Spot.POSITION_Z));
	}

	private double rad2AngLessThan90(double radiant) {
		double ang = FastMath.toDegrees(radiant);
		while (ang >= 90) {
			ang -= 180;
		}
		return FastMath.abs(ang);
	}
}
