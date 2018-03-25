package maars.agents;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import ij.gui.Roi;
import maars.cellAnalysis.GeometryContainer;
import maars.cellAnalysis.SpotsContainer;
import maars.utils.ImgUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Tong LI
 */
public class Cell implements Serializable {
   
   private int cellNumber;
   private transient Roi cellShapeRoi;
   private HashMap<String, Double> measurements = new HashMap<>();
   private transient SpotsContainer spotContainer;
   private GeometryContainer geoContainer;
   private ArrayList<Integer> spotInBetweenFrames = new ArrayList<>();
   private ArrayList<Integer> frameWithUnalignedSpot_ = new ArrayList<>();
   private int anaBOnsetFrame_;

   /**
    * @param roiCellShape : ROI that correspond to segmented cell
    * @param cellNb       ：cell instance index in array
    */
   public Cell(Roi roiCellShape, int cellNb) {
      this.cellShapeRoi = roiCellShape;
      this.cellNumber = cellNb;
      this.spotContainer = new SpotsContainer();
      this.geoContainer = new GeometryContainer();
   }

   /**
    * @return ROI corresponding to segmented cell
    */
   public Roi getCellShapeRoi() {
      return this.cellShapeRoi;
   }

   public Roi rescaleCellShapeRoi(double[] factors) {
      return ImgUtils.rescaleRoi(this.cellShapeRoi, factors);
   }

   public int getCellNumber() {
      return this.cellNumber;
   }

   void setRoiMeasurement(String[] featureNames, String measurements) {
      String[] values = measurements.split("\t", -1);
      assert featureNames.length == values.length;
      for (int i = 0; i < featureNames.length; i++){
         this.measurements.put(featureNames[i], Double.parseDouble(values[i]));
      }
   }

   public double get(String parameter) {
      return measurements.get(parameter);
   }

   public void addChannel(String channel) {
      this.spotContainer.addChannel(channel);
      this.geoContainer.addChannel(channel);
   }

   public void putSpot(String channel, int frame, Spot s) {
      this.spotContainer.putSpot(channel, frame, s);
   }

   public int getNbOfSpots(String channel, int frame) {
      return this.spotContainer.getNbOfSpot(channel, frame);
   }

   public Iterable<Spot> getSpotsInFrame(String channel, int frame) {
      return this.spotContainer.getSpotsInFrame(channel, frame);
   }

   public void removeSpot(String channel, int frame, Spot s) {
      this.spotContainer.removeSpot(channel, frame, s);
   }

   public void setTrackmateModel(Model model) {
      this.spotContainer.setTrackmateModel(model);
   }

   public SpotsContainer getSpotContainer() {
      return this.spotContainer;
   }

   public void putGeometry(String channel, int frame, HashMap<String, Double> geometries) {
      this.geoContainer.putGeometry(channel, frame, geometries);
   }

   public GeometryContainer getGeometryContainer() {
      return this.geoContainer;
   }

   public void addSpotInBtwnFrame(Integer frame) {
      this.spotInBetweenFrames.add(frame);
   }

   public ArrayList<Integer> getSpotInBtwnFrames() {
      return this.spotInBetweenFrames;
   }

   public void setAnaBOnsetFrame(int anaBOnsetFrame) {
      anaBOnsetFrame_ = anaBOnsetFrame;
   }

   public void addFrameWithUnalignedSpot(int frame) {
      frameWithUnalignedSpot_.add(frame);
   }

   public ArrayList<Integer> unalignedSpotFrames() {
      return frameWithUnalignedSpot_;
   }

   public Boolean hasSpotsInBetween(){
      return spotInBetweenFrames.size() > 0;
   }

   public Boolean hasUnalignedDots(){
        return frameWithUnalignedSpot_.size() > 0;
   }
}
