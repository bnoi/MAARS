package maars.cellAnalysis;

import fiji.plugin.trackmate.Spot;
import maars.utils.CollectionUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
   private double x, y, major, angle_, calibratedXBase, calibratedYBase;
   private ArrayList<Spot> poles;

   /**
    * @param x               x_centroid of cell in origin fluo image
    * @param y               y_centroid of cell in origin fluo image
    * @param major           cell major axis length
    * @param angle           cell major axis absolut angle
    * @param calibratedXBase x base in micron
    * @param calibratedYBase y base in micron
    */
   SpotSetAnalyzor(double x, double y, double major, double angle, double calibratedXBase,
                   double calibratedYBase) {
      this.x = x;
      this.y = y;
      this.major = major;
      angle_ = angle;
      this.calibratedXBase = calibratedXBase;
      this.calibratedYBase = calibratedYBase;
   }

   /**
    * Calculate the distance between spots
    *
    * @param s1 the first spot
    * @param s2 the second spot
    * @return distance
    */
   public static double distance(Spot s1, Spot s2) {
      double dx = s1.getFeature(Spot.POSITION_X) - s2.getFeature(Spot.POSITION_X);
      double dy = s1.getFeature(Spot.POSITION_Y) - s2.getFeature(Spot.POSITION_Y);
      double dz = s1.getFeature(Spot.POSITION_Z) - s2.getFeature(Spot.POSITION_Z);
      return FastMath.sqrt(dx * dx + dy * dy + dz * dz);
   }

   /**
    * Analyse spotset and return a hashmap of features
    *
    * @param spotSet set of spots to analyze
    */
   HashMap<String, Double> compute(Iterable<Spot> spotSet) {
      // this functions modify directly coordinates of spot in
      // soc, because it's back-up
      // cptgeometry.centerSpots(spotSet);
      HashMap<String, Double> geometry;
      int setSize = CollectionUtils.size(spotSet);
      if (setSize > 1) {
         poles = findMostDistant2Spots(spotSet);
         if (!overlap(poles.get(0), poles.get(1))) {
            geometry = computeGeometry(setSize);
         } else {
            geometry = emptyGeometry();
         }
      } else {
         geometry = emptyGeometry();
      }
      return geometry;
   }

   private HashMap<String, Double> computeGeometry(int setSize) {
      HashMap<String, Double> geometry = new HashMap<>();
      geometry.put(NbOfSpotDetected, (double) setSize);
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
      geometry.put(CellCenterToSpCenterAng, Vector3D.angle(spot2Vector3D(spCenter).subtract(spot2Vector3D(cellCenter)), Vector3D.PLUS_I));
      return geometry;
   }

   ArrayList<Spot> getPoles() {
      return this.poles;
   }

   /**
    * return a vector from two given spots
    *
    * @param poles the 2 spots corresponding to each poles
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
    *
    * @param spotSet set of spots
    * @return spotSet changed set of spots
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
    * @param spotSet a set of spots
    * @return the SPBs
    */
   private ArrayList<Spot> findMostDistant2Spots(Iterable<Spot> spotSet) {
      ArrayList<Spot> poles = new ArrayList<>();
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
    * get the center spot of poles
    *
    * @param poles the 2 SPBs
    * @return center spot
    */
   private Spot getCenter(ArrayList<Spot> poles) {
      Spot s1 = poles.get(0);
      Spot s2 = poles.get(1);
      double centerx = (s1.getFeature(Spot.POSITION_X) + s2.getFeature(Spot.POSITION_X)) / 2 - calibratedXBase;
      double centery = (s1.getFeature(Spot.POSITION_Y) + s2.getFeature(Spot.POSITION_Y)) / 2 - calibratedYBase;
      double centerz = (s1.getFeature(Spot.POSITION_Z) + s2.getFeature(Spot.POSITION_Z)) / 2 - fakeSpotZ;
      return new Spot(centerx, centery, centerz, fakeSpotRadius, fakeSpotQuality);
   }

   /**
    * Get the angle between the spindle and the cell major axis
    *
    * @param polesVec the vector of SPBs
    * @return the angle between polesVec and cell major axe
    */
   private double getSpAngToMajAxis(Vector3D polesVec) {
      Vector3D cellMajAxisVec = new Vector3D(major * FastMath.cos(FastMath.toRadians(angle_)),
            major * FastMath.sin(FastMath.toRadians(angle_)), fakeSpotZ);
      return getLessThan90Ang(FastMath.toDegrees(Vector3D.angle(cellMajAxisVec, polesVec)));
   }

   /**
    * Create a vector from a spot (so [0,0,0] to [x,-y,z])
    *
    * @param s a spot
    * @return a vector
    */
   private Vector3D spot2Vector3D(Spot s) {
      return new Vector3D(s.getFeature(Spot.POSITION_X), -s.getFeature(Spot.POSITION_Y),
            s.getFeature(Spot.POSITION_Z));
   }

   private double getLessThan90Ang(double angle) {
      angle = FastMath.abs(angle);
      while (angle >= 90) {
         angle -= 180;
      }
      return FastMath.abs(angle);
   }

   private boolean overlap(Spot s1, Spot s2) {
      return Objects.equals(s1.getFeature(Spot.POSITION_X), s2.getFeature(Spot.POSITION_X)) &&
            Objects.equals(s1.getFeature(Spot.POSITION_Y), s2.getFeature(Spot.POSITION_Y)) &&
            Objects.equals(s1.getFeature(Spot.POSITION_Z), s2.getFeature(Spot.POSITION_Z));
   }

   private HashMap<String, Double> emptyGeometry() {
      HashMap<String, Double> geometry = new HashMap<>();
      geometry.put(NbOfSpotDetected, 1.);
      geometry.put(SpLength, Double.NaN);
      geometry.put(SpCenterX, Double.NaN);
      geometry.put(SpCenterY, Double.NaN);
      geometry.put(SpCenterZ, Double.NaN);
      geometry.put(SpAngToMaj, Double.NaN);
      geometry.put(CellCenterToSpCenterLen, Double.NaN);
      geometry.put(CellCenterToSpCenterAng, Double.NaN);
      return geometry;
   }
}
